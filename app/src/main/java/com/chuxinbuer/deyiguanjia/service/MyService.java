package com.chuxinbuer.deyiguanjia.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.chuxinbuer.deyiguanjia.utils.LogUtils;

import org.apache.commons.lang.StringUtils;

public class MyService extends Service {
    // 心跳间隔
    // 以前为final static的，但是服务器要求修改间隔时间，则去掉
    //心跳间隔时间 默认毫秒
    public static final int DEFAULT_INTERVAL = 3 * 60 * 1000;
    //心跳间隔时间 默认毫秒
    public static int HEART_SPE = DEFAULT_INTERVAL;
    public static final String ACTION_ALARM_HEARTBEAT = "ACTION_ALARM_HEARTBEAT";

    private PendingIntent heartbeatPi = null;
    private AlarmManager heartbeatAlarm;

    private int shark = 0;

    private ScreenControlAlarmReceiver screenControlAlarmReceiver;

    /**
     * 初始化窗口
     */
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("### N1GHeartService onCreate");
        initData();
        initReceiver();
    }

    private void initData() {
        heartbeatAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    /**
     * 初始化接收对象
     */
    private void initReceiver() {
        screenControlAlarmReceiver = new ScreenControlAlarmReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ALARM_HEARTBEAT);
        registerReceiver(screenControlAlarmReceiver, intentFilter);
    }

    /**
     * 释放资源
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenControlAlarmReceiver);
        endTimer();
    }

    // 每次启动Servcie时都会调用该方法
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("--onStartCommand()--");
        shark = intent.getIntExtra("SHARK", DEFAULT_INTERVAL);
        startTimer(shark * 1000 + "");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 更新心跳间隔，并启动心跳
     *
     * @param timeString 心跳间隔  单位：毫秒
     */
    private void startTimer(String timeString) {

        HEART_SPE = DEFAULT_INTERVAL;
        int tempTime = 0;
        if (StringUtils.isEmpty(timeString) || "null".equals(timeString)) {
            //如果登录认证就失败了，也需要启动timer
            tempTime = HEART_SPE;
        } else {
            try {
                tempTime = Integer.parseInt(timeString);
            } catch (NumberFormatException e) {
                tempTime = HEART_SPE;
                e.printStackTrace();
            }
        }

        if (tempTime == HEART_SPE && heartbeatPi != null) {
            //间隔时间和原来一致,并且启动过timer，不重启timer
            return;
        } else {
            //间隔时间和原来不一致，重启timer
            HEART_SPE = tempTime;
            endTimer();

            Intent alarmIntent = new Intent(ACTION_ALARM_HEARTBEAT);
            heartbeatPi = PendingIntent.getBroadcast(MyService.this, 0, alarmIntent, 0);//通过广播接收
            heartbeatAlarm.setRepeating(AlarmManager.RTC, 0, HEART_SPE, heartbeatPi);//INTERVAL毫秒后触发
            LogUtils.e("心跳间隔:" + HEART_SPE + "");
            heartBeat();

        }
    }

    public class ScreenControlAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //你的逻辑处理
            //如果需要实现间隔定时器功能，在重新执行1的发送步骤，实现间隔定时，间隔时间为INTERVAL
            heartBeat();
        }
    }

    /**
     * 停止心跳
     */
    private void endTimer() {
        if (heartbeatAlarm != null && heartbeatPi != null) {
            heartbeatAlarm.cancel(heartbeatPi);
            heartbeatPi = null;
        }
    }

    /**
     * 心跳上传数据
     */
    private void heartBeat() {
    }
}
