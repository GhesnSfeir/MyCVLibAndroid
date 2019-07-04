package mycvlib.sample.com.mycvlibandroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;
import mycvlib.sample.com.mycvlib.Engine;
import mycvlib.sample.com.mycvlibandroid.opengl.DefaultCameraRenderer;
import mycvlib.sample.com.mycvlibandroid.opengl.TextureViewGLWrapper;

import java.util.Arrays;

public class CameraBaseActivity extends AppCompatActivity {

    private static final String TAG = "MyCVLib_CamBaseAct";

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    boolean mCanOpenCamera = false;
    CameraManager mCameraManager = null;
    CameraDevice mCameraDevice = null;
    CameraCaptureSession mSession = null;
    Surface mSurface = null;

    TextureView mTextureView = null;
    SurfaceTexture mSurfaceTexture = null;
    HandlerThread mBackgroundThread = new HandlerThread("BackgroundThread");
    Handler mBackgroundHandler = null;

    TextureViewGLWrapper mTextureViewGLWrapper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_base);

        mTextureView = (TextureView) findViewById(R.id.texture_view);

        DefaultCameraRenderer defaultCameraRenderer = new DefaultCameraRenderer(this);

        mTextureViewGLWrapper = new TextureViewGLWrapper(defaultCameraRenderer);
        mTextureViewGLWrapper.setListener(new TextureViewGLWrapper.EGLSurfaceTextureListener() {
            @Override
            public void onSurfaceTextureReady(SurfaceTexture surfaceTexture) {
                mSurfaceTexture = surfaceTexture;
                openCamera();
            }
        }, new Handler(getMainLooper()));

        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mTextureViewGLWrapper.onSurfaceTextureAvailable(surface, width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mTextureViewGLWrapper.onSurfaceTextureSizeChanged(surface, width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return mTextureViewGLWrapper.onSurfaceTextureDestroyed(surface);
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                mTextureViewGLWrapper.onSurfaceTextureUpdated(surface);
            }
        });

        String text = "OpenCV version detected: " + Engine.getVersionString();

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        if (!mCanOpenCamera) return;
        if (!mTextureView.isAvailable()) return;
        if (mSurfaceTexture == null) return;
        if (mCameraDevice != null) return;
        
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraManager.openCamera("0", new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    mSurface = new Surface(mSurfaceTexture);
                    mSurfaceTexture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
                    try {
                        final CaptureRequest.Builder req = mCameraDevice.createCaptureRequest(mCameraDevice.TEMPLATE_PREVIEW);
                        req.addTarget(mSurface);
                        mCameraDevice.createCaptureSession(Arrays.asList(mSurface), new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                req.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                req.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                                req.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
                                try {
                                    session.setRepeatingRequest(req.build(), null, null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                                mSession = session;
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                Log.e(TAG, "onConfigureFailed...");
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "onError: Cannot open Camera");
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (mSession != null) {
            mSession.close();
            mSession = null;
        }
        mCameraDevice.close();
        mCameraDevice = null;
        mSurfaceTexture = null;
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            new Camera2BasicFragment.ConfirmationDialog().show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Camera2BasicFragment.ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
            } else {
                mCanOpenCamera = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
