package com.chuxinbuer.deyiguanjia;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.serialport.SerialPortFinder;
import android.support.annotation.RequiresApi;
import android.support.multidex.MultiDex;

import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.database.AppConfigManager;
import com.chuxinbuer.deyiguanjia.database.AppConfigPB;
import com.chuxinbuer.deyiguanjia.database.UpdateManager;
import com.chuxinbuer.deyiguanjia.fresco.ImagePipelineConfigFactory;
import com.chuxinbuer.deyiguanjia.imagevideobanner.VideoPlayManeger;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.chuxinbuer.deyiguanjia.utils.DeviceUtils;
import com.chuxinbuer.deyiguanjia.utils.SPUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.sunchen.netbus.NetStatusBus;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wry
 * @time 2016.10.10 15:15
 */

public class MyApplication extends Application {

    private final String TAG = "MyApplication";

    public static Context mContext;

    public static AppConfigPB appConfigPB;
    // 单例模式
    private static MyApplication instance;

    /**
     * 单例模式中获取唯一的MyApp实例
     *
     * @return
     */
    public static MyApplication getInstance() {
        if (null == instance) {
            instance = new MyApplication();
        }
        return instance;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate() {
        mContext = getApplicationContext();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        new Thread(new Runnable() {
            @Override
            public void run() {
                appConfigPB = AppConfigManager.getInitedAppConfig();
                try {
                    appConfigPB.loadFromPref();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                UpdateManager.newUpdateManager();
            }
        }).start();
        checkFile();

        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfigFactory.getOkHttpImagePipelineConfig(mContext);
        Fresco.initialize(this, imagePipelineConfig);

        Bugly.init(getApplicationContext(), "213326339a", false);


        SerialPortFinder finder = new SerialPortFinder();
        String[] path = finder.getAllDevicesPath();
        for (String s : path) {
            if (s.equals("/dev/ttyS4")) {
                SPUtil.getInstance().put(Constant.SERIAL_PORT_LOCK, s);
            } else if (s.equals("/dev/ttyS0")) {
                SPUtil.getInstance().put(Constant.SERIAL_PORT_WEIGHT, s);
            }
        }
        SPUtil.getInstance().put(Constant.BAUD_RATE, "9600");
        SPUtil.getInstance().put(Constant.CHECK_DIGIT, "0");
        SPUtil.getInstance().put(Constant.DATA_BITS, "8");
        SPUtil.getInstance().put(Constant.STOP_BIT, "1");


        if (Common.empty(SPUtil.getInstance().getString(Constant.DEVICE_TOKEN))) {
            SPUtil.getInstance().put(Constant.DEVICE_TOKEN, "");

            SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG1, 0f);
            SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG2, 0f);
            SPUtil.getInstance().put(Constant.WEIGHT_FANGZHI, 0f);
            SPUtil.getInstance().put(Constant.WEIGHT_BOLI, 0f);
//            SPUtil.getInstance().put(Constant.WEIGHT_SULIAO, 0f);
            SPUtil.getInstance().put(Constant.WEIGHT_JINSHU, 0f);


            SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG1_CUR, 0f);
            SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG2_CUR, 0f);
            SPUtil.getInstance().put(Constant.WEIGHT_FANGZHI_CUR, 0f);
            SPUtil.getInstance().put(Constant.WEIGHT_BOLI_CUR, 0f);
//            SPUtil.getInstance().put(Constant.WEIGHT_SULIAO_CUR, 0f);
            SPUtil.getInstance().put(Constant.WEIGHT_JINSHU_CUR, 0f);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取保存在sd中的 设备唯一标识符
//                    String readDeviceID =getIMEI();
                    String readDeviceID = DeviceUtils.getUniquePsuedoDeviceID();
                    appConfigPB.updatePrefer(AppConfigPB.DEVICE_TOKEN, readDeviceID);
                    CrashReport.setUserId(readDeviceID);  //该用户本次启动后的异常日志用户ID都将是9527

                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION, 0);
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VIDEO_POSITION, 0);
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION_CUR, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }).start();

        //待机视频
        VideoPlayManeger.init(this);
        NetStatusBus.getInstance().init(this);
        super.onCreate();
    }



    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void checkFile() {
        List<File> filelist = new ArrayList<File>();
        File picpath = new File(Constant.STORE_PATH);//用于存放相机拍照的临时图片
        File picpath2 = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + MyApplication.mContext.getPackageName() + "/resource/");
        //用于存放下载的安装包或者图片文件
        filelist.add(picpath);
        filelist.add(picpath2);
        for (File file : filelist) {
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    private String faceList = "";

    public void setFaceList(String str) {
        faceList = str;
    }

    public String getFaceList() {
        return faceList;
    }

    private String faceList_manager = "";

    public void setFaceList_Manager(String str) {
        faceList_manager = str;
    }

    public String getFaceList_Manager() {
        return faceList_manager;
    }
}