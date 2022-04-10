package com.chuxinbuer.deyiguanjia.imagevideobanner;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.view.KeyEvent;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.base.BaseActivity;
import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.database.AppConfigManager;
import com.chuxinbuer.deyiguanjia.database.AppConfigPB;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.imagevideobanner.banner.ImageVideoBanner;
import com.chuxinbuer.deyiguanjia.imagevideobanner.bean.BannerBean;
import com.chuxinbuer.deyiguanjia.mvp.model.EventMessage;
import com.chuxinbuer.deyiguanjia.mvp.presenter.HttpsPresenter;
import com.chuxinbuer.deyiguanjia.mvp.view.activity.ChooseRefuseKindActivity;
import com.chuxinbuer.deyiguanjia.mvp.view.activity.ChooseRefuseKindActivity_Manager;
import com.chuxinbuer.deyiguanjia.mvp.view.iface.IBaseView;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.chuxinbuer.deyiguanjia.utils.DestroyActivityUtil;
import com.chuxinbuer.deyiguanjia.utils.LogUtils;
import com.chuxinbuer.deyiguanjia.utils.MediaFile;
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class PlayBannerActivity extends BaseActivity implements IBaseView {
    @BindView(R.id.banner)
    ImageVideoBanner banner;
    private List<BannerBean> list = new ArrayList<>();

    private AudioManager mAudioManager;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_playbanner;
    }

    @Override
    protected void init() {
        mAudioManager = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public AudioManager getAudioManager(){
        return mAudioManager;
    }

    @Override
    protected void initBundleData() {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        EventBus.getDefault().register(this);
        list = new ArrayList<>();
        if (!Common.empty(getIntent().getSerializableExtra("list"))) {
            File[] fileList = (File[]) getIntent().getSerializableExtra("list");
            for (int i = 0; i < fileList.length; i++) {
                if (!fileList[i].isDirectory()) {
                    BannerBean bannerBean = new BannerBean();
                    bannerBean.setUrl(fileList[i].getAbsolutePath());
                    if (MediaFile.isVideoFileType(fileList[i].getAbsolutePath())) {
                        bannerBean.setType(1);
                        list.add(bannerBean);
                    } else if (MediaFile.isImageFileType(fileList[i].getAbsolutePath())) {
                        bannerBean.setType(0);
                        list.add(bannerBean);
                    } else {

                    }
                }
            }
        }
        if (!Common.empty(AppConfigManager.getInitedAppConfig().getBannerposition()) && list.size() > 0) {
            int position = AppConfigManager.getInitedAppConfig().getBannerposition() % list.size();
            if (position < list.size()) {
                List<BannerBean> newList = new ArrayList<>();
                for (int i = position; i < list.size(); i++) {
                    newList.add(list.get(i));
                }
                for (int i = 0; i < position; i++) {
                    newList.add(list.get(i));
                }
                list.clear();
                list.addAll(newList);
            }
        }
        LogUtils.e("----------" + JSON.toJSONString(list));

        banner.replaceData(list);
        banner.startBanner();
    }


    @Override
    protected void onPause() {
        super.onPause();

        DestroyActivityUtil.destoryActivity(this.getClass().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (banner != null) {
            banner.removeHandler();
            banner.stopBanner();
        }
        EventBus.getDefault().unregister(this);

//        VideoPlayManeger.startMonitor();//重新发送和设置最后一次点击的时间
//        VideoPlayManeger.eliminateEvent();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(EventMessage message) {
        if (message.getType().equals("video")) {
            PlayBannerActivity.this.finish();
        } else if (message.getType().equals("image")) {
            PlayBannerActivity.this.finish();
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
                            new HttpsPresenter(PlayBannerActivity.this, PlayBannerActivity.this).request(map, Constant.LOGIN_SCAN);
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

    @Override
    public void showResult(String status, String pRows, String url) {
        if (status.equals(ExceptionEngine._SUCCESS)) {
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
                Common.openActivity(PlayBannerActivity.this, ChooseRefuseKindActivity.class);
            } else if (login_type == 2) {
                Common.openActivity(PlayBannerActivity.this, ChooseRefuseKindActivity_Manager.class);
            }
            finish();
        } else {
            if (url.equals(Constant.LOGIN_SCAN)) {
                ToastUtil.showLong("扫码失败，请重试！");
                isGetResult = false;
            }
        }
    }
}
