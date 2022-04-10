package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;
import com.chuxinbuer.deyiguanjia.widget.CountDownButton;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity_Password extends BaseActivity implements IBaseView {
    @BindView(R.id.mRemainTime)
    CountDownButton mRemainTime;
    @BindView(R.id.et_Phone)
    EditText et_Phone;
    @BindView(R.id.et_Password)
    EditText et_Password;
    @BindView(R.id.mLayout_Check_Password)
    RelativeLayout mLayout_Check_Password;
    @BindView(R.id.mImage)
    ImageView mImage;

    private HttpsPresenter mHttpsPresenter;
    private boolean isHide = true;


    @Override
    protected int getContentViewId() {
        return R.layout.activity_login_password;
    }

    @Override
    protected void init() {
        mLayout_Check_Password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHide) {
                    isHide = false;
                    //选择状态 显示明文--设置为可见的密码
                    et_Password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    mImage.setImageResource(R.drawable.ic_login_eye_close);
                } else {
                    isHide = true;
                    //默认状态显示密码--设置文本 要一起写才能起作用 InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                    et_Password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mImage.setImageResource(R.drawable.ic_login_eye);
                }
            }
        });
        et_Password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    Map<String, String> map = new HashMap<>();
                    if (Common.empty(et_Phone.getText().toString())) {
                        ToastUtil.showShort("请输入账号");
                        return false;
                    }
                    map.put("mobile", et_Phone.getText().toString());
                    if (Common.empty(et_Password.getText().toString())) {
                        ToastUtil.showShort("请输入密码");
                        return false;
                    }
                    map.put("password", et_Password.getText().toString());
                    mHttpsPresenter.request(map, Constant.LOGIN_PASSWORD);
                    return true;
                }
                return false;
            }
        });

        mRemainTime.start();
        mRemainTime.setOnFinishTimeClick(new CountDownButton.OnFinishTimeClick() {
            @Override
            public void onFinishTimeClick() {
                hideKeyBoard();
                if (Common.isTopActivity(LoginActivity_Password.this, "com.chuxinbuer.deyiguanjia.mvp.view.activity.LoginActivity_Password")) {
                    Common.openActivity(LoginActivity_Password.this, LoginActivity.class);
                }
                stopTask();
            }
        });
    }

    @Override
    protected void initBundleData() {
        mHttpsPresenter = new HttpsPresenter(this, this);
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


    @OnClick({R.id.mLayout_Back, R.id.btn_Confirm})
    public void onClick(View view) {
        if (Common.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.mLayout_Back:
                hideKeyBoard();
                onBackPressed();
                break;
            case R.id.btn_Confirm:
                Map<String, String> map = new HashMap<>();
                if (Common.empty(et_Phone.getText().toString())) {
                    ToastUtil.showShort("请输入账号");
                    return;
                }
                map.put("mobile", et_Phone.getText().toString());
                if (Common.empty(et_Password.getText().toString())) {
                    ToastUtil.showShort("请输入密码");
                    return;
                }
                map.put("password", et_Password.getText().toString());
                mHttpsPresenter.request(map, Constant.LOGIN_PASSWORD);
                break;
        }
    }

    @Override
    public void showResult(String status, String pRows, String url) {
        if (status.equals(ExceptionEngine._SUCCESS)) {
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
            Common.openActivity(LoginActivity_Password.this, ChooseRefuseKindActivity.class);
            stopTask();
        }
    }
}

