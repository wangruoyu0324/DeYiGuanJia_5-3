package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
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
import com.chuxinbuer.deyiguanjia.utils.LogUtils;
import com.chuxinbuer.deyiguanjia.utils.SPUtil;
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;
import com.chuxinbuer.deyiguanjia.widget.CountDownButton;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bingoogolapple.bgabanner.BGABanner;

public class ChooseRefuseKindActivity extends BaseActivity implements IBaseView {

    @BindView(R.id.mBanner)
    BGABanner mBanner;
    @BindView(R.id.mWeight)
    TextView mWeight;
    @BindView(R.id.mTip)
    TextView mTip;
    @BindView(R.id.mLayout_Dialog)
    LinearLayout mLayout_Dialog;
    @BindView(R.id.mRemainTime)
    CountDownButton mRemainTime;
    @BindView(R.id.mRootView)
    RelativeLayout mRootView;
    @BindView(R.id.btn_pos)
    Button btn_pos;
    @BindView(R.id.mLayout_LoginInfo)
    LinearLayout mLayout_LoginInfo;
    private List<BannerModel> bannerList = new ArrayList<>();
    private BGABanner.Adapter<RelativeLayout, BannerModel> mImagesAdapter;

    private float curWeight = 0;

    private boolean isManager = false;

    private SpeechSynthesizer mSpeechSynthesizer;
    private AudioManager mAudioManager;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_choose_refusekind;
    }

    @Override
    protected void init() {
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        mRemainTime.start();
        mRemainTime.setOnFinishTimeClick(new CountDownButton.OnFinishTimeClick() {
            @Override
            public void onFinishTimeClick() {
                if (Common.isTopActivity(ChooseRefuseKindActivity.this, "com.chuxinbuer.deyiguanjia.mvp.view.activity.ChooseRefuseKindActivity")) {
                    requestNetwor();
                } else {
                    stopTask();
                }
            }
        });

        Map<String, String> map = new HashMap<>();
        map.put("type", "3");
        new HttpsPresenter(this, this).request(map, Constant.BANNER, false);

        mLayout_Dialog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    protected void initBundleData() {
        isManager = getIntent().getBooleanExtra("isManager", false);
        mLayout_Dialog.setVisibility(View.GONE);
        if (isManager) {
            mRemainTime.setLength(600 * 1000);
            mTip.setText("请选择更换类型");
            mLayout_LoginInfo.setVisibility(View.INVISIBLE);
            mWeight.setVisibility(View.INVISIBLE);
        } else {
            mRemainTime.setLength(180 * 1000);
            mTip.setText("请选择投放类型");
            mLayout_LoginInfo.setVisibility(View.VISIBLE);
            mWeight.setVisibility(View.VISIBLE);
        }

        if (mSpeechSynthesizer == null)
            mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(ChooseRefuseKindActivity.this);

        // 3. 设置appId，appKey.secretKey
        mSpeechSynthesizer.setAppId("24256045");
        mSpeechSynthesizer.setApiKey("dT0LLGVWgzEYApGLAzyGKwtC", "2L4h3ZFSZpBIZnXEDxl5kOwsoNZbWFLo");

// 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声  3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-15 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        // 6. 初始化
        // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
        TtsMode ttsMode = TtsMode.ONLINE;
        mSpeechSynthesizer.initTts(ttsMode);
    }

    @Override
    public void onBackPressed() {
        requestNetwor();
    }

    @OnClick({R.id.mLayout_Back, R.id.mLayout_FANGZHI, R.id.mLayout_ZHIZHANG1,
            R.id.mLayout_BOLI, R.id.mLayout_ZHIZHANG2, R.id.mLayout_JINSHU, R.id.mLayout_SULIAO, R.id.btn_neg, R.id.btn_pos})
    public void onClick(View view) {
        if (Common.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.mLayout_Back:
                onBackPressed();
                break;
            case R.id.mLayout_ZHIZHANG1:
                if (Common.isWeightTop(Constant.REFUSEKIND_ZHIZHANG1)) {
                    fullNotice();
                }
                try {
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.REFUSE_TYPE, Constant.REFUSEKIND_ZHIZHANG1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Bundle b1 = new Bundle();
                b1.putBoolean("isManager", isManager);
                Common.openActivity(this, OpenRefuseBoxActivity.class, b1, 1, R.anim.push_right_in, R.anim.push_left_out);
                break;
            case R.id.mLayout_ZHIZHANG2:
                if (Common.isWeightTop(Constant.REFUSEKIND_ZHIZHANG2)) {
                    fullNotice();
                }
                try {
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.REFUSE_TYPE, Constant.REFUSEKIND_ZHIZHANG2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Bundle b2 = new Bundle();
                b2.putBoolean("isManager", isManager);
                Common.openActivity(this, OpenRefuseBoxActivity.class, b2, 1, R.anim.push_right_in, R.anim.push_left_out);
                break;
            case R.id.mLayout_FANGZHI:
                if (Common.isWeightTop(Constant.REFUSEKIND_FANGZHI)) {
                    fullNotice();
                }
                try {
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.REFUSE_TYPE, Constant.REFUSEKIND_FANGZHI);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Bundle b3 = new Bundle();
                b3.putBoolean("isManager", isManager);
                Common.openActivity(this, OpenRefuseBoxActivity.class, b3, 1, R.anim.push_right_in, R.anim.push_left_out);
                break;
            case R.id.mLayout_BOLI:
                try {
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.REFUSE_TYPE, Constant.REFUSEKIND_BOLI);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Common.isWeightTop(Constant.REFUSEKIND_BOLI)) {
                    fullNotice();
                }
                Bundle b4 = new Bundle();
                b4.putBoolean("isManager", isManager);
                Common.openActivity(this, OpenRefuseBoxActivity.class, b4, 1, R.anim.push_right_in, R.anim.push_left_out);
                break;
            case R.id.mLayout_JINSHU:
                if (Common.isWeightTop(Constant.REFUSEKIND_JINSHU)) {
                    fullNotice();
                }
                try {
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.REFUSE_TYPE, Constant.REFUSEKIND_JINSHU);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Bundle b5 = new Bundle();
                b5.putBoolean("isManager", isManager);
                Common.openActivity(this, OpenRefuseBoxActivity.class, b5, 1, R.anim.push_right_in, R.anim.push_left_out);
                break;
            case R.id.mLayout_SULIAO:
                if (Common.isWeightTop(Constant.REFUSEKIND_SULIAO)) {
                    fullNotice();
                }
                try {
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.REFUSE_TYPE, Constant.REFUSEKIND_SULIAO);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Bundle b6 = new Bundle();
                b6.putBoolean("isManager", isManager);
                Common.openActivity(this, OpenRefuseBoxActivity.class, b6, 1, R.anim.push_right_in, R.anim.push_left_out);
                break;
            case R.id.btn_neg:
                mLayout_Dialog.setVisibility(View.GONE);

                clearTimer();
                mRemainTime.clearCurTimer();
                mRemainTime.setLength(180 * 1000);
                mRemainTime.start();
                break;
            case R.id.btn_pos:
                mLayout_Dialog.setVisibility(View.GONE);

                requestNetwor();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {//重量数据返回
                if (data != null) {
                    curWeight = data.getFloatExtra("weight", 0);
//                    if (curWeight <= 10) {
//                        curWeight = 0;
//                    } else {
//                        curWeight = curWeight - 10;
//                    }

                    if (curWeight <= 0) {
                        curWeight = 0;
                    }

                    if (mWeight != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SpannableStringBuilder ssb = new SpannableStringBuilder("本次丢弃垃圾" + curWeight + "g");
                                ssb.setSpan(new ForegroundColorSpan(Color.RED), 6, ssb.toString().length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                mWeight.setText(ssb);
                            }
                        });
                    }

                    float money = 0.00f;
                    if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
                        SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG1_CUR, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_ZHIZHANG1_CUR));
                        SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG1, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_ZHIZHANG1));

                        money = AppConfigManager.getInitedAppConfig().getRate_zhizhang() * curWeight;
                    } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
                        SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG2_CUR, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_ZHIZHANG2_CUR));
                        SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG2, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_ZHIZHANG2));

                        money = AppConfigManager.getInitedAppConfig().getRate_zhizhang() * curWeight;
                    } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
                        SPUtil.getInstance().put(Constant.WEIGHT_FANGZHI_CUR, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_FANGZHI_CUR));
                        SPUtil.getInstance().put(Constant.WEIGHT_FANGZHI, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_FANGZHI));

                        money = AppConfigManager.getInitedAppConfig().getRate_fangzhi() * curWeight;
                    } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
                        SPUtil.getInstance().put(Constant.WEIGHT_BOLI_CUR, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_BOLI_CUR));
                        SPUtil.getInstance().put(Constant.WEIGHT_BOLI, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_BOLI));

                        money = AppConfigManager.getInitedAppConfig().getRate_boli() * curWeight;
                    } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
                        SPUtil.getInstance().put(Constant.WEIGHT_JINSHU_CUR, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_JINSHU_CUR));
                        SPUtil.getInstance().put(Constant.WEIGHT_JINSHU, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_JINSHU));

                        money = AppConfigManager.getInitedAppConfig().getRate_jinshu() * curWeight;
                    } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
                        SPUtil.getInstance().put(Constant.WEIGHT_SULIAO_CUR, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_SULIAO_CUR));
                        SPUtil.getInstance().put(Constant.WEIGHT_SULIAO, curWeight + SPUtil.getInstance().getFloat(Constant.WEIGHT_SULIAO));

                        money = AppConfigManager.getInitedAppConfig().getRate_suliao() * curWeight;
                    }

                    int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (max * AppConfigManager.getInitedAppConfig().getVoice_breadcost()), 0);

                    money = Common.formatDouble3(money / 100);
                    if (money > 0.01) {
                        // 1. 获取实例
                        if (mSpeechSynthesizer == null) {
                            ToastUtil.showShort("[ERROR], 初始化失败");
                        } else {
                            mSpeechSynthesizer.speak("本次获得环保积分奖励" + money + "元");
                        }
                    }


                    mRemainTime.clearCurTimer();
                    mRemainTime.setLength(AppConfigManager.getInitedAppConfig().getOvertime() * 1000);
                    mRemainTime.start();

                    requestNetwor_Update();
                }
            }
        }
    }

    @Override
    public void showResult(String status, String pRows, String url) {
        if (status.equals(ExceptionEngine._SUCCESS)) {
            if (url.equals(Constant.POST_REFUSE_SETTLEMENT)) {
                LogUtils.e("mParams=" + pRows);

                SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG1_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG2_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_FANGZHI_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_BOLI_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_JINSHU_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_SULIAO_CUR, 0f);

                Common.openActivity(ChooseRefuseKindActivity.this, LoginActivity.class);
                stopTask();
            } else if (url.equals(Constant.ADDJUNKLOG)) {
            } else if (url.equals(Constant.FULLNOTICE)) {

            } else {
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
            }
        } else {
            if (url.equals(Constant.POST_REFUSE_SETTLEMENT)) {
                Common.openActivity(ChooseRefuseKindActivity.this, LoginActivity.class);
                stopTask();
            } else if (url.equals(Constant.ADDJUNKLOG)) {
            }
        }
    }


    private void stopTask() {
        if (mRootView != null) {
            mRootView.setVisibility(View.VISIBLE);
        }

        if (mRemainTime != null) mRemainTime.clearCurTimer();
        clearTimer();
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

    private Timer timer;
    private TimerTask timerTask;
    private long countDownLength = 60 * 1000;

    /**
     * 开始倒计时s
     */
    public void start() {
        initTimer();
        timer.schedule(timerTask, 0, 1000);
    }

    /**
     * 初始化时间
     */
    private void initTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        timer = new Timer();

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (handler != null) {
                    handler.sendEmptyMessage(1);
                }
            }
        };
    }

    /**
     * 更新显示的文本
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            countDownLength -= 1000;
            btn_pos.setText("确定（" + countDownLength / 1000 + "s）");
            if (countDownLength < 0) {
                clearTimer();

                LogUtils.e("------------定时器执行");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLayout_Dialog.setVisibility(View.GONE);
                    }
                });
                requestNetwor();
            }
        }
    };

    /**
     * 清除倒计时
     */
    private void clearTimer() {
        countDownLength = 60 * 1000;
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }


    private List<BannerModel> bannerList2;

    private void requestNetwor() {
        //'c1'=>'纺织物','c2'=>'玻璃','c3'=>'纸张1','c31'=>'纸张2','c4'=>'金属',
        // 'c5'=>'塑料1','c51'=>'塑料2','c6'=>'厨余1'，,'c61'=>'厨余2,'c7'=>'有毒害','c8'=>'其他1' 'c81'=>'其他2'"
        Map<String, String> map = new HashMap<>();
        map.put("c3", SPUtil.getInstance().getFloat(Constant.WEIGHT_ZHIZHANG1_CUR) + "");
        map.put("c31", SPUtil.getInstance().getFloat(Constant.WEIGHT_ZHIZHANG2_CUR) + "");
        map.put("c1", SPUtil.getInstance().getFloat(Constant.WEIGHT_FANGZHI_CUR) + "");
        map.put("c2", SPUtil.getInstance().getFloat(Constant.WEIGHT_BOLI_CUR) + "");
        map.put("c4", SPUtil.getInstance().getFloat(Constant.WEIGHT_JINSHU_CUR) + "");
        map.put("c5", SPUtil.getInstance().getFloat(Constant.WEIGHT_SULIAO_CUR) + "");

        map.put("w3", SPUtil.getInstance().getFloat(Constant.WEIGHT_ZHIZHANG1) + "");
        map.put("w31", SPUtil.getInstance().getFloat(Constant.WEIGHT_ZHIZHANG2) + "");
        map.put("w1", SPUtil.getInstance().getFloat(Constant.WEIGHT_FANGZHI) + "");
        map.put("w2", SPUtil.getInstance().getFloat(Constant.WEIGHT_BOLI) + "");
        map.put("w4", SPUtil.getInstance().getFloat(Constant.WEIGHT_JINSHU) + "");
        map.put("w5", SPUtil.getInstance().getFloat(Constant.WEIGHT_SULIAO) + "");

        map.put("token", AppConfigManager.getInitedAppConfig().getToken());
        map.put("deviceno", AppConfigManager.getInitedAppConfig().getDevice_token());
        new HttpsPresenter(ChooseRefuseKindActivity.this, ChooseRefuseKindActivity.this).request(map, Constant.POST_REFUSE_SETTLEMENT, false);
    }

    private void requestNetwor_Update() {
        Map<String, String> map = new HashMap<>();
        if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
            map.put("cid", "3");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
            map.put("cid", "31");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
            map.put("cid", "1");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
            map.put("cid", "2");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
            map.put("cid", "4");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
            map.put("cid", "5");
        }
        map.put("k", (int) curWeight + "");
        map.put("token", AppConfigManager.getInitedAppConfig().getToken());
        map.put("deviceno", AppConfigManager.getInitedAppConfig().getDevice_token());
        new HttpsPresenter(ChooseRefuseKindActivity.this, ChooseRefuseKindActivity.this).request(map, Constant.ADDJUNKLOG, false);
    }

    private void fullNotice() {
        Map<String, String> map = new HashMap<>();
        if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
            map.put("cid", "3");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
            map.put("cid", "31");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
            map.put("cid", "2");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
            map.put("cid", "1");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
            map.put("cid", "4");
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
            map.put("cid", "5");
        }
        map.put("token", AppConfigManager.getInitedAppConfig().getToken());
        map.put("deviceno", AppConfigManager.getInitedAppConfig().getDevice_token());
        new HttpsPresenter(ChooseRefuseKindActivity.this, ChooseRefuseKindActivity.this).request(map, Constant.FULLNOTICE, false);
    }
}

