package com.g00dnatur3.app.view.fragment;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.g00dnatur3.camera.CameraUtils;
import com.g00dnatur3.event.Function;
import com.g00dnatur3.view.ViewUtils;
import com.g00dnatur3.view.fragment.HandlerFragment;
import com.g00dnatur3.app.Application;
import com.hp.app.R;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by developer on 11/16/15.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraFragment extends HandlerFragment {

    @Inject
    CameraManager cameraManager;

    @Inject @Named("camera.front.id")
    String cameraId;

    @Inject @Named("camera.front.maxOutputSize")
    Size maxOutputSize;

    private CameraDevice cameraDevice;

    private CaptureRequest.Builder previewRequestBuilder;

    private CameraCaptureSession previewSession;

    private TextureView textureView;

    private ImageReader imageReader;

    private BootstrapButton takePictureBtn;

    private File file;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Application) getActivity().getApplication()).component().inject(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textureView = (TextureView) view.findViewById(R.id.textureView);

        takePictureBtn =  (BootstrapButton) getActivity().findViewById(R.id.take_picture_btn);
        takePictureBtn.setOnClickListener(new View.OnClickListener() {

            Function onLockFocusComplete = new Function() {
                @Override
                public void call(Object... args) {
                    if (args[0] != null) {
                        Log.i(TAG, "lockFocusComplete - err: " + args[0]);
                        return;
                    }
                    Integer state = (Integer) args[1];
                    Log.i(TAG, "lockFocusComplete - state: " + state);
                }
            };

            @Override
            public void onClick(View v) {
                if (cameraDevice != null && previewSession != null && previewRequestBuilder != null) {
                    Log.i(TAG, "takePictureBtn - onClick");
                    CameraUtils.lockFocus(previewSession, previewRequestBuilder, getBackgroundHandler(), onLockFocusComplete);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    Log.i(TAG, "onSurfaceTextureAvailable");
                    openCamera();
                }
                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    Log.i(TAG, "onSurfaceTextureSizeChanged");
                }
                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    Log.i(TAG, "onSurfaceTextureDestroyed");
                    return false;
                }
                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    //Log.i(TAG, "onSurfaceTextureUpdated");
                }
            });
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        super.onPause();
    }

    private void openCamera() {
        adjustAspectRatio();
        Function onComplete = new Function() {
            @Override
            public void call(Object... args) {
                if (args[0] != null) {
                    Log.i(TAG, "openCamera - err: " + args[0]);
                    cameraDevice = null;
                } else {
                    cameraDevice = (CameraDevice) args[1];
                    createPreviewSession();
                }
            }
        };
        CameraUtils.openCamera(getActivity(), cameraManager, cameraId, getBackgroundHandler(), onComplete);
    }

    private void adjustAspectRatio() {
        ViewUtils.adjustAspectRatio(textureView, maxOutputSize, getResources().getConfiguration().orientation);
    }

    private void createPreviewSession() {
        Surface surface = new Surface(textureView.getSurfaceTexture());
        Function onComplete = new Function() {
            @Override
            public void call(Object... args) {
                if (cameraDevice == null) {
                    Log.i(TAG, "createCaptureSession - err: cameraDevice is null");
                    return;
                }
                if (args[0] != null) {
                    Log.i(TAG, "createCaptureSession - err: " + args[0]);
                    previewSession = null;
                    return;
                }
                previewSession = (CameraCaptureSession) args[1];
                previewRequestBuilder = (CaptureRequest.Builder) args[2];
            }
        };
        CameraUtils.createPreviewSession(cameraDevice, surface, getBackgroundHandler(), onComplete);
    }

    public void takePicture() {

        //imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                //ImageFormat.JPEG, /*maxImages*/2);

    }

    private void closeCamera() {
        if (previewSession != null) {
            previewSession.close();
            previewSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

}
