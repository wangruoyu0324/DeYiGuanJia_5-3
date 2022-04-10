package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.view.KeyEvent;

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
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity_Scan extends BaseActivity implements IBaseView {
    @BindView(R.id.mRemainTime)
    CountDownButton mRemainTime;
    @BindView(R.id.mQrcode)
    SimpleDraweeView mQrcode;

    private String str222 = "";

    @Override
    protected int getContentViewId() {
        return R.layout.activity_login_scan;
    }

    @Override
    protected void init() {
        //增加结束符$201003-881D【增加结束符 CRLF(0x0D,0x0A)】
        mRemainTime.start();
        mRemainTime.setOnFinishTimeClick(new CountDownButton.OnFinishTimeClick() {
            @Override
            public void onFinishTimeClick() {
                if (Common.isTopActivity(LoginActivity_Scan.this, "com.chuxinbuer.deyiguanjia.mvp.view.activity.LoginActivity_Scan")) {
                    Common.openActivity(LoginActivity_Scan.this, LoginActivity.class);
                }
                stopTask();
            }
        });
    }

    @Override
    protected void initBundleData() {
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


    @OnClick(R.id.mLayout_Back)
    public void onClick() {
        stopTask();
    }


    String barcode = "";
    private boolean isGetResult = false;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!isGetResult) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                char pressedKey = (char) event.getUnicodeChar();
                barcode += pressedKey;
                LogUtils.e("------" + barcode);
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
                            new HttpsPresenter(LoginActivity_Scan.this, LoginActivity_Scan.this).request(map, Constant.LOGIN_SCAN);
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
            if (!Common.empty(pRows)) {
                JSONObject jsonObject = JSON.parseObject(pRows);
                if (jsonObject.containsKey("token")) {
                    try {
                        AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.TOKEN, jsonObject.get("token"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            Common.openActivity(LoginActivity_Scan.this, ChooseRefuseKindActivity.class);
            stopTask();
        } else {
            if (url.equals(Constant.LOGIN_SCAN)) {
                ToastUtil.showLong("扫码失败，请重试！");
                isGetResult = false;
            }
        }
    }
}

