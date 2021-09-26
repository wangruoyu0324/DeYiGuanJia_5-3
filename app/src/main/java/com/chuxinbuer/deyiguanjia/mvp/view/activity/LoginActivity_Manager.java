package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.chuxinbuer.deyiguanjia.fresco.FrescoUtil;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.mvp.model.BannerModel;
import com.chuxinbuer.deyiguanjia.mvp.presenter.HttpsPresenter;
import com.chuxinbuer.deyiguanjia.mvp.view.iface.IBaseView;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.chuxinbuer.deyiguanjia.utils.DestroyActivityUtil;
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;
import com.chuxinbuer.deyiguanjia.widget.CountDownButton;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bingoogolapple.bgabanner.BGABanner;

public class LoginActivity_Manager extends BaseActivity implements IBaseView {
    @BindView(R.id.mBanner)
    BGABanner mBanner;
    @BindView(R.id.mRemainTime)
    CountDownButton mRemainTime;
    @BindView(R.id.et_Password)
    EditText et_Password;
    @BindView(R.id.mLayout_Check_Password)
    RelativeLayout mLayout_Check_Password;
    @BindView(R.id.mImage)
    ImageView mImage;

    private List<BannerModel> bannerList = new ArrayList<>();
    private BGABanner.Adapter<RelativeLayout, BannerModel> mImagesAdapter;

    private HttpsPresenter mHttpsPresenter;
    private boolean isHide = true;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_login_manager;
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
                    if (Common.empty(et_Password.getText().toString())) {
                        ToastUtil.showShort("请输入您的管理员密码");
                        return false;
                    }
                    hideKeyBoard();
                    map.put("password", et_Password.getText().toString());
                    mHttpsPresenter.request(map, Constant.LOGIN_MANAGER);
                    return true;
                }
                return false;
            }
        });


        mRemainTime.start();
        mRemainTime.setOnFinishTimeClick(new CountDownButton.OnFinishTimeClick() {
            @Override
            public void onFinishTimeClick() {
                if (Common.isTopActivity(LoginActivity_Manager.this, "com.chuxinbuer.deyiguanjia.mvp.view.activity.LoginActivity_Manager")) {
                    Common.openActivity(LoginActivity_Manager.this, LoginActivity.class);
                }
                stopTask();
            }
        });

        Map<String, String> map = new HashMap<>();
        map.put("type", "3");
        new HttpsPresenter(this, this).request(map, Constant.BANNER, false);
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
                if (Common.empty(et_Password.getText().toString())) {
                    ToastUtil.showShort("请输入您的管理员密码");
                    return;
                }
                hideKeyBoard();
                map.put("password", et_Password.getText().toString());
                mHttpsPresenter.request(map, Constant.LOGIN_MANAGER);
                break;
        }
    }

    @Override
    public void showResult(String status, String pRows, String url) {
        if (status.equals(ExceptionEngine._SUCCESS)) {
            if (url.equals(Constant.BANNER)) {
                bannerList.clear();
                if (!Common.empty(pRows)) {
                    bannerList = JSON.parseArray(pRows, BannerModel.class);
                }
                bannerList.add(new BannerModel());
                if(bannerList.size() > 1){
                    mBanner.setAutoPlayAble(true);
                }else {
                    mBanner.setAutoPlayAble(false);
                }
                if (mBanner != null) {
                    mBanner.setData(R.layout.item_fresco, bannerList, null);
                    mImagesAdapter = new BGABanner.Adapter<RelativeLayout, BannerModel>() {
                        @Override
                        public void fillBannerItem(BGABanner banner, RelativeLayout itemView, @Nullable BannerModel model, int position) {
                            SimpleDraweeView simpleDraweeView = itemView.findViewById(R.id.mPic);

                            RelativeLayout mLayout_Integral = itemView.findViewById(R.id.mLayout_Integral);
                            SimpleDraweeView mPic2 = itemView.findViewById(R.id.mPic2);
                            TextView mIntegralZhiZhang = itemView.findViewById(R.id.mIntegralZhiZhang);
                            TextView mIntegralSuLiao = itemView.findViewById(R.id.mIntegralSuLiao);
                            TextView mIntegralBoLi = itemView.findViewById(R.id.mIntegralBoLi);
                            TextView mIntegralFangZhi = itemView.findViewById(R.id.mIntegralFangZhi);
                            TextView mIntegralJinShu = itemView.findViewById(R.id.mIntegralJinShu);
                            TextView mIntegralChuYu = itemView.findViewById(R.id.mIntegralChuYu);
                            TextView mIntegralOther = itemView.findViewById(R.id.mIntegralOther);
                            TextView mIntegralDuHai = itemView.findViewById(R.id.mIntegralDuHai);

                            TextView mUnitZhiZhang = itemView.findViewById(R.id.mUnit_ZhiZhang);
                            TextView mUnitSuLiao = itemView.findViewById(R.id.mUnit_SuLiao);
                            TextView mUnitBoLi = itemView.findViewById(R.id.mUnit_BoLi);
                            TextView mUnitFangZhi = itemView.findViewById(R.id.mUnit_FangZhi);
                            TextView mUnitJinShu = itemView.findViewById(R.id.mUnit_JinShu);
                            TextView mUnitChuYu = itemView.findViewById(R.id.mUnit_ChuYu);
                            TextView mUnitOther = itemView.findViewById(R.id.mUnit_Other);
                            TextView mUnitDuHai = itemView.findViewById(R.id.mUnit_DuHai);

                            if (position == bannerList.size() - 1) {
                                mLayout_Integral.setVisibility(View.VISIBLE);
                                simpleDraweeView.setVisibility(View.GONE);
                                FrescoUtil.display(mPic2, AppConfigManager.getInitedAppConfig().getCate_background());

                                mIntegralZhiZhang.setText("纸张：" + (int) (AppConfigManager.getInitedAppConfig().getRate_zhizhang() * 1000) + "积分/");
                                mIntegralSuLiao.setText("塑料：" + (int) (AppConfigManager.getInitedAppConfig().getRate_suliao() * 1000) + "积分/");
                                mIntegralBoLi.setText("玻璃：" + (int) (AppConfigManager.getInitedAppConfig().getRate_boli() * 1000) + "积分/");
                                mIntegralFangZhi.setText("纺织：" + (int) (AppConfigManager.getInitedAppConfig().getRate_fangzhi() * 1000) + "积分/");
                                mIntegralJinShu.setText("金属：" + (int) (AppConfigManager.getInitedAppConfig().getRate_jinshu() * 1000) + "积分/");
                                mIntegralChuYu.setText("厨余：" + (int) (AppConfigManager.getInitedAppConfig().getRate_chuyu() * 1000) + "积分/");
                                mIntegralOther.setText("其他：" + (int) (AppConfigManager.getInitedAppConfig().getRate_other() * 1000) + "积分/");
                                mIntegralDuHai.setText("有毒害：" + (int) (AppConfigManager.getInitedAppConfig().getRate_duhai() * 1000) + "积分/");

                                mUnitZhiZhang.setText("kg");
                                mUnitSuLiao.setText("kg");
                                mUnitBoLi.setText("kg");
                                mUnitFangZhi.setText("kg");
                                mUnitJinShu.setText("kg");
                                mUnitChuYu.setText("kg");
                                mUnitOther.setText("kg");
                                mUnitDuHai.setText("kg");
                            } else {
                                mLayout_Integral.setVisibility(View.GONE);
                                simpleDraweeView.setVisibility(View.VISIBLE);
                                FrescoUtil.display(simpleDraweeView, model.getImage());
                            }
                        }
                    };
                    mBanner.setAdapter(mImagesAdapter);
                }
            } else {
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
                Bundle b = new Bundle();
                b.putBoolean("isManager",true);
                Common.openActivity(LoginActivity_Manager.this, ChooseRefuseKindActivity_Manager.class,b);
                stopTask();
            }
        }
    }
}

