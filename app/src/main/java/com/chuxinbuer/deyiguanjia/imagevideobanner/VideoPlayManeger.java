
package com.chuxinbuer.deyiguanjia.imagevideobanner;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.chuxinbuer.deyiguanjia.MyApplication;
import com.chuxinbuer.deyiguanjia.database.AppConfigManager;
import com.chuxinbuer.deyiguanjia.database.AppConfigPB;
import com.chuxinbuer.deyiguanjia.utils.Common;

import java.io.File;
import java.util.List;

/**
 * Created by hzy on 2019/1/14
 **/
public class VideoPlayManeger {
    private static final String TAG = "VideoPlayManeger";

    private static Context mContext;
    private static long lastTime = 0;//最后一次点击的时间

    public static final int SEND_TIME = 5 * 1000;//每秒钟发送一次信息

    public static void init(Context context) {
        mContext = context;
    }

    public static void startMonitor() {
        handler.sendEmptyMessageDelayed(0, SEND_TIME);//隔一分钟发送message
    }

    /**
     * 设置当前时间
     */
    public static void eliminateEvent() {
        lastTime = System.currentTimeMillis();
        Log.e(TAG, "lastTime = " + lastTime);
    }

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            long idleTime = System.currentTimeMillis() - lastTime;//闲置时间：当前的时间减去最后一次触发时间
            Log.i(TAG, "now = " + System.currentTimeMillis());
            Log.i(TAG, " idleTime = " + idleTime);
            if (idleTime >= AppConfigManager.getInitedAppConfig().getVideo_interval()*1000) {
                if(isForeground(mContext)){
                    if (Common.isTopActivity(mContext, "com.chuxinbuer.deyiguanjia.mvp.view.activity.LoginActivity")) {
                        goPlayActivity(); //跳转到视频播放页面
                    }
                }
            } else {
                handler.sendEmptyMessageDelayed(0, SEND_TIME);
            }
        }
    };

    private static boolean isForeground(Context context) {
        if (context != null) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo: processes) {
                if (processInfo.processName.equals(context.getPackageName())) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void goPlayActivity() {
//        if (!Common.empty(AppConfigManager.getInitedAppConfig().getUsbpath())) {
//            File rootFile = new File(AppConfigManager.getInitedAppConfig().getUsbpath());
//            if (rootFile.listFiles() != null) {
//                Intent intent = new Intent(mContext, PlayBannerActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mContext.startActivity(intent);
//            }
//        }else {
//            try {
//                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION, 0);
//                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VIDEO_POSITION, 0);
//                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION_CUR, 0);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        File[] fileList = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + MyApplication.mContext.getPackageName() + "/resource/").listFiles();
        if (fileList != null && fileList.length > 0) {
            Intent intent = new Intent(mContext, PlayBannerActivity.class);
            intent.putExtra("list",fileList);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }else {
            try {
                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION, 0);
                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VIDEO_POSITION, 0);
                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION_CUR, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
