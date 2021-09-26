package com.chuxinbuer.deyiguanjia.face;

import com.dface.dto.DetectMode;

public class AppConfig {
    //Recognition threshold
    public static float CONFIDENCE = 0.75f;

    //RGBLiveness threshold
    public static float RGB_LIVENESS_THRES = 0.70f;

    //RGBLiveness level
    public static int RGB_LIVENESS_LEVEL = 2;

    //Detect min size
    public static int DETECT_MIN_SIZE = 80;

    //Detect precision
    public static int DETECT_PRECISION = DetectMode.Precision_Low.getMode();

    //Recognize min size
    public static int RECOGNIZE_MIN_SIZE = 80;

    //Quality threshold
    public static float QUALITY_THRES = 0.45f;

    //Enable RGB-Liveness check filter
    public static boolean ENABLE_RGBLIVENESS_FILTER = false;

    //Whether to enable face quality(blur) filtering
    public static boolean ENABLE_QUALITY_FILTER = true;

    //camera resolution(width)
    public static int CAMERA_WIDTH = 640;

    //camera resolution(height)
    public static int CAMERA_HEIGHT = 480;

    //Color camera
    public static int COLOR_CAMERAID = 1;

    //NIR camera
    public static int IR_CAMERAID = -1;

    //Additional camera rotation angle
    public static int CAMERA_ADDITIONAL_ROTATE = 0;

    //Color camera flip horizontally
    public static boolean CAMERA_IS_MIRROW = false;

    //NIR camera flip horizontally
    public static boolean NIR_MIRROW = false;

}
