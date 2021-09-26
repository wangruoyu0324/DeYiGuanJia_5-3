package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chuxinbuer.deyiguanjia.MyApplication;
import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.base.BaseActivity;
import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.database.AppConfigManager;
import com.chuxinbuer.deyiguanjia.database.AppConfigPB;
import com.chuxinbuer.deyiguanjia.dialog.MyAlertDialog;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.imagevideobanner.VideoPlayManeger;
import com.chuxinbuer.deyiguanjia.mvp.model.ConfigModel;
import com.chuxinbuer.deyiguanjia.mvp.presenter.HttpsPresenter;
import com.chuxinbuer.deyiguanjia.mvp.view.iface.IBaseView;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.chuxinbuer.deyiguanjia.utils.DestroyActivityUtil;
import com.chuxinbuer.deyiguanjia.utils.LogUtils;
import com.chuxinbuer.deyiguanjia.utils.SPUtil;
import com.chuxinbuer.deyiguanjia.utils.ShellUtils;
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;
import com.king.app.updater.AppUpdater;
import com.king.app.updater.callback.UpdateCallback;
import com.sunchen.netbus.NetStatusBus;
import com.sunchen.netbus.annotation.NetSubscribe;
import com.sunchen.netbus.type.Mode;
import com.sunchen.netbus.type.NetType;
import com.sunchen.netbus.utils.Constrants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends BaseActivity implements IBaseView {

    private long mExitTime = 0;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public static final String TAG = LoginActivity.class.getSimpleName();

    static {
        System.loadLibrary("dfacepro");
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_login;
    }

    @Override
    protected void init() {
        if (Common.empty(SPUtil.getInstance().getString(Constant.DEVICE_TOKEN))) {
            MyAlertDialog myAlertDialog = new MyAlertDialog(this).builder();
            myAlertDialog.setCancelable(false).setCanceledOnTouchOutside(false).setTitle("绑定设备").setMsg("请将设备唯一标识码与小区信息进行后台绑定，设备唯一标识码为：" + AppConfigManager.getInitedAppConfig().getDevice_token()).setPositiveButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SPUtil.getInstance().put(Constant.DEVICE_TOKEN, AppConfigManager.getInitedAppConfig().getDevice_token());

                    Map<String, String> map3 = new HashMap<>();
                    map3.put("deviceno", AppConfigManager.getInitedAppConfig().getDevice_token());
                    new HttpsPresenter(LoginActivity.this, LoginActivity.this).request(map3, Constant.GETDEVICEINFO, false);
                }
            }).show();
        }
        if (hasPermission()) {
            mkdirs();
            //The production environment only needs to be copied once
            copyAssertToSdCard("model");
        } else {
            requestPermission();
        }

        SPUtil.getInstance().put(Constant.IS_DOWNLOADING, false);

        try {
            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.LAST_REQUESTTIME, "0");
            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.LAST_REQUESTTIME_MANAGER, "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> map = new HashMap<>();
        map.put("deviceno", AppConfigManager.getInitedAppConfig().getDevice_token());
        map.put("type", "1");
        map.put("start", AppConfigManager.getInitedAppConfig().getLast_requesttime());
        new HttpsPresenter(this, this).request(map, Constant.FACELIST, "facelist", false);

        Map<String, String> map2= new HashMap<>();
        map2.put("deviceno", AppConfigManager.getInitedAppConfig().getDevice_token());
        map2.put("type", "2");
        map2.put("start", AppConfigManager.getInitedAppConfig().getLast_requesttime_manager());
        new HttpsPresenter(this, this).request(map2, Constant.FACELIST, "facelist_manager", false);
        try {
            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.LAST_REQUESTTIME, System.currentTimeMillis() / 1000 + "");
            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.LAST_REQUESTTIME_MANAGER, System.currentTimeMillis() / 1000 + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            VideoPlayManeger.eliminateEvent();//设置手指离开屏幕的时间
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoPlayManeger.startMonitor();//重新发送和设置最后一次点击的时间
        VideoPlayManeger.eliminateEvent();
    }

    @AfterPermissionGranted(10000)
    private void requireSomePermission() {
        String[] perms = {
                // 把你想要申请的权限放进这里就行，注意用逗号隔开
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (EasyPermissions.hasPermissions(this, perms)) {
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "需要获取以下权限方能继续使用",
                    10000, perms);
        }
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            ToastUtil.showShort(R.string.tip_extiapp);
            mExitTime = System.currentTimeMillis();
        } else {
            DestroyActivityUtil.exitApp();
        }
    }


    @Override
    protected void initBundleData() {
        ToastUtil.showLong(Common.getVersionCode(this));
    }

    @OnClick({R.id.mLayout_Manager, R.id.mLayout_ScanFace, R.id.mLayout_ScanQrcode, R.id.mLayout_Password})
    public void onClick(View view) {
        if (Common.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.mLayout_Manager:
                Common.openActivity(this, ChooseLoginActivity_Manager.class);
                break;
            case R.id.mLayout_ScanFace:
                Common.openActivity(this, LoginActivity_Face.class);
                break;
            case R.id.mLayout_ScanQrcode:
                Common.openActivity(this, LoginActivity_Scan.class);
                break;
            case R.id.mLayout_Password:
                Common.openActivity(this, LoginActivity_Password.class);
                break;
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) ||
//                    shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
//                Toast.makeText(MainActivity.this,
//                        "Camera AND storage permission are required for this demo", Toast.LENGTH_LONG).show();
//            }
//            requestPermissions(new String[] {PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
//        }
//        requestPermissions(new String[] {PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
        String[] perms = {
                // 把你想要申请的权限放进这里就行，注意用逗号隔开
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, perms, PERMISSIONS_REQUEST);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mkdirs();
                //Copy the normal_binary folder of the assert to the dface directory of the sd card
                copyAssertToSdCard("normal_binary");
            } else {
                requestPermission();
            }
        }
    }

    /**
     * Copy the model to the dface directory of the SD card (only copy files that did not originally exist)
     */
    private void copyFile(String filename) {
        File sdDir = Environment.getExternalStorageDirectory();//Get root directory
        File file = new File(sdDir.toString() + "/dface/");
        if (!file.exists()) {
            file.mkdir();
        }

        AssetManager assetManager = this.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = file.toString() + "/" + filename;
            out = new FileOutputStream(newFileName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }


    private void mkdirs() {
        File sdDir = Environment.getExternalStorageDirectory();//Get SD root directory
        File file = new File(sdDir.toString() + "/dface/");
        if (!file.exists()) {
            file.mkdir();
        }

        //Create faceimg directory
        File fileImg = new File(sdDir.toString() + "/dface/faceimg");
        if (!fileImg.exists()) {
            fileImg.mkdir();
        }

        //Create a log directory
        File fileLog = new File(sdDir.toString() + "/dface/logs");
        if (!fileLog.exists()) {
            fileLog.mkdir();
        }

        //Create database db directory
        File fileDB = new File(sdDir.toString() + "/dface/db");
        if (!fileDB.exists()) {
            fileDB.mkdir();
        }
    }


    /**
     * Copy the assert directory to the SD card
     */
    private void copyAssertToSdCard(String path) {
        File sdDir = Environment.getExternalStorageDirectory();//Get SD root directory
        File file = new File(sdDir.toString() + "/dface/");
        if (!file.exists()) {
            file.mkdir();
        }

        //Copy the model files in the assert/normal_binary directory to the dface/normal_binary of the sd card
        AssetManager assetManager = this.getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = file.toString() + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyAssertToSdCard(path + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "I/O Exception");
        }
    }

    @Override
    public void showResult(String status, String pRows, String url) {
        if (status.equals(ExceptionEngine._SUCCESS)) {
            if (url.equals(Constant.UPDATE_APK)) {
                if (!Common.empty(pRows)) {
                    JSONObject jsonObject = JSON.parseObject(pRows);
                    if (jsonObject.containsKey("version")) {
                        if (!jsonObject.getString("version").equals(Common.getVersionCode(this))) {
                            //一句代码，傻瓜式更新
                            new AppUpdater(LoginActivity.this, jsonObject.getString("package")).setUpdateCallback(new UpdateCallback() {
                                @Override
                                public void onDownloading(boolean isDownloading) {
                                    SPUtil.getInstance().put(Constant.IS_DOWNLOADING, isDownloading);
                                }

                                @Override
                                public void onStart(String url) {

                                }

                                @Override
                                public void onProgress(long progress, long total, boolean isChange) {
                                    LogUtils.e("progress:" + Common.formatDouble3(progress * 100 / (float) total) + "%");
                                }

                                @Override
                                public void onFinish(File file) {
                                    SPUtil.getInstance().put(Constant.IS_DOWNLOADING, false);
//                                    excuteSuCMD(file.getAbsolutePath());
//                                    installSilent("com.chuxinbuer.deyiguanjia", file.getAbsolutePath());
                                    ShellUtils.execCommand("pm install -r -i com.chuxinbuer.deyiguanjia " + file.getAbsolutePath(), true);
                                }

                                @Override
                                public void onError(Exception e) {

                                }

                                @Override
                                public void onCancel() {

                                }
                            }).start();
                        }
                    }
                }
            } else if (url.equals(Constant.LOGIN_SCAN)) {
                isGetResult = false;
                int login_type = 1;
                if (!Common.empty(pRows)) {
                    JSONObject jsonObject = JSON.parseObject(pRows);
                    if (jsonObject.containsKey("token")) {
                        try {
                            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.TOKEN, jsonObject.get("token"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (jsonObject.containsKey("login_type")) {
                        login_type = jsonObject.getIntValue("login_type");
                    }
                }
                if (login_type == 1) {
                    Common.openActivity(LoginActivity.this, ChooseRefuseKindActivity.class);
                } else if (login_type == 2) {
                    Common.openActivity(LoginActivity.this, ChooseRefuseKindActivity_Manager.class);
                }
            } else if (url.equals(Constant.GETCONFATTR)) {
                if (!Common.empty(pRows)) {
                    List<ConfigModel> list = JSON.parseArray(pRows, ConfigModel.class);
                    for (ConfigModel model : list) {
                        if (model.getName().equals("voiceon")) {//设置声音
                            JSONObject jsonObject = JSON.parseObject(model.getValue());
                            if (jsonObject.containsKey("end")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VIDEOTIME_END, jsonObject.getLongValue("end"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("start")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VIDEOTIME_START, jsonObject.getLongValue("start"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (model.getName().equals("close_time")) {//设置关门超时时间
                            try {
                                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.OVERTIME, Integer.parseInt(model.getValue()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (model.getName().equals("voice_video")) {//设置 视频音量
                            try {
                                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VOICE_VIDEO, Float.parseFloat(model.getValue()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (model.getName().equals("voice_breadcost")) {//设置 语音播报音量
                            try {
                                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VOICE_BREADCOST, Float.parseFloat(model.getValue()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (model.getName().equals("video_interval")) {//视频播放间隔(秒)
                            try {
                                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VIDEO_INTERVAL, Integer.parseInt(model.getValue()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (model.getName().equals("cate_background")) {//积分兑换比例背景图
                            try {
                                if (!Common.empty(model.getValue())) {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.CATE_BACKGROUND, model.getValue());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (model.getName().equals("face_similar")) {//人脸相似度
                            try {
                                if (!Common.empty(model.getValue())) {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.FACE_SIMILAR, Float.parseFloat(model.getValue()));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (model.getName().equals("cate_full")) {//获取垃圾箱报警上限
                            JSONObject jsonObject = JSON.parseObject(model.getValue());
                            if (jsonObject.containsKey("box_c1")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_FANGZHI, jsonObject.getFloatValue("box_c1"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c2")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_BOLI, jsonObject.getFloatValue("box_c2"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c3")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_ZHIZHANG1, jsonObject.getFloatValue("box_c3"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c31")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_ZHIZHANG2, jsonObject.getFloatValue("box_c31"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c4")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_JINSHU, jsonObject.getFloatValue("box_c4"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c5")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_SULIAO, jsonObject.getFloatValue("box_c5"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c6")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_CHUYU1, jsonObject.getFloatValue("box_c6"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c61")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_CHUYU2, jsonObject.getFloatValue("box_c61"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c7")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_DUHAI, jsonObject.getFloatValue("box_c7"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c8")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_OTHER1, jsonObject.getFloatValue("box_c8"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("box_c81")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.WEIGHTLIMIT_OTHER2, jsonObject.getFloatValue("box_c81"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (model.getName().equals("cate_rate")) {//获取积分兑换比例
                            JSONObject jsonObject = JSON.parseObject(model.getValue());
                            if (jsonObject.containsKey("c1")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.RATE_FANGZHI, jsonObject.getFloatValue("c1"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("c2")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.RATE_BOLI, jsonObject.getFloatValue("c2"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("c3")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.RATE_ZHIZHANG, jsonObject.getFloatValue("c3"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("c4")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.RATE_JINSHU, jsonObject.getFloatValue("c4"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("c5")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.RATE_SULIAO, jsonObject.getFloatValue("c5"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("c6")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.RATE_CHUYU, jsonObject.getFloatValue("c6"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("c7")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.RATE_DUHAI, jsonObject.getFloatValue("c7"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonObject.containsKey("c8")) {
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.RATE_OTHER, jsonObject.getFloatValue("c8"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } else if (url.equals("facelist")) {
                if (!Common.empty(pRows)) {
                    MyApplication.getInstance().setFaceList(pRows);
                }
            } else if (url.equals("facelist_manager")) {
                if (!Common.empty(pRows)) {
                    MyApplication.getInstance().setFaceList_Manager(pRows);
                }
            }
        } else {
            if (url.equals(Constant.LOGIN_SCAN)) {
                ToastUtil.showLong("扫码失败，请重试！");
                isGetResult = false;
            }
        }
    }

    String barcode = "";
    private boolean isGetResult = false;
    private String str222 = "";

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!isGetResult) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                char pressedKey = (char) event.getUnicodeChar();
                barcode += pressedKey;
            }
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                isGetResult = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String str = URLEncoder.encode(barcode, "gb2312");
                            Map<String, String> map = new HashMap<>();
                            str222 = str.replace("%00", "").replace("%0A", "");
                            map.put("code", str222);
                            LogUtils.e("----------------------------------" + str222);
                            new HttpsPresenter(LoginActivity.this, LoginActivity.this).request(map, Constant.LOGIN_SCAN);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        barcode = "";
                    }
                });
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /*
　　@pararm apkPath //cmd ="pm install -r "+apk存储路径
**/
    private int excuteSuCMD(String cmd) {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("chmod 777 " + cmd);
            PrintWriter
                    .println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
            PrintWriter.println("pm install -r " + cmd);
            LogUtils.e("path=" + cmd);
            // PrintWriter.println("exit");
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            LogUtils.e("File.toString()====value=静默安装返回值===" + value);
            return (Integer) value;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }


    /**
     * 静默安装
     *
     * @param packageName 　 　调用installSilent函数的应用包名
     * @param filePath    　　　　静默安装应用的apk路径
     * @return 0 安装成功
     * 　　　　　　　 1 文件不存在
     * 　　　　　　　 2 安装失败
     */
    public static int installSilent(String packageName, String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || file == null || file.length() <= 0 || !file.exists() || !file.isFile()) {
            return 1;
        }
        //pm install -i 包名 --user 0 apkpath
        String[] args = {"pm", "install", "-i", packageName, "--user", "0", "-r", filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
                LogUtils.e("installSilent while successMsg s:" + s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
                LogUtils.e("installSilent while errorMsg s:" + s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        LogUtils.e("installSilent successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        NetStatusBus.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        NetStatusBus.getInstance().unregister(this);
    }

    //所有网络变化都会被调用，可以通过 NetType 来判断当前网络具体状态
    @NetSubscribe(mode = Mode.AUTO)
    public void netChange(NetType netType) {
        Log.e(Constrants.LOG_TAG, netType.name());
        if (!netType.name().equals(NetType.NONE)) {
            Map<String, String> map2 = new HashMap<>();
            map2.put("group", "5");
            map2.put("deviceno", AppConfigManager.getInitedAppConfig().getDevice_token());
            new HttpsPresenter(this, this).request(map2, Constant.UPDATE_APK, false);


            Map<String, String> map = new HashMap<>();
            map.put("deviceno", AppConfigManager.getInitedAppConfig().getDevice_token());
            new HttpsPresenter(this, this).request(map, Constant.GETCONFATTR, false);
        }
    }
}

