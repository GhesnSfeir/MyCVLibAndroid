package mycvlib.sample.com.mycvlibandroid.opengl;

import android.graphics.SurfaceTexture;
import android.opengl.*;
import android.util.Log;

class EglHelper {
    
    private static String TAG = "MyCVLib_EglHelper";

    private EGLContext eglContext;
    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface;
    private SurfaceTexture eglSurfaceTexture;
    private int[] eglTextures = new int[1];

    SurfaceTexture getEglSurfaceTexture() {
        return eglSurfaceTexture;
    }

    SurfaceTexture createSurface(SurfaceTexture surfaceTexture, boolean isVideo) {
        this.eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] unusedEglVersion = new int[2];
        if (!EGL14.eglInitialize(eglDisplay, unusedEglVersion, 0, unusedEglVersion, 1)) {
            throw new RuntimeException("Unable to initialize EGL14");
        }

        //Prepare the context
        int[] eglContextAttributes = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, //Version 3
                EGL14.EGL_NONE //Null
        };

        EGLConfig eglConfig = createEGLConfig(3, isVideo);
        if (eglConfig != null) {
            eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, eglContextAttributes, 0);
            if (EGL14.eglGetError() != EGL14.EGL_SUCCESS) {
                Log.e(TAG, "Failed to create EGL3 context");
                eglContext = EGL14.EGL_NO_CONTEXT;
            }
        }

        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            eglContextAttributes[1] = 2; //Fall back to version 2
            eglConfig = createEGLConfig(2, isVideo);
            eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, eglContextAttributes, 0);
        }

        // Confirm with query.
        int[] values = new int[1];
        EGL14.eglQueryContext(eglDisplay, eglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0);
        Log.d(TAG, "EGLContext created, client version " + values[0]);

        // Prepare the surface
        int[] surfaceAttributes = {
                EGL14.EGL_NONE //Null
        };
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, surfaceAttributes, 0);
        checkEGLError("eglCreateWindowSurface");
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }

        //Create eglTextures
        GLES20.glGenTextures(eglTextures.length, eglTextures, 0);
        GlUtil.checkGLError("Texture bind");
        eglSurfaceTexture = new SurfaceTexture(eglTextures[0]);

        return eglSurfaceTexture;
    }

    void destroySurface() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "Disposing EGL resources");
            boolean released;
            released = EGL14.eglTerminate(eglDisplay);
            Log.d(TAG, "eglTerminate: " + released);
            released = EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            Log.d(TAG, "eglMakeCurrent NONE: " + released);
            released = EGL14.eglDestroyContext(eglDisplay, eglContext);
            Log.d(TAG, "eglDestroyContext: " + released);
            released = EGL14.eglReleaseThread();
            Log.d(TAG, "eglReleaseThread: " + released);
        }

        eglDisplay = EGL14.EGL_NO_DISPLAY;
        eglContext = EGL14.EGL_NO_CONTEXT;
        eglSurface = EGL14.EGL_NO_SURFACE;
        eglSurfaceTexture = null;
    }

    private EGLConfig createEGLConfig(int version, boolean isVideo) {
        // The actual surface is generally RGBA, so omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        int renderType = version == 3 ? EGLExt.EGL_OPENGL_ES3_BIT_KHR : EGL14.EGL_OPENGL_ES2_BIT;
        int[] attributeList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                //EGL14.EGL_DEPTH_SIZE, 16, //We are not going to use depth buffers
                //EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, renderType,
                EGL14.EGL_NONE, 0,      // placeholder for video, if set
                EGL14.EGL_NONE //Null terminated
        };
        if (isVideo) {
            //Custom flag to allow recording video from openGL texture
            attributeList[attributeList.length - 3] = 0x3142; //Magic
            attributeList[attributeList.length - 2] = 1;
        }
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(eglDisplay, attributeList, 0, configs, 0, configs.length, numConfigs, 0)) {
            Log.e(TAG, "unable to find RGB8888 " + version + " EGLConfig");
            return null;
        }
        return configs[0];
    }


    boolean makeCurrent() {
        boolean success = EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext);
        if (!success) {
            Log.e(TAG, "eglMakeCurrent failed");
        }
        return success;
    }

    boolean swapBuffers() {
        boolean success = EGL14.eglSwapBuffers(eglDisplay, eglSurface);
        if (!success) {
            Log.e(TAG, "eglSwapBuffers failed");
        }
        return success;
    }

    /**
     * Checks for EGL errors.  Throws an exception if an error has been raised.
     */
    private static void checkEGLError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }
}
