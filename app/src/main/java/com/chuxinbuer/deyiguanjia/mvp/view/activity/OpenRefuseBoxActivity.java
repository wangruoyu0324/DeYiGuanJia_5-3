package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.base.BaseActivity;
import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.database.AppConfigManager;
import com.chuxinbuer.deyiguanjia.database.AppConfigPB;
import com.chuxinbuer.deyiguanjia.dialog.MyAlertDialog;
import com.chuxinbuer.deyiguanjia.fresco.FrescoUtil;
import com.chuxinbuer.deyiguanjia.http.exception.ApiException;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.mvp.model.BannerModel;
import com.chuxinbuer.deyiguanjia.mvp.presenter.HttpsPresenter;
import com.chuxinbuer.deyiguanjia.mvp.view.iface.IBaseView;
import com.chuxinbuer.deyiguanjia.utils.CRC16Util;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.chuxinbuer.deyiguanjia.utils.DestroyActivityUtil;
import com.chuxinbuer.deyiguanjia.utils.LogUtils;
import com.chuxinbuer.deyiguanjia.utils.SPUtil;
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;
import com.chuxinbuer.deyiguanjia.widget.CountDownButton;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tencent.bugly.crashreport.CrashReport;
import com.vi.vioserial.COMSerial;
import com.vi.vioserial.listener.OnComDataListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bingoogolapple.bgabanner.BGABanner;

public class OpenRefuseBoxActivity extends BaseActivity implements IBaseView {
    @BindView(R.id.mBanner)
    BGABanner mBanner;
    @BindView(R.id.mLayout_Back)
    LinearLayout mLayoutBack;
    @BindView(R.id.mRemainTime)
    CountDownButton mRemainTime;
    @BindView(R.id.mLayout_Info)
    RelativeLayout mLayoutInfo;
    @BindView(R.id.mTip)
    TextView mTip;
    @BindView(R.id.mView11)
    View mView11;
    @BindView(R.id.mView12)
    View mView12;
    @BindView(R.id.mView21)
    View mView21;
    @BindView(R.id.mView22)
    View mView22;
    @BindView(R.id.mView31)
    View mView31;
    @BindView(R.id.mView32)
    View mView32;
    @BindView(R.id.mView41)
    View mView41;
    @BindView(R.id.mView42)
    View mView42;
    @BindView(R.id.mView51)
    View mView51;
    @BindView(R.id.mView52)
    View mView52;

    private List<BannerModel> bannerList = new ArrayList<>();
    private BGABanner.Adapter<RelativeLayout, BannerModel> mImagesAdapter;

    private boolean isManager = false;//????????????????????????

    private AnimationDrawable animationDrawable1;
    private AnimationDrawable animationDrawable2;
    private AnimationDrawable animationDrawable3;
    private AnimationDrawable animationDrawable4;
    private AnimationDrawable animationDrawable5;
    private AnimationDrawable animationDrawable6;
    private AnimationDrawable animationDrawable7;
    private AnimationDrawable animationDrawable8;
    private AnimationDrawable animationDrawable9;
    private AnimationDrawable animationDrawable10;

    private MyAlertDialog myAlertDialog;

    private int repeatNum = 0;

    private long time = 0;

    private SpeechSynthesizer mSpeechSynthesizer;
    private AudioManager mAudioManager;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_open_refusebox;
    }

    @Override
    protected void init() {
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        time = System.currentTimeMillis() / 1000;//????????????
        SPUtil.getInstance().put(Constant.LASTTIME, time);
        mRemainTime.setLength(120 * 1000);
        mRemainTime.start();
        mRemainTime.setOnFinishTimeClick(new CountDownButton.OnFinishTimeClick() {
            @Override
            public void onFinishTimeClick() {
                long time = SPUtil.getInstance().getLong(Constant.LASTTIME);
                if (Common.isTopActivity(OpenRefuseBoxActivity.this, "com.chuxinbuer.deyiguanjia.mvp.view.activity.OpenRefuseBoxActivity")) {
                    registerSendRepeat("", false);
                    Common.openActivity(OpenRefuseBoxActivity.this, LoginActivity.class);
                }
                stopTask();
            }
        });


        Map<String, String> map = new HashMap<>();
        map.put("type", "3");
        new HttpsPresenter(this, this).request(map, Constant.BANNER, false);
    }

    @OnClick(R.id.mLayout_Back)
    public void onClick() {
        stopTask();
    }

    private void dealReceiveData(String result, int type) {
        if (type == 1) {//??????????????????
            result = result.trim();
            if (result.contains(" ")) {
                String[] str = result.split(" ");
                String status = str[str.length - 2];
                if (status.equals("11")) {//????????????????????????????????????????????????????????????
                    if (AppConfigManager.getInitedAppConfig().isIs_opendoor()) {//????????????????????????
                        try {
                            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.IS_OPENDOOR, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        registerSendRepeat("", false);
                        if (AppConfigManager.getInitedAppConfig().isIs_manager()) {//???????????????????????????????????????????????????
                            Map<String, String> map = new HashMap<>();
                            map.put("token", AppConfigManager.getInitedAppConfig().getToken());

                            //"??????????????? 'c1'=>'?????????','c2'=>'??????','c3'=>'??????','c4'=>'??????',
                            // 'c5'=>'??????','c6'=>'??????1'???,'c61'=>'??????2,'c7'=>'?????????','c8'=>'??????1' 'c81'=>'??????2'"
                            if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
                                map.put("box_class", "c3");
                            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
                                map.put("box_class", "c31");
                            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
                                map.put("box_class", "c1");
                            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
                                map.put("box_class", "c2");
                            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
                                map.put("box_class", "c4");
                            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
                                map.put("box_class", "c5");
                            }
                            new HttpsPresenter(OpenRefuseBoxActivity.this, OpenRefuseBoxActivity.this).request(map, Constant.EXCHANGE_REFUSEBOX_MANAGER);
                        } else {//?????????????????????
                            sendMsg_Weight(getWeightSendMsg(AppConfigManager.getInitedAppConfig().getRefuse_type()));
                        }
                    } else {//???????????????
                        repeatNum++;
                        if (repeatNum >= 4) {
                            repeatNum = 0;

                            if (myAlertDialog == null)
                                myAlertDialog = new MyAlertDialog(OpenRefuseBoxActivity.this).builder().setCancelable(false).setCanceledOnTouchOutside(false);
                            myAlertDialog.setTitle("????????????").setMsg("????????????????????????????????????????????????????????????").setPositiveButton("??????", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Common.openActivity(OpenRefuseBoxActivity.this, LoginActivity.class);
                                    stopTask();
                                }
                            }).show();
                            start();
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    repeatOpenDoor();
                                }
                            }, 500);
                        }
                    }
                } else {
                    if (!AppConfigManager.getInitedAppConfig().isIs_opendoor()) { //???????????????????????????????????????????????????
                        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (max * AppConfigManager.getInitedAppConfig().getVoice_breadcost()), 0);
                        // 1. ????????????
                        if (mSpeechSynthesizer == null) {
                            ToastUtil.showShort("[ERROR], ???????????????");
                        } else {
                            mSpeechSynthesizer.speak("??????????????????????????????????????????");
                        }
                        try {
                            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.IS_OPENDOOR, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sendMsg_Weight(getPeelSendMsg(AppConfigManager.getInitedAppConfig().getRefuse_type()));
                    } else {
                    }
                }
            }
        } else if (type == 2) {//??????????????????
            float weight = 0f;
            if (result.contains(" ")) {
                try {
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.IS_WEIGHTING, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (result.split(" ").length == 8) {
                    String value = result.split(" ")[4] + result.split(" ")[5];
                    int d = Integer.valueOf(value, 16);
                    if (d == 0) {
                        if (isPeel) {
                            LogUtils.e("????????????????????????" + result + "-----????????????");
                            registerSendRepeat(getSendMsg(false, AppConfigManager.getInitedAppConfig().isIs_manager()), true);
                        } else {
                            LogUtils.e("????????????????????????" + result + "-----????????????");
                            deaiWeight(0, result);
                        }
                    } else {
                        ToastUtil.showShort("????????????,???????????????????????????");
                        LogUtils.e("????????????????????????" + result + "-----????????????");
                    }
                } else {
                    if (result.length() > 12) {
                        String value = result.split(" ")[3] + result.split(" ")[4];
                        int d = Integer.valueOf(value, 16);
                        if (d < 32767) {
                            weight = d * 10;
                            deaiWeight(weight, result);
                        } else {
                            weight = 0;
                            deaiWeight(weight, result);
                        }
                    } else {
                        ToastUtil.showShort("????????????????????????,???????????????????????????");
                        LogUtils.e("????????????????????????");
                    }
                }
            }
        }
    }

    private void repeatOpenDoor() {
        if (isManager) {
            if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
                sendMsg(checkXor("8A010211"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
                sendMsg(checkXor("8A010411"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
                sendMsg(checkXor("8A010611"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
                sendMsg(checkXor("8A010811"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
                sendMsg(checkXor("8A010A11"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
                sendMsg(checkXor("8A010A11"));
            }
        } else {
            if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
                sendMsg(checkXor("8A010111"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
                sendMsg(checkXor("8A010311"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
                sendMsg(checkXor("8A010511"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
                sendMsg(checkXor("8A010711"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
                sendMsg(checkXor("8A010911"));
            } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
                sendMsg(checkXor("8A010911"));
            }
        }
    }

    public void sendMsg(String result) {
        isCompleteReceiveData = false;
        boolean isSendMsg = false;
        if (COMSerial.instance().isOpen(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""))) {
            COMSerial.instance().close(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""));
        }
        if (!COMSerial.instance().isOpen(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""))) {
            COMSerial.instance().addCOM(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""), 9600, 1, 8, 0, 0);
        }
        do {
            isSendMsg = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    COMSerial.instance().sendHex(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""), result);
                }
            }, 200);
        } while (COMSerial.instance().isOpen(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, "")) && !isSendMsg && !COMSerial.instance().isOpen(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, "")));
    }

    public void sendMsg_Weight(String result) {
        LogUtils.e("??????????????????:" + result);
        isCompleteReceiveData = false;
        boolean isSendMsg = false;
        registerSendRepeat("", false);
        if (COMSerial.instance().isOpen(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""))) {
            COMSerial.instance().close(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""));
        }
        if (!COMSerial.instance().isOpen(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""))) {
            COMSerial.instance().addCOM(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""), 19200, 1, 8, 2, 0);
        }
        do {
            isSendMsg = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    COMSerial.instance().sendHex(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""), result);
                }
            }, 200);
        } while (COMSerial.instance().isOpen(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, "")) && !isSendMsg && !COMSerial.instance().isOpen(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, "")));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        registerSendRepeat("", false);
    }

    private boolean isCompleteReceiveData = false;

    @Override
    protected void initBundleData() {
        if (mSpeechSynthesizer == null)
            mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(OpenRefuseBoxActivity.this);

        // 3. ??????appId???appKey.secretKey
        mSpeechSynthesizer.setAppId("24256045");
        mSpeechSynthesizer.setApiKey("dT0LLGVWgzEYApGLAzyGKwtC", "2L4h3ZFSZpBIZnXEDxl5kOwsoNZbWFLo");

// 5. ??????setParam ??????????????????????????????????????????
        // ??????????????????????????? 0 ???????????????????????? 1 ????????????  3 ????????????<?????????> 4 ???????????????<?????????>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // ????????????????????????0-15 ????????? 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // ????????????????????????0-15 ????????? 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // ????????????????????????0-15 ????????? 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        // 6. ?????????
        // TtsMode.MIX; ????????????????????????????????? TtsMode.ONLINE ???????????? ???????????????
        TtsMode ttsMode = TtsMode.ONLINE;
        mSpeechSynthesizer.initTts(ttsMode);

        try {
            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.IS_OPENDOOR, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //dataListener??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????hex???
        COMSerial.instance().addDataListener(new OnComDataListener() {
            @Override
            public void comDataBack(String com, String hexData) {
                if (!isCompleteReceiveData) {
                    isCompleteReceiveData = true;
                    if (com.equals(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""))) {
                        dealReceiveData(addSpace(hexData), 1);
                    } else if (com.equals(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""))) {
                        dealReceiveData(addSpace(hexData), 2);
                    }
                }
            }
        });

        isManager = getIntent().getBooleanExtra("isManager", false);
        try {
            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.IS_MANAGER, isManager);
            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.IS_WEIGHTING, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtils.e("??????????????????");
        sendMsg(getSendMsg(true, AppConfigManager.getInitedAppConfig().isIs_manager()));

        animationDrawable1 = (AnimationDrawable) mView11.getBackground();
        animationDrawable2 = (AnimationDrawable) mView12.getBackground();
        animationDrawable3 = (AnimationDrawable) mView21.getBackground();
        animationDrawable4 = (AnimationDrawable) mView22.getBackground();
        animationDrawable5 = (AnimationDrawable) mView31.getBackground();
        animationDrawable6 = (AnimationDrawable) mView32.getBackground();
        animationDrawable7 = (AnimationDrawable) mView41.getBackground();
        animationDrawable8 = (AnimationDrawable) mView42.getBackground();
        animationDrawable9 = (AnimationDrawable) mView51.getBackground();
        animationDrawable10 = (AnimationDrawable) mView52.getBackground();
        if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
            if (AppConfigManager.getInitedAppConfig().isIs_manager()) {
                animationDrawable2.start();
                mTip.setText("??????1??????????????????");
            } else {
                animationDrawable1.start();
                mTip.setText("??????1??????????????????");
            }
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
            if (AppConfigManager.getInitedAppConfig().isIs_manager()) {
                animationDrawable4.start();
                mTip.setText("??????2??????????????????");
            } else {
                animationDrawable3.start();
                mTip.setText("??????2??????????????????");
            }
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
            if (AppConfigManager.getInitedAppConfig().isIs_manager()) {
                animationDrawable6.start();
                mTip.setText("???????????????????????????");
            } else {
                animationDrawable5.start();
                mTip.setText("???????????????????????????");
            }
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
            if (AppConfigManager.getInitedAppConfig().isIs_manager()) {
                animationDrawable8.start();
                mTip.setText("???????????????????????????");
            } else {
                animationDrawable7.start();
                mTip.setText("???????????????????????????");
            }
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
            if (AppConfigManager.getInitedAppConfig().isIs_manager()) {
                animationDrawable10.start();
                mTip.setText("???????????????????????????");
            } else {
                animationDrawable9.start();
                mTip.setText("???????????????????????????");
            }
        } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
            if (AppConfigManager.getInitedAppConfig().isIs_manager()) {
                animationDrawable10.start();
                mTip.setText("???????????????????????????");
            } else {
                animationDrawable9.start();
                mTip.setText("???????????????????????????");
            }
        }
    }

    private void stopTask() {
        mHandler.removeCallbacksAndMessages(null);
        COMSerial.instance().clearAllDataListener();
        COMSerial.instance().close(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""));
        COMSerial.instance().close(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""));

        SPUtil.getInstance().put(Constant.LASTTIME, System.currentTimeMillis() / 1000);
        SPUtil.getInstance().put(Constant.IS_FINISHPAGE, true);
        if (mRemainTime != null) mRemainTime.clearCurTimer();
        if (myAlertDialog != null) {
            myAlertDialog.dismiss();
        }

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

                                mIntegralZhiZhang.setText("?????????" + (int) (AppConfigManager.getInitedAppConfig().getRate_zhizhang() * 1000) + "??????/");
                                mIntegralSuLiao.setText("?????????" + (int) (AppConfigManager.getInitedAppConfig().getRate_suliao() * 1000) + "??????/");
                                mIntegralBoLi.setText("?????????" + (int) (AppConfigManager.getInitedAppConfig().getRate_boli() * 1000) + "??????/");
                                mIntegralFangZhi.setText("?????????" + (int) (AppConfigManager.getInitedAppConfig().getRate_fangzhi() * 1000) + "??????/");
                                mIntegralJinShu.setText("?????????" + (int) (AppConfigManager.getInitedAppConfig().getRate_jinshu() * 1000) + "??????/");
                                mIntegralChuYu.setText("?????????" + (int) (AppConfigManager.getInitedAppConfig().getRate_chuyu() * 1000) + "??????/");
                                mIntegralOther.setText("?????????" + (int) (AppConfigManager.getInitedAppConfig().getRate_other() * 1000) + "??????/");
                                mIntegralDuHai.setText("????????????" + (int) (AppConfigManager.getInitedAppConfig().getRate_duhai() * 1000) + "??????/");

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
            } else if (url.equals(Constant.EXCHANGE_REFUSEBOX_MANAGER)) {//????????????????????????????????????????????????????????????
                //                ???????????????????????????
                sendMsg_Weight(getZeroSendMsg(AppConfigManager.getInitedAppConfig().getRefuse_type()));
            }
        }
    }

    private Timer timer;
    private TimerTask timerTask;
    private long countDownLength = 15 * 1000;

    /**
     * ???????????????
     */
    public void start() {
        initTimer();
        timer.schedule(timerTask, 0, 1000);
    }

    /**
     * ???????????????
     */
    private void initTimer() {
        timer = new Timer();
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
     * ?????????????????????
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            countDownLength -= 1000;
            if (countDownLength < 0) {
                clearTimer();
                if (myAlertDialog != null && myAlertDialog.isShowing()) {
                    myAlertDialog.dismiss();
                }


                Common.openActivity(OpenRefuseBoxActivity.this, LoginActivity.class);
                stopTask();
            }
        }
    };

    /**
     * ???????????????
     */
    private void clearTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler.removeMessages(1);
//            handler = null;
        }
    }


    private String addSpace(String s) {
        String str = "";
        if (s.length() % 2 == 0) {
            StringBuilder builder = new StringBuilder();
            char[] array = s.toCharArray();
            int length = array.length;
            for (int i = 0; i < length; i += 2) {
                if (i != 0 && i <= length - 2) {
                    builder.append(" ");
                }

                builder.append(array[i]);
                builder.append(array[i + 1]);
            }
            str = builder.toString();
        } else {
            str = s;
        }
        return str;
    }

    public String getSendMsg(boolean isOpenDoor, boolean isManager) {
        String result = "";
        if (isManager) {
            if (isOpenDoor) {
                if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
                    result = checkXor("8A010211");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
                    result = checkXor("8A010411");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
                    result = checkXor("8A010611");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
                    result = checkXor("8A010811");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
                    result = checkXor("8A010A11");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
                    result = checkXor("8A010A11");
                }
            } else {
                if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
                    result = checkXor("80010233");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
                    result = checkXor("80010433");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
                    result = checkXor("80010633");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
                    result = checkXor("80010833");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
                    result = checkXor("80010A33");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
                    result = checkXor("80010A33");
                }
            }
        } else {
            if (isOpenDoor) {
                if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
                    result = checkXor("8A010111");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
                    result = checkXor("8A010311");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
                    result = checkXor("8A010511");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
                    result = checkXor("8A010711");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
                    result = checkXor("8A010911");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
                    result = checkXor("8A010911");
                }
            } else {
                if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG1) {
                    result = checkXor("80010133");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_ZHIZHANG2) {
                    result = checkXor("80010333");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_BOLI) {
                    result = checkXor("80010533");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_FANGZHI) {
                    result = checkXor("80010733");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_JINSHU) {
                    result = checkXor("80010933");
                } else if (AppConfigManager.getInitedAppConfig().getRefuse_type() == Constant.REFUSEKIND_SULIAO) {
                    result = checkXor("80010933");
                }
            }
        }
        LogUtils.e("result====" + result);
        return result;
    }

    public String checkXor(String data) {
        int checkData = 0;
        for (int i = 0; i < data.length(); i = i + 2) {
            //???????????????????????????????????????
            int start = Integer.parseInt(data.substring(i, i + 2), 16);
            //??????????????????
            checkData = start ^ checkData;
        }
        return integerToHexString(data, checkData);
    }

    /**
     * ???????????????????????????????????????????????????
     */
    public String integerToHexString(String data, int s) {
        String ss = Integer.toHexString(s);
        if (ss.length() % 2 != 0) {
            ss = "0" + ss;//0F??????
        }
        return data + ss.toUpperCase();
    }

    private boolean isOpenRepeat = false;

    public void registerSendRepeat(String msg, boolean isOpenRepeat) {
        this.isOpenRepeat = isOpenRepeat;
        // ??????500??????????????????????????????
        if (isOpenRepeat && !Common.empty(msg)) {
            Message message = new Message();
            message.obj = msg;
            mHandler.sendMessage(message);
        } else {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!Common.empty(msg.obj)) {
                sendMsg((String) msg.obj);
                Message message = new Message();
                message.obj = msg.obj;
                mHandler.sendMessageDelayed(message, 500);
            } else {
                mHandler.removeCallbacksAndMessages(null);
            }
        }
    };


    public void saveWeight(int refuseType, float weight) {
        switch (refuseType) {
            case Constant.REFUSEKIND_ZHIZHANG1:
                SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG1_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG1, weight);
                break;
            case Constant.REFUSEKIND_ZHIZHANG2:
                SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG2_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG2, weight);
                break;
            case Constant.REFUSEKIND_BOLI:
                SPUtil.getInstance().put(Constant.WEIGHT_BOLI_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_BOLI, weight);
                break;
            case Constant.REFUSEKIND_FANGZHI:
                SPUtil.getInstance().put(Constant.WEIGHT_FANGZHI_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_FANGZHI, weight);
                break;
            case Constant.REFUSEKIND_JINSHU:
                SPUtil.getInstance().put(Constant.WEIGHT_JINSHU_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_JINSHU, weight);
                break;
            case Constant.REFUSEKIND_SULIAO:
                SPUtil.getInstance().put(Constant.WEIGHT_SULIAO_CUR, 0f);
                SPUtil.getInstance().put(Constant.WEIGHT_SULIAO, weight);
                break;
        }
    }

    public String getWeightSendMsg(int refuseType) {
        LogUtils.e("refuseType=" + refuseType);
        String result = "";
        if (refuseType == Constant.REFUSEKIND_ZHIZHANG1) {
            result = "01 03 00 2A 00 01" + CRC16Util.getCrc("01 03 00 2A 00 01");
        } else if (refuseType == Constant.REFUSEKIND_ZHIZHANG2) {
            result = "02 03 00 2A 00 01" + CRC16Util.getCrc("02 03 00 2A 00 01");
        } else if (refuseType == Constant.REFUSEKIND_BOLI) {
            result = "03 03 00 2A 00 01" + CRC16Util.getCrc("03 03 00 2A 00 01");
        } else if (refuseType == Constant.REFUSEKIND_FANGZHI) {
            result = "04 03 00 2A 00 01" + CRC16Util.getCrc("04 03 00 2A 00 01");
        } else if (refuseType == Constant.REFUSEKIND_JINSHU) {
            result = "05 03 00 2A 00 01" + CRC16Util.getCrc("05 03 00 2A 00 01");
        } else if (refuseType == Constant.REFUSEKIND_SULIAO) {
            result = "05 03 00 2A 00 01" + CRC16Util.getCrc("05 03 00 2A 00 01");
        }
        return result.replace(" ", "");
    }

    public String getZeroSendMsg(int refuseType) {
        isPeel = false;

        String result = "";
        if (refuseType == Constant.REFUSEKIND_ZHIZHANG1) {
            result = "01 06 00 24 00 00" + CRC16Util.getCrc("01 06 00 24 00 00");
        } else if (refuseType == Constant.REFUSEKIND_ZHIZHANG2) {
            result = "02 06 00 24 00 00" + CRC16Util.getCrc("02 06 00 24 00 00");
        } else if (refuseType == Constant.REFUSEKIND_BOLI) {
            result = "03 06 00 24 00 00" + CRC16Util.getCrc("03 06 00 24 00 00");
        } else if (refuseType == Constant.REFUSEKIND_FANGZHI) {
            result = "04 06 00 24 00 00" + CRC16Util.getCrc("04 06 00 24 00 00");
        } else if (refuseType == Constant.REFUSEKIND_JINSHU) {
            result = "05 06 00 24 00 00" + CRC16Util.getCrc("05 06 00 24 00 00");
        } else if (refuseType == Constant.REFUSEKIND_SULIAO) {
            result = "05 06 00 24 00 00" + CRC16Util.getCrc("05 06 00 24 00 00");
        }
        return result.replace(" ", "");
    }


    public String getPeelSendMsg(int refuseType) {
        isPeel = true;

        String result = "";
        if (refuseType == Constant.REFUSEKIND_ZHIZHANG1) {
            result = "01 06 00 25 00 00" + CRC16Util.getCrc("01 06 00 25 00 00");
        } else if (refuseType == Constant.REFUSEKIND_ZHIZHANG2) {
            result = "02 06 00 25 00 00" + CRC16Util.getCrc("02 06 00 25 00 00");
        } else if (refuseType == Constant.REFUSEKIND_BOLI) {
            result = "03 06 00 25 00 00" + CRC16Util.getCrc("03 06 00 25 00 00");
        } else if (refuseType == Constant.REFUSEKIND_FANGZHI) {
            result = "04 06 00 25 00 00" + CRC16Util.getCrc("04 06 00 25 00 00");
        } else if (refuseType == Constant.REFUSEKIND_JINSHU) {
            result = "05 06 00 25 00 00" + CRC16Util.getCrc("05 06 00 25 00 00");
        } else if (refuseType == Constant.REFUSEKIND_SULIAO) {
            result = "05 06 00 25 00 00" + CRC16Util.getCrc("05 06 00 25 00 00");
//            result=" 05 10 00 08 00 02 04 00 00 00 00 E7 39";
        }
        return result.replace(" ", "");
    }


    private boolean isPeel = false;//???????????????

    private void deaiWeight(float weight, String result) {
        if (weight < 0) {
            weight = 0;
        }
        LogUtils.e("????????????=" + weight + "g");
        LogUtils.e("????????????????????????" + result + "-----???????????????" + weight + "-----???????????????" + AppConfigManager.getInitedAppConfig().getRefuse_type());
        if (AppConfigManager.getInitedAppConfig().isIs_manager()) {
            saveWeight(AppConfigManager.getInitedAppConfig().getRefuse_type(), weight);

            Common.openActivity(OpenRefuseBoxActivity.this, LoginActivity.class);
            stopTask();
        } else {
            if (weight <= 0) {
                try {
                    //...
                    LogUtils.e("????????????????????????" + result + "-----???????????????" + weight + "-----???????????????" + AppConfigManager.getInitedAppConfig().getRefuse_type());
                    bannerList2.add(new BannerModel());
                } catch (NullPointerException thr) {
                    ApiException ex = new ApiException(thr, "0001");
                    CrashReport.postCatchedException(ex);  // bugly????????????throwable??????


                    Intent intent = new Intent();
                    intent.putExtra("weight", weight);
                    setResult(RESULT_OK, intent);
                    stopTask();
                }
            } else {
                Intent intent = new Intent();
                intent.putExtra("weight", weight);
                setResult(RESULT_OK, intent);
                stopTask();
            }
        }
    }

    private List<BannerModel> bannerList2;
}

