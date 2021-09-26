package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.chuxinbuer.deyiguanjia.MyApplication;
import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.base.BaseActivity;
import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.database.AppConfigManager;
import com.chuxinbuer.deyiguanjia.database.AppConfigPB;
import com.chuxinbuer.deyiguanjia.face.AppConfig;
import com.chuxinbuer.deyiguanjia.face.CameraHelper;
import com.chuxinbuer.deyiguanjia.face.CameraListener;
import com.chuxinbuer.deyiguanjia.face.FaceOverlayView;
import com.chuxinbuer.deyiguanjia.face.FaceResult;
import com.chuxinbuer.deyiguanjia.face.FeaturesBindIds;
import com.chuxinbuer.deyiguanjia.face.MovingAverage;
import com.chuxinbuer.deyiguanjia.face.NV21ToBitmap;
import com.chuxinbuer.deyiguanjia.face.SimiIndexPair;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.mvp.model.FaceModel;
import com.chuxinbuer.deyiguanjia.mvp.presenter.HttpsPresenter;
import com.chuxinbuer.deyiguanjia.mvp.view.iface.IBaseView;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.chuxinbuer.deyiguanjia.utils.DestroyActivityUtil;
import com.chuxinbuer.deyiguanjia.utils.LogUtils;
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;
import com.chuxinbuer.deyiguanjia.widget.CountDownButton;
import com.dface.api.FaceAssessment;
import com.dface.api.FaceCompare;
import com.dface.api.FaceDetect;
import com.dface.api.FaceRGBLiveness;
import com.dface.api.FaceRecognize;
import com.dface.api.FaceTool;
import com.dface.dto.AccuracyMode;
import com.dface.dto.Bbox;
import com.dface.dto.DetectMode;
import com.dface.dto.LivenessLevel;
import com.dface.dto.LivenessType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

import static java.lang.Math.min;

public class LoginActivity_Face extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener, IBaseView {
    @BindView(R.id.mRemainTime)
    CountDownButton mRemainTime;
    @BindView(R.id.single_face_surfaceview)
    SurfaceView previewView;
    @BindView(R.id.overlay_view)
    FaceOverlayView faceOverlayView;


    private static final String TAG = LoginActivity_Face.class.toString();
    private Handler handler;
    private CameraHelper cameraHelper;
    private Camera.Size previewSize;

    //camera ID
    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    //private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;

    private NV21ToBitmap yuvConvert = null;


    int MAX_FACE_COUNT = 1;
    int DETECT_MIN_SIZE = 80;
    int RECOGNIZE_MIN_SIZE = 100;
    //Monocular RGB Liveness detect filter
    boolean RGBLIVE_FILTER = true;
    boolean QUALITY_FILTER = true;
    //Monocular RGB Liveness threshold
    float RGBLIVE_THRESHOLD = 0.70f;
    //Monocular RGB Liveness level
    int RGBLIVE_LEVEL = 2;
    float QUALITY_THRES = 0.4f;
    //Face detection precision mode
    private int DETECT_PRECISION = DetectMode.Precision_Low.getMode();
//  private int DETECT_PRECISION = DetectMode.Precision_High.getMode();


    private int cameraWidth = 640;
    private int cameraHeight = 480;
    //Similarity threshold
    private float simiConfidence = 0.70f;
    //Extra rotation angle of color camera
    int cameraAdditionalRotate = 0;
    //Whether the color camera is flipped
    boolean cameraMirrow = false;
    //Allow Recognition flag (Recognition is no longer allowed within 3 seconds after successful recognition)
    boolean allowRecognizeFlag = true;
    //Detected faces (face frame rendering data cache)
    public FaceResult drawFaceBuffer[];
    private FeaturesBindIds allFaceFeaturesBuffer = new FeaturesBindIds();
    //RGBLiveness score moving average
    MovingAverage movingAverage = new MovingAverage(3);
    //Fake counter
    private int fakeCounter = 0;
    //max fake count, notify some message to user
    private int maxFakeCount = 10;
    long loadFeatureTime = 0;

    private FaceCompare faceCompare = new FaceCompare();
    private FaceRecognize faceRecognize = new FaceRecognize();
    private FaceDetect faceDetect = new FaceDetect();
    private FaceAssessment faceAssessment = new FaceAssessment();
    private FaceRGBLiveness faceRGBLiveness = new FaceRGBLiveness();
    private FaceTool faceTool = new FaceTool();

    private boolean initSuccess = false;
    long start, end;
    int counter = 0;
    double fps;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_login_face;
    }

    @Override
    protected void init() {
        mRemainTime.start();
        mRemainTime.setOnFinishTimeClick(new CountDownButton.OnFinishTimeClick() {
            @Override
            public void onFinishTimeClick() {
                if (Common.isTopActivity(LoginActivity_Face.this, "com.chuxinbuer.deyiguanjia.mvp.view.activity.LoginActivity_Face")) {
                    Common.openActivity(LoginActivity_Face.this, LoginActivity.class);
                }
                stopTask();
            }
        });

        if (Common.empty(MyApplication.getInstance().getFaceList())) {
            try {
                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.LAST_REQUESTTIME, "0");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Map<String, String> map = new HashMap<>();
        map.put("deviceno", AppConfigManager.getInitedAppConfig().getDevice_token());
        map.put("type", "1");
        map.put("start", AppConfigManager.getInitedAppConfig().getLast_requesttime());
        new HttpsPresenter(LoginActivity_Face.this, LoginActivity_Face.this).request(map, Constant.FACELIST);
    }


    /**
     * Initialize the engine
     * We recommend setting them(faceDetect,faceRecognize,faceRGBLiveness,facePose) as global objects in the production environment
     * to avoid repeated initialization each time and reduce the time spent on activity startup
     */
    private void initEngine(AppConfig appConfig) {
        String modelPath = getModelPath();
        /**initialization faceDetect facePose faceRecognize faceCompare
         */
        initSuccess = faceDetect.initLoad(modelPath, AppConfig.DETECT_PRECISION);
        initSuccess = faceCompare.initLoad(modelPath, AccuracyMode.V3.getMode());
        initSuccess = faceRecognize.initLoad(modelPath, AccuracyMode.V3.getMode());
        initSuccess = faceRGBLiveness.initLoad(modelPath, LivenessLevel.LEVEL_2.getLevel());
        initSuccess = faceAssessment.initLoad(modelPath);
    }

    /**
     * Destruction engine
     */
    private void unInitEngine() {
        if(initSuccess) {
            faceCompare.uninitLoad();
            faceRGBLiveness.uninitLoad();
            faceRecognize.uninitLoad();
            faceDetect.uninitLoad();
            faceAssessment.uninitLoad();
        }
    }

    /**
     * Read configuration file
     * @return Return configuration information object
     */
    private AppConfig readConfig(){
        return new AppConfig();
    }



    private SimiIndexPair findMaxSimilarity(byte[] feature, List<byte[]> featureN){
        int featureCount = featureN.size();

        float maxVal = 0.0f;
        int maxIndex = 0;
        for (int j = 0; j<featureCount; j++){
            float simi = faceCompare.similarityByFeature(feature, featureN.get(j));
            if(simi > maxVal){
                maxVal = simi;
                maxIndex = j;
            }
        }
        SimiIndexPair simiIndexPair = new SimiIndexPair(maxVal, maxIndex);
        return simiIndexPair;
    }


    private void freshDisplayDrawFaceBoxs(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                //send face to FaceView to draw rect
                faceOverlayView.setFaces(drawFaceBuffer);
                faceOverlayView.setDrawLive(true);
            }
        });
    }

    private void freshDrawFps(){
        end = System.currentTimeMillis();
        counter++;
        double time = (double) (end - start) / 1000;
        if (time != 0)
            fps = counter / time;
        faceOverlayView.setFPS(fps);
        if (counter == (Integer.MAX_VALUE - 1000))
            counter = 0;
    }


    private void clearDrawFaceBuffer(){
        for(int i=0; i<drawFaceBuffer.length; i++){
            drawFaceBuffer[i].clear();
        }
    }



    @Override
    protected void onDestroy() {
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
        unInitEngine();
        super.onDestroy();
    }

    @Override
    protected void initBundleData() {
        //Keep screen bright on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                break;
        }

        handler = new Handler();

        // After the activity starts, it locks to the direction when it started
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        yuvConvert = new NV21ToBitmap(this);

        //Data cache initialization for face frame rendering
        drawFaceBuffer = new FaceResult[MAX_FACE_COUNT];
        for (int i = 0; i < MAX_FACE_COUNT; i++) {
            drawFaceBuffer[i] = new FaceResult();
        }

        //Read configuration file
        AppConfig appConfig = readConfig();

        cameraWidth = appConfig.CAMERA_WIDTH;
        cameraHeight = appConfig.CAMERA_HEIGHT;
        cameraID = appConfig.COLOR_CAMERAID;
        simiConfidence = appConfig.CONFIDENCE;

        DETECT_MIN_SIZE = appConfig.DETECT_MIN_SIZE;
        RECOGNIZE_MIN_SIZE = appConfig.RECOGNIZE_MIN_SIZE;
        QUALITY_THRES = appConfig.QUALITY_THRES;
        cameraAdditionalRotate = appConfig.CAMERA_ADDITIONAL_ROTATE;
        cameraMirrow = appConfig.CAMERA_IS_MIRROW;
        RGBLIVE_FILTER = appConfig.ENABLE_RGBLIVENESS_FILTER;
        QUALITY_FILTER = appConfig.ENABLE_QUALITY_FILTER;
        DETECT_PRECISION = appConfig.DETECT_PRECISION;
        RGBLIVE_THRESHOLD = appConfig.RGB_LIVENESS_THRES;
        RGBLIVE_LEVEL = appConfig.RGB_LIVENESS_LEVEL;


        int camera_size = min(cameraWidth, cameraHeight);
        //Monocular RGB liveness detect effective distance within 1 meter
        if(camera_size >= 700){
            RECOGNIZE_MIN_SIZE = 120;
        }

        //Initialize dface
        initEngine(appConfig);

        setBrightness(0.7f);
    }

    private String getModelPath(){
        File sdDir = Environment.getExternalStorageDirectory();//Get SD card root directory
        String modelPath = sdDir.getPath() + "/dface/model/";
        return modelPath;
    }


    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        CameraListener cameraListener = new CameraListener() {

            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                previewSize = camera.getParameters().getPreviewSize();

                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    faceOverlayView.setFront(true);
                } else {
                    faceOverlayView.setFront(false);
                }
                faceOverlayView.setDisplayOrientation(displayOrientation);
                if(displayOrientation == 90 || displayOrientation == 270) {
                    faceOverlayView.setPreviewSize(previewSize.height, previewSize.width);
                }else{
                    faceOverlayView.setPreviewSize(previewSize.width, previewSize.height);
                }
                start = System.currentTimeMillis();

            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera, int cameraRotation) {
                try {
                    if (!initSuccess) {
                        Common.openActivity(LoginActivity_Face.this, AuthActivity.class);
                        stopTask();
                        return;
                    }

                    int cameraPreviewWidth = camera.getParameters().getPreviewSize().width;
                    int cameraPreviewHeight = camera.getParameters().getPreviewSize().height;

                    Bitmap frameBitmap = yuvConvert.nv21ToBitmap(nv21, cameraPreviewWidth, cameraPreviewHeight);
                    //Rotate the image to the positive direction
                    Bitmap rotatedBitmap = faceTool.rotateBitmap(frameBitmap, cameraRotation);
                    faceDetect.SetMinSize(DETECT_MIN_SIZE);

                    //Detect face
                    List<Bbox> detectFaceBoxs;
                    synchronized (faceDetect) {
                        detectFaceBoxs = faceDetect.detectionMax(rotatedBitmap, true);
                    }

                    if (detectFaceBoxs == null || detectFaceBoxs.isEmpty() || !allowRecognizeFlag) {
                        clearDrawFaceBuffer();
                        freshDisplayDrawFaceBoxs();
                        freshDrawFps();
                        if (frameBitmap != null && !frameBitmap.isRecycled()) {
                            frameBitmap.recycle();
                            frameBitmap = null;
                        }
                        return;
                    }

                    Bbox faceBox = detectFaceBoxs.get(0);
                    drawFaceBuffer[0].setFace(0, faceBox.getScore(), faceBox.getX(), faceBox.getY(), faceBox.getWidth(), faceBox.getHeight(), faceBox.getPoint(), System.currentTimeMillis(), "", Color.WHITE);
                    freshDisplayDrawFaceBoxs();
                    freshDrawFps();
                    LivenessType livenessType = LivenessType.REAL;
                    if (faceBox.getWidth() < RECOGNIZE_MIN_SIZE || faceBox.getHeight() < RECOGNIZE_MIN_SIZE) {
//                        String statusMessage = "Small Size";
//                        drawFaceBuffer[0].setFace(0, 1.0f, faceBox.getX(), faceBox.getY(), faceBox.getWidth(), faceBox.getHeight(), faceBox.getPoint(), System.currentTimeMillis(), statusMessage, Color.YELLOW);
//                        freshDisplayDrawFaceBoxs();
//                        freshDrawFps();
                        return;
                    }

                    long timeStart = System.currentTimeMillis();

                    //Detect face quality
                    float qualityScore;
                    synchronized (faceAssessment) {
                        qualityScore = faceAssessment.predictQuality(rotatedBitmap, faceBox);
                    }
                    //Filter low quality faces
                    if (qualityScore < QUALITY_THRES) {
//                        String statusMessage = "Low Quality";
//                        drawFaceBuffer[0].setFace(0, 1.0f, faceBox.getX(), faceBox.getY(), faceBox.getWidth(), faceBox.getHeight(), faceBox.getPoint(), System.currentTimeMillis(), statusMessage, Color.YELLOW);
//                        freshDisplayDrawFaceBoxs();
//                        freshDrawFps();
                        return;
                    }


                    if (RGBLIVE_FILTER) {
                        //Monocular RGB liveness detect
                        float livenessScore = 0.0f;
                        synchronized (faceRGBLiveness) {
                            livenessScore = faceRGBLiveness.liveness_check(rotatedBitmap, faceBox);
                        }

                        //double average_score = movingAverage.next(livenessScore);
                        if (livenessScore < RGBLIVE_THRESHOLD) {
                            //Non-living
                            livenessType = LivenessType.FAKE;
//                            String statusMessage = "Fake";
//                            drawFaceBuffer[0].setFace(0, 1.0f, faceBox.getX(), faceBox.getY(), faceBox.getWidth(), faceBox.getHeight(), faceBox.getPoint(), System.currentTimeMillis(), statusMessage, Color.RED);
//                            freshDisplayDrawFaceBoxs();
//                            freshDrawFps();
                            if (++fakeCounter > maxFakeCount) {
                                ToastUtil.showShort("请抬头");
                            } else {
//                                ToastUtil.showShort("人脸伪造未能识别");
                            }
                        } else {
                            //Living
                            livenessType = LivenessType.REAL;
                            fakeCounter = 0;
                            //clear moving average scores
                            movingAverage.clear();
                        }
                    }


                    if(livenessType == LivenessType.REAL) {
//                        String statusMessage = "Real";
//                        drawFaceBuffer[0].setFace(0, 1.0f, faceBox.getX(), faceBox.getY(), faceBox.getWidth(), faceBox.getHeight(), faceBox.getPoint(), System.currentTimeMillis(), statusMessage, Color.GREEN);
//                        freshDisplayDrawFaceBoxs();
//                        freshDrawFps();

                        if(null == allFaceFeaturesBuffer.getFeatures() || allFaceFeaturesBuffer.getFeatures().isEmpty()){
                            return;
                        }
                        //Living
                        byte[] feature;
                        synchronized (faceRecognize) {
                            feature = faceRecognize.extractFaceFeatureByImg(rotatedBitmap, faceBox);
                        }
                        SimiIndexPair maxSimiIndexPair = findMaxSimilarity(feature, allFaceFeaturesBuffer.getFeatures());
                        long durTime = System.currentTimeMillis() - timeStart;

                        LogUtils.e("相似度===" + maxSimiIndexPair.simi);
                        //Customer logic
                        if (maxSimiIndexPair.simi >= AppConfigManager.getInitedAppConfig().getFace_similar()) {
                            //feature id
                            String token = allFaceFeaturesBuffer.getToken().get(maxSimiIndexPair.index);
                            //Similarity
                            float simiScore = maxSimiIndexPair.simi;

                            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.TOKEN, token);


                            Map<String, String> map = new HashMap<>();
                            map.put("token", token);
                            new HttpsPresenter(LoginActivity_Face.this, LoginActivity_Face.this).request(map, Constant.FACELOG, false);

                            Common.openActivity(LoginActivity_Face.this, ChooseRefuseKindActivity.class);
                            stopTask();
                        } else {
                            //Stranger logic
//                            ToastUtil.showShort("未录入人脸库");
                        }
                    }

                    if (frameBitmap != null && !frameBitmap.isRecycled()) {
                        frameBitmap.recycle();
                        frameBitmap = null;
                    }

                } catch (Exception e) {

                }
            }


            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        //Get screen orientation
        int ScreenRotation = getWindowManager().getDefaultDisplay().getRotation();
        int cameraOrientation = 1;

        switch (ScreenRotation){
            case Surface.ROTATION_0:
                cameraOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_90:
                cameraOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            case Surface.ROTATION_180:
                cameraOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
            case Surface.ROTATION_270:
                cameraOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                break;
            default:
                cameraOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                break;
        }


        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(cameraWidth,cameraHeight))
                .rotation(cameraOrientation)
                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(cameraMirrow)
                .additionalRotation(cameraAdditionalRotate)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
    }



    /**
     * After the first layout of {@link #previewView}, remove the monitor and initialize the engine and camera
     */
    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        //初始化相机
        initCamera();
    }


    public void setBrightness(float f){
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = f;
//        pd=new ProgressDialog(this);
        getWindow().setAttributes(lp);
    }


    @OnClick(R.id.mLayout_Back)
    public void onClick() {
        stopTask();
    }

    @Override
    public void showResult(String status, String pRows, String url) {
        if (status.equals(ExceptionEngine._SUCCESS)) {
            if (url.equals(Constant.FACELIST)) {
                try {
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.LAST_REQUESTTIME, System.currentTimeMillis() / 1000 + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                List<byte[]> featureList = new ArrayList<byte[]>();
                List<String> idList = new ArrayList<>();
                List<FaceModel> mAllList = new ArrayList<>();
                if (!Common.empty(pRows)) {
                    mAllList.addAll(JSON.parseArray(pRows, FaceModel.class));
                }

                if (!Common.empty(MyApplication.getInstance().getFaceList())) {
                    mAllList.addAll(JSON.parseArray(MyApplication.getInstance().getFaceList(), FaceModel.class));
                }
                LogUtils.e("size=" + mAllList.size());
                MyApplication.getInstance().setFaceList(JSON.toJSONString(mAllList));

                for (FaceModel model : mAllList) {
                    byte[] feature = Base64.decode(model.getFeature(), Base64.DEFAULT);
                    idList.add(model.getToken());
                    featureList.add(feature);
                }
                allFaceFeaturesBuffer.setFeatures(featureList);
                allFaceFeaturesBuffer.setToken(idList);
            }
        }
    }

    private void stopTask() {
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
        unInitEngine();
        if (mRemainTime != null) mRemainTime.clearCurTimer();
        DestroyActivityUtil.destoryActivity(this.getClass().getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRemainTime != null)
            mRemainTime.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRemainTime != null)
            mRemainTime.resume();
    }
}

