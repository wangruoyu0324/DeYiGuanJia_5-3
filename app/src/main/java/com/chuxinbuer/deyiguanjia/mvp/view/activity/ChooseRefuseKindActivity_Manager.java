package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.base.BaseActivity;
import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.database.AppConfigManager;
import com.chuxinbuer.deyiguanjia.database.AppConfigPB;
import com.chuxinbuer.deyiguanjia.dialog.MyAlertDialog;
import com.chuxinbuer.deyiguanjia.fresco.FrescoUtil;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.mvp.model.BannerModel;
import com.chuxinbuer.deyiguanjia.mvp.model.EventMessage;
import com.chuxinbuer.deyiguanjia.mvp.model.OpenDoorModel;
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
import com.vi.vioserial.COMSerial;
import com.vi.vioserial.listener.OnComDataListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bingoogolapple.bgabanner.BGABanner;

public class ChooseRefuseKindActivity_Manager extends BaseActivity implements IBaseView {


    @BindView(R.id.mBanner)
    BGABanner mBanner;
    @BindView(R.id.mRemainTime)
    CountDownButton mRemainTime;

    private List<BannerModel> bannerList = new ArrayList<>();
    private BGABanner.Adapter<RelativeLayout, BannerModel> mImagesAdapter;

    private List<OpenDoorModel> mList = new ArrayList<>();//??????????????????

    private MyAlertDialog myAlertDialog;

    private int repeatNum = 0;
    private boolean isClickContinue = false;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_choose_refusekind_manager;
    }

    @Override
    protected void init() {

        mRemainTime.setLength(20* 60 * 1000);
        mRemainTime.start();
        mRemainTime.setOnFinishTimeClick(new CountDownButton.OnFinishTimeClick() {
            @Override
            public void onFinishTimeClick() {
                if (Common.isTopActivity(ChooseRefuseKindActivity_Manager.this, "com.chuxinbuer.deyiguanjia.mvp.view.activity.ChooseRefuseKindActivity_Manager")) {
                    Common.openActivity(ChooseRefuseKindActivity_Manager.this, LoginActivity.class);
                }
                stopTask();
            }
        });

        Map<String, String> map = new HashMap<>();
        map.put("type", "3");
        new HttpsPresenter(this, this).request(map, Constant.BANNER, false);

        COMSerial.instance().addCOM(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""), 9600, 1, 8, 0, 0);
        COMSerial.instance().addCOM(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""), 19200, 1, 8, 2, 0);
        COMSerial.instance().addDataListener(new OnComDataListener() {
            @Override
            public void comDataBack(String com, String hexData) {
                if (com.equals(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""))) {
                    dealReceiveData(addSpace(hexData), 2);
                } else if (com.equals(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""))) {
                    dealReceiveData(addSpace(hexData), 1);
                }
            }
        });
    }


    private void dealReceiveData(String it, int type) {
        if (type == 1) {//??????????????????
            LogUtils.e("-------------------?????????????????????" + it);
            registerSendRepeat("", false);
            float weight = 0f;
            if (it.contains(" ")) {
                try {
                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.IS_WEIGHTING, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (it.split(" ").length == 8) {
                    String value = it.split(" ")[4] + it.split(" ")[5];
                    int d = Integer.valueOf(value, 16);
                    if (d == 0) {
                        if (isPeel) {
                            LogUtils.e("????????????????????????" + it + "-----????????????");
                            registerSendRepeat("80010033B2", true);
                        } else {
                            LogUtils.e("????????????????????????" + it + "-----????????????");
                            deaiWeight(0, it);
                        }
                    } else {
                        ToastUtil.showShort("????????????,???????????????????????????");
                        LogUtils.e("????????????????????????" + it + "-----????????????");
                    }
                } else {
                    if (it.length() > 12) {
                        String value = it.split(" ")[3] + it.split(" ")[4];
                        int d = Integer.valueOf(value, 16);
                        if (d < 32767) {
                            weight = d * 10;
                            deaiWeight(weight, it);
                        } else {
                            weight = 0;
                            deaiWeight(weight, it);
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showShort("????????????????????????,???????????????????????????");
                            }
                        });

                        LogUtils.e("????????????????????????");
                    }
                }
            }
        } else if (type == 2) {//????????????
            LogUtils.e("-------------------?????????????????????" + it);
            if (Common.isTopActivity(ChooseRefuseKindActivity_Manager.this, "com.chuxinbuer.deyiguanjia.mvp.view.activity.ChooseRefuseKindActivity_Manager")) {
                checkSuccess(it);
            } else {
                EventBus.getDefault().post(new EventMessage("openrefusebox", it));
            }
        }
    }

    private String tran2Bit(String result) {
        String str = Integer.toBinaryString(Integer.parseInt(result, 16));
        StringBuffer stringBuffer = new StringBuffer();
        if (str.length() < 8) {
            int num = 8 - str.length();
            for (int i = 0; i < num; i++) {
                stringBuffer.append("0");
            }
        }
        str = stringBuffer.toString() + str;
        return new StringBuffer(str).reverse().toString();
    }

    private boolean isCloseComplete = true;//??????????????????????????????

    private void checkSuccess(String result) {
        if (Common.isTopActivity(ChooseRefuseKindActivity_Manager.this, "com.chuxinbuer.deyiguanjia.mvp.view.activity.ChooseRefuseKindActivity_Manager")) {
            result = result.trim();
            if (result.contains(" ")) {
                String[] str = result.split(" ");
                if (str.length == 7) {//?????????????????????????????????
                    String status_1_8 = tran2Bit(str[4]);
                    String status_9_16 = tran2Bit(str[3]);
                    String status_17_24 = tran2Bit(str[2]);
                    String checkResult = status_1_8 + status_9_16 + status_17_24;
                    LogUtils.e("???????????????1=" + checkResult);
//48:0 ?????? 49:1 ??????


                    byte[] b = checkResult.getBytes();
                    int num = mList.size();
                    for (int i = 0; i < num; i++) {
                        if (i == 4 || i == 5) {
                            if (mList.get(i).isClose() == false) {
                                if ((b[9] + "").equals("49")) {
                                    mList.get(i).setClose(true);
                                    for (int j = 0; j < num; j++) {
                                        if (j == i) {
                                            mList.get(j).setCurClose(true);
                                        } else {
                                            mList.get(j).setCurClose(false);
                                        }
                                    }
                                    hadWeighting = false;
                                    break;
                                } else {
                                    mList.get(i).setCurClose(false);
                                }
                            }
                        } else {
                            if (mList.get(i).isClose() == false) {
                                if ((b[i * 2 + 1] + "").equals("49")) {
                                    mList.get(i).setClose(true);
                                    for (int j = 0; j < num; j++) {
                                        if (j == i) {
                                            mList.get(j).setCurClose(true);
                                        } else {
                                            mList.get(j).setCurClose(false);
                                        }
                                    }
                                    hadWeighting = false;
                                    break;
                                } else {
                                    mList.get(i).setCurClose(false);
                                }
                            }
                        }
                    }
                    isCloseComplete = true;
                    int num2 = mList.size();
                    for (int i = 0; i < num2; i++) {
                        if (mList.get(i).isClose() == false) {
                            isCloseComplete = false;
                            break;
                        }
                    }

                    if (isCloseComplete) {
                        registerSendRepeat("", false);

                        mRemainTime.clearCurTimer();
                        mRemainTime.setLength(30 * 1000);
                        mRemainTime.start();
                    }
                    int num3 = mList.size();
                    for (int i = 0; i < num3; i++) {
                        if (mList.get(i).isCurClose()) {
                            if (!hadWeighting) {
                                hadWeighting = true;
                                registerSendRepeat("", false);
                                Map<String, String> map = new HashMap<>();
                                map.put("token", AppConfigManager.getInitedAppConfig().getToken());

                                //"??????????????? 'c1'=>'?????????','c2'=>'??????','c3'=>'??????','c4'=>'??????',
                                // 'c5'=>'??????','c6'=>'??????1'???,'c61'=>'??????2,'c7'=>'?????????','c8'=>'??????1' 'c81'=>'??????2'"

                                if (mList.get(i).getType() == 1) {
                                    map.put("box_class", "c3");
                                } else if (mList.get(i).getType() == 2) {
                                    map.put("box_class", "c31");
                                } else if (mList.get(i).getType() == 3) {
                                    map.put("box_class", "c2");
                                } else if (mList.get(i).getType() == 4) {
                                    map.put("box_class", "c1");
                                } else if (mList.get(i).getType() == 5) {
                                    map.put("box_class", "c4");
                                } else if (mList.get(i).getType() == 6) {
                                    map.put("box_class", "c5");
                                }
                                new HttpsPresenter(ChooseRefuseKindActivity_Manager.this, ChooseRefuseKindActivity_Manager.this).request(map, Constant.EXCHANGE_REFUSEBOX_MANAGER);
                            }
                            break;
                        }
                    }
                } else {
                    String status = str[str.length - 2];
                    if (status.equals("11")) {//????????????????????????????????????????????????????????????
                        repeatNum++;
                        if (repeatNum >= 3) {
                            repeatNum = 0;

                            if (myAlertDialog == null)
                                myAlertDialog = new MyAlertDialog(ChooseRefuseKindActivity_Manager.this).builder().setCancelable(false).setCanceledOnTouchOutside(false);
                            myAlertDialog.setTitle("????????????").setMsg("????????????????????????????????????????????????").setPositiveButton("??????", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).show();
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    repeatOpenDoor();
                                }
                            }, 500);
                        }
                    } else {
                        String strrr = "";
                        if (result.contains(" ")) {
                            strrr = result.replace(" ", "");
                        }
                        //8A 01 07 00 8C
                        //8A 01 03 00 88
                        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        String code = "";
                        if (strrr.contains("0200")) {
                            isPeel = true;
                            mList.get(0).setClose(false);
                            code = "01 06 00 25 00 00" + CRC16Util.getCrc("01 06 00 25 00 00");
                        } else if (strrr.contains("0400")) {
                            isPeel = true;
                            mList.get(1).setClose(false);
                            code = "02 06 00 25 00 00" + CRC16Util.getCrc("02 06 00 25 00 00");
                        } else if (strrr.contains("0600")) {
                            isPeel = true;
                            mList.get(2).setClose(false);
                            code = "03 06 00 25 00 00" + CRC16Util.getCrc("03 06 00 25 00 00");
                        } else if (strrr.contains("0800")) {
                            isPeel = true;
                            mList.get(3).setClose(false);
                            code = "04 06 00 25 00 00 " + CRC16Util.getCrc("04 06 00 25 00 00");
                        } else if (strrr.contains("0A00")) {
                            isPeel = true;
                            if (doorType == 5) {
                                mList.get(4).setClose(false);
                            } else {
                                mList.get(5).setClose(false);
                            }
                            code = "05 06 00 25 00 00 " + CRC16Util.getCrc("05 06 00 25 00 00");
                        }
                        String finalCode = code;
                        String str2 = "";
                        if (finalCode.contains(" ")) {
                            str2 = finalCode.replace(" ", "");
                        }
                        String finalStr = str2;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendMsg_Weight(finalStr);
                            }
                        });
                    }
                }
            }
        }
    }


    public void sendMsg(String result) {
        COMSerial.instance().sendHex(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""), result);
        LogUtils.e("-------------------??????????????????:" + result);
    }

    public void sendMsg_Weight(String result) {
        registerSendRepeat("", false);
        COMSerial.instance().sendHex(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""), result);
        LogUtils.e("-------------------??????????????????:" + result);
    }

    public void registerSendRepeat(String msg, boolean isOpenRepeat) {
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

    private void repeatOpenDoor() {
        if (doorType == 1) {
            sendMsg(checkXor("8A010211"));
        } else if (doorType == 2) {
            sendMsg(checkXor("8A010411"));
        } else if (doorType == 3) {
            sendMsg(checkXor("8A010611"));
        } else if (doorType == 4) {
            sendMsg(checkXor("8A010811"));
        } else if (doorType == 5 || doorType == 6) {
            sendMsg(checkXor("8A010A11"));
        }
    }

    @Override
    protected void initBundleData() {
        int num = 6;
        for (int i = 0; i < num; i++) {
            OpenDoorModel model = new OpenDoorModel();
            model.setType(i + 1);
            model.setClose(true);
            model.setCurClose(false);
            mList.add(model);
        }
        try {
            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.IS_WEIGHTING, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopTask() {
        mHandler.removeCallbacksAndMessages(null);
        COMSerial.instance().clearAllDataListener();
        COMSerial.instance().close(SPUtil.getInstance().getString(Constant.SERIAL_PORT_LOCK, ""));
        COMSerial.instance().close(SPUtil.getInstance().getString(Constant.SERIAL_PORT_WEIGHT, ""));

        if (mRemainTime != null) mRemainTime.clearCurTimer();
        DestroyActivityUtil.destoryActivity(this.getClass().getName());
    }


    private boolean isPeel = false;//???????????????

    private boolean hadWeighting = false;//?????????????????????

    private void deaiWeight(float weight, String result) {
        if (weight < 0) {
            weight = 0;
        }
        LogUtils.e("????????????=" + weight + "g");

        int num3 = mList.size();
        for (int i = 0; i < num3; i++) {
            if (mList.get(i).isCurClose()) {
                if (mList.get(i).getType() == 1) {
                    SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG1_CUR, 0f);
                    SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG1, 0f);
                } else if (mList.get(i).getType() == 2) {
                    SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG2_CUR, 0f);
                    SPUtil.getInstance().put(Constant.WEIGHT_ZHIZHANG2, 0f);
                } else if (mList.get(i).getType() == 3) {
                    SPUtil.getInstance().put(Constant.WEIGHT_BOLI_CUR, 0f);
                    SPUtil.getInstance().put(Constant.WEIGHT_BOLI, 0f);
                } else if (mList.get(i).getType() == 4) {
                    SPUtil.getInstance().put(Constant.WEIGHT_FANGZHI_CUR, 0f);
                    SPUtil.getInstance().put(Constant.WEIGHT_FANGZHI, 0f);
                } else if (mList.get(i).getType() == 5) {
                    SPUtil.getInstance().put(Constant.WEIGHT_JINSHU_CUR, 0f);
                    SPUtil.getInstance().put(Constant.WEIGHT_JINSHU, 0f);
                } else if (mList.get(i).getType() == 6) {
                    SPUtil.getInstance().put(Constant.WEIGHT_SULIAO_CUR, 0f);
                    SPUtil.getInstance().put(Constant.WEIGHT_SULIAO, 0f);
                }
            }
        }
        LogUtils.e("???????????????-----------" + isCloseComplete);
        if (!isCloseComplete) {
            registerSendRepeat("", false);
            registerSendRepeat("80010033B2", true);
        }
    }

    private int doorType = 1;

    @OnClick({R.id.mLayout_Back, R.id.mLayout_ZHIZHANG1, R.id.mLayout_ZHIZHANG2, R.id.mLayout_BOLI,
            R.id.mLayout_FANGZHI, R.id.mLayout_JINSHU, R.id.mLayout_SULIAO})
    public void onClick(View view) {
        if (Common.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.mLayout_Back:
                Common.openActivity(this, LoginActivity.class);
                stopTask();
                break;
            case R.id.mLayout_ZHIZHANG1:
                doorType = 1;
                checkDoor(doorType);
                break;
            case R.id.mLayout_ZHIZHANG2:
                doorType = 2;
                checkDoor(doorType);
                break;
            case R.id.mLayout_BOLI:
                doorType = 3;
                checkDoor(doorType);
                break;
            case R.id.mLayout_FANGZHI:
                doorType = 4;
                checkDoor(doorType);
                break;
            case R.id.mLayout_JINSHU:
                doorType = 5;
                checkDoor(doorType);
                break;
            case R.id.mLayout_SULIAO:
                doorType = 6;
                checkDoor(doorType);
                break;
        }
    }


    private void checkDoor(int doorType) {
        int num = mList.size();
        for (int i = 0; i < num; i++) {
            if (doorType == (i + 1)) {
                if (!mList.get(i).isClose()) {
                    ToastUtil.showShort("??????????????????");
                    return;
                }
            }
        }
        if(isCloseComplete){
            mRemainTime.clearCurTimer();
            mRemainTime.setLength(20 * 60 * 1000);
            mRemainTime.start();
        }
        switch (doorType) {
            case 1:
                sendMsg(checkXor("8A010211"));
                break;
            case 2:
                sendMsg(checkXor("8A010411"));
                break;
            case 3:
                sendMsg(checkXor("8A010611"));
                break;
            case 4:
                sendMsg(checkXor("8A010811"));
                break;
            case 5:
            case 6:
                sendMsg(checkXor("8A010A11"));
                break;
        }
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
                sendMsg_Weight(getZeroSendMsg());
            }
        }
    }

    public String getZeroSendMsg() {
        isPeel = false;

        String result = "";
        int num3 = mList.size();
        for (int i = 0; i < num3; i++) {
            if (mList.get(i).isCurClose()) {
                if (mList.get(i).getType() == 1) {
                    result = "01 06 00 24 00 00" + CRC16Util.getCrc("01 06 00 24 00 00");
                    break;
                } else if (mList.get(i).getType() == 2) {
                    result = "02 06 00 24 00 00" + CRC16Util.getCrc("02 06 00 24 00 00");
                    break;
                } else if (mList.get(i).getType() == 3) {
                    result = "03 06 00 24 00 00" + CRC16Util.getCrc("03 06 00 24 00 00");
                    break;
                } else if (mList.get(i).getType() == 4) {
                    result = "04 06 00 24 00 00" + CRC16Util.getCrc("04 06 00 24 00 00");
                    break;
                } else if (mList.get(i).getType() == 5 || mList.get(i).getType() == 6) {
                    result = "05 06 00 24 00 00" + CRC16Util.getCrc("05 06 00 24 00 00");
                    break;
                }
            }
        }
        return result.replace(" ", "");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        registerSendRepeat("", false);
        stopTask();
    }
}

