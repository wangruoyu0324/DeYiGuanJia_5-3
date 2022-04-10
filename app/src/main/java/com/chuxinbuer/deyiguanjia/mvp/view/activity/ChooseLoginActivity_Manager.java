package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.view.KeyEvent;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.base.BaseActivity;
import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.database.AppConfigManager;
import com.chuxinbuer.deyiguanjia.database.AppConfigPB;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.mvp.presenter.HttpsPresenter;
import com.chuxinbuer.deyiguanjia.mvp.view.iface.IBaseView;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.chuxinbuer.deyiguanjia.utils.DestroyActivityUtil;
import com.chuxinbuer.deyiguanjia.utils.LogUtils;
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;
import com.chuxinbuer.deyiguanjia.widget.CountDownButton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class ChooseLoginActivity_Manager extends BaseActivity implements IBaseView {

    @BindView(R.id.mRemainTime)
    CountDownButton mRemainTime;

    public static final String TAG = ChooseLoginActivity_Manager.class.getSimpleName();

    @Override
    protected int getContentViewId() {
        return R.layout.activity_chooselogin_manager;
    }

    @Override
    protected void init() {
        mRemainTime.setLength(60 * 1000);
        mRemainTime.start();
        mRemainTime.setOnFinishTimeClick(new CountDownButton.OnFinishTimeClick() {
            @Override
            public void onFinishTimeClick() {
                stopTask();
            }
        });
    }

    private void stopTask() {
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

    @Override
    protected void initBundleData() {

    }

    @OnClick({R.id.mLayout_ScanFace, R.id.mLayout_Back, R.id.mLayout_ScanQrcode, R.id.mLayout_Password})
    public void onClick(View view) {
        if (Common.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.mLayout_Back:
                stopTask();
                break;
            case R.id.mLayout_ScanFace:
                Common.openActivity(this, LoginActivity_Face_Manager.class);
                break;
            case R.id.mLayout_ScanQrcode:
                break;
            case R.id.mLayout_Password:
                Common.openActivity(this, LoginActivity_Manager.class);
                break;
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
                            new HttpsPresenter(ChooseLoginActivity_Manager.this, ChooseLoginActivity_Manager.this).request(map, Constant.LOGIN_SCAN);
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
            if (login_type == 2) {
                Common.openActivity(ChooseLoginActivity_Manager.this, ChooseRefuseKindActivity_Manager.class);
            }
            stopTask();
        } else {
            if (url.equals(Constant.LOGIN_SCAN)) {
                ToastUtil.showLong("扫码失败，请重试！");
                isGetResult = false;
            }
        }
    }
}

