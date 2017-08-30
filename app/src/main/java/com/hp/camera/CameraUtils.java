package com.hp.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.hp.app.view.fragment.CameraFragment;
import com.hp.event.Function;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by developer on 7/17/17.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraUtils {

    private static final String TAG = CameraUtils.class.getCanonicalName();

    public static String getFrontCameraId(CameraManager cameraManager) {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                int facing = cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING);
                if (facing == CameraMetadata.LENS_FACING_FRONT) {
                    return cameraId;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Size> getOutputSizes(CameraManager cameraManager, String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            return Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Size maxOutputSize(List<Size> outputSizes) {
        class CompareSizesByArea implements Comparator<Size> {
            @Override
            public int compare(Size lhs, Size rhs) {
                // We cast here to ensure the multiplications won't overflow
                return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                        (long) rhs.getWidth() * rhs.getHeight());
            }
        }
        return Collections.max(outputSizes, new CompareSizesByArea());
    }

    public static void openCamera(Activity activity, CameraManager cameraManager, String cameraId, Handler handler, final Function onComplete) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice cameraDevice) {
                    Log.i(TAG, "onOpened");
                    onComplete.call(null, cameraDevice);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    Log.i(TAG, "onDisconnected");
                    cameraDevice.close();
                    onComplete.call("camera disconnected");
                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int error) {
                    Log.i(TAG, "onError: " + error);
                    cameraDevice.close();
                    onComplete.call("failed to open camera");
                }
            }, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            onComplete.call("CameraAccessException occurred: " + e.getMessage());
        }
    }

    public static void createPreviewSession(CameraDevice cameraDevice, final Surface surface, final Handler handler, final Function onComplete) {
        try {
            final CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            if (surface == null) Log.i(TAG, "surface is null");

            requestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession previewSession) {
                            Log.i(TAG, "createPreviewSession - onConfigured");
                            try {
                                requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                previewSession.setRepeatingRequest(requestBuilder.build(), null, handler);
                                onComplete.call(null, previewSession, requestBuilder);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                                onComplete.call("session cannot be configured as requested");
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.i(TAG, "createPreviewSession - onConfigureFailed");
                            onComplete.call("session cannot be configured as requested");
                        }
                    }, handler
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
            onComplete.call("CameraAccessException occurred: " + e.getMessage());
        }
    }

    public static void lockFocus(CameraCaptureSession session, CaptureRequest.Builder requestBuilder, final Handler handler, final Function onComplete) {
        try {
            final Function onResult = new Function() {
                private boolean onCompleteCalled = false;
                @Override
                public void call(Object... args) {
                    if (onCompleteCalled) return; //onComplete already called
                    Integer state;
                    if (args[0] instanceof TotalCaptureResult) {
                        state = ((TotalCaptureResult) args[0]).get(CaptureResult.CONTROL_AF_STATE);
                        Log.i(TAG, "lockFocus - complete - state: " + state);
                        onComplete.call(null, state);
                        onCompleteCalled = true;
                    } else {
                        state = ((CaptureResult) args[0]).get(CaptureResult.CONTROL_AF_STATE);
                        Log.i(TAG, "lockFocus - progress - state: " + state);
                        if (state == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                                || state == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                            Log.i(TAG, "CONTROL_AF_STATE_FOCUSED_LOCKED || CONTROL_AF_STATE_NOT_FOCUSED_LOCKED");
                            onComplete.call(null, state);
                            onCompleteCalled = true;
                        }
                    }
                }
            };
            requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            session.capture(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                                @NonNull CaptureRequest request,
                                                @NonNull CaptureResult partialResult) {
                    onResult.call(partialResult);
                }
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    onResult.call(result);
                }
            }, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
