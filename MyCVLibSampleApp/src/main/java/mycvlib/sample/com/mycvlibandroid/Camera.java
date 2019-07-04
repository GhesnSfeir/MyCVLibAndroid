package mycvlib.sample.com.mycvlibandroid;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

public class Camera {

    private String mCameraId;

    public Camera(CameraManager manager) {
        try {
            setUpCameraId(manager);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCameraId(CameraManager manager) throws CameraAccessException {
        for (String cameraId : manager.getCameraIdList()) {
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);
            Integer cameraDirection = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_BACK) {
                mCameraId = cameraId;
                break;
            }
        }
    }
}
