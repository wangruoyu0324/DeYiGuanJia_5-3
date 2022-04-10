//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.yxq.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public enum CameraUtil {
    INSTANCE;

    public static final int DEFAULT_PREVIEW_WIDTH = 640;
    public static final int DEFAULT_PREVIEW_HEIGHT = 480;
    private Camera mCamera = null;
    private CameraInfo mCameraInfo = null;
    private OnCameraListener mListener = null;
    private OnTakePictureListener mTakePictureListener = null;

    public void setOnTakePictureListener(OnTakePictureListener listener) {
        this.mTakePictureListener = listener;
    }

    public interface OnTakePictureListener {
        void onPictureTaken(byte[] data, Camera camera);
    }

    private CameraUtil() {
    }


    public void takePicture() {
        if (mCamera != null)
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (mTakePictureListener != null) {
                        mTakePictureListener.onPictureTaken(data, camera);
                    }
                }
            });
    }

    public void setPreviewView(final CameraPreview previewView) {
        if (previewView != null) {
            previewView.addSurfaceCallback(new Callback() {
                public void surfaceCreated(SurfaceHolder holder) {
                    CameraUtil.this.openCamera(holder);
                    CameraUtil.this.updateCameraParameters(previewView);
                }

                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                public void surfaceDestroyed(SurfaceHolder holder) {
                    CameraUtil.this.releaseCamera();
                }
            });
        }

    }

    public void setOnCameraListener(OnCameraListener listener) {
        this.mListener = listener;
    }

    public int getCameraOrientation() {
        return this.mCameraInfo == null ? -1 : this.mCameraInfo.orientation;
    }

    private void releaseCamera() {
        if (this.mCamera != null) {
            try {
                this.mCamera.setPreviewCallback((PreviewCallback) null);
                this.mCamera.stopPreview();
                this.mCamera.release();
            } catch (Exception var2) {
                var2.printStackTrace();
            }

            this.mCamera = null;
        }

    }

    private void openCamera(SurfaceHolder holder) {
        this.releaseCamera();
        CameraInfo info = new CameraInfo();

        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.getCameraInfo(i, info);
            if (info.facing == 1) {
                try {
                    this.mCamera = Camera.open(i);
                    this.mCameraInfo = info;
                    break;
                } catch (RuntimeException var7) {
                    var7.printStackTrace();
                    if (this.mCamera != null) {
                        this.mCamera.release();
                        this.mCamera = null;
                    }
                }
            }
        }

        if (this.mCamera == null) {
            try {
                this.mCamera = Camera.open(0);
                this.mCameraInfo = info;
            } catch (RuntimeException var6) {
                var6.printStackTrace();
                if (this.mCamera != null) {
                    this.mCamera.release();
                    this.mCamera = null;
                }
            }
        }

        if (this.mCamera == null) {
            if (this.mListener != null) {
                this.mListener.onError(CameraError.OPEN_CAMERA);
            }
        } else {
            try {
                this.mCamera.setPreviewDisplay(holder);
            } catch (Exception var5) {
                this.releaseCamera();
                if (this.mListener != null) {
                    this.mListener.onError(CameraError.OPEN_CAMERA);
                }
            }
        }

    }

    private void updateCameraParameters(CameraPreview previewView) {
        if (this.mCamera != null) {
            try {

                Camera.Parameters parameters = this.mCamera.getParameters();//得到摄像头的参数
                parameters.setJpegQuality(80);//设置照片的质量
                parameters.setPreviewSize(480, 480);//设置预览尺寸
                parameters.setPictureSize(480, 480);//设置照片尺寸
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式

//                Parameters parameters = this.mCamera.getParameters();
//                parameters.setPreviewFormat(17);
//                parameters.setPreviewSize(640, 480);
//                if (parameters.getMinExposureCompensation() < 0 && parameters.getMaxExposureCompensation() > 0 && Math.abs(parameters.getMinExposureCompensation()) == parameters.getMaxExposureCompensation()) {
//                    parameters.setExposureCompensation(0);
//                }
//
//                if (parameters.getSupportedFocusModes().contains("continuous-video")) {
//                    parameters.setFocusMode("continuous-video");
//                }

                parameters.setSceneMode("auto");
                if (previewView.getScreenOrientation() != 2) {
                    parameters.set("orientation", "portrait");
                    parameters.set("rotation", 90);
                    if (this.mCameraInfo.facing == 1 && this.mCameraInfo.orientation == 90) {
                        this.mCamera.setDisplayOrientation(270);
                    } else {
                        this.mCamera.setDisplayOrientation(90);
                    }
                } else {
                    parameters.set("orientation", "landscape");
                    this.mCamera.setDisplayOrientation(0);
                }

//                previewView.updatePreviewSize(200, 200);
                this.mCamera.setParameters(parameters);
                this.mCamera.setPreviewCallback(new PreviewCallback() {
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        if (CameraUtil.this.mListener != null) {
                            CameraUtil.this.mListener.onCameraDataFetched(data);
                        }

                    }
                });
                this.mCamera.startPreview();
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

    }

    public void startPreview() {
        this.mCamera.startPreview();
    }
}
