package com.chuxinbuer.deyiguanjia.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * Created by wry on 2018-03-09 14:30
 */

public class NetBroadcastReceiver extends BroadcastReceiver {

//    public NetEvevt evevt2 = HeadActivity.evevt;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        // 如果相等的话就说明网络状态发生了变化
//        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
//            int netWorkState = NetUtil.getNetWorkState(context);
//            // 接口回调传过去状态的类型
//            if (evevt2 != null) {
//                evevt2.onNetChange(netWorkState);
//            }
//        }
    }

    // 自定义接口
    public interface NetEvevt {
        public void onNetChange(int netMobile);
    }
}
