/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.chuxinbuer.deyiguanjia.database;

import android.content.Context;

import com.chuxinbuer.deyiguanjia.utils.Common;

import java.io.Serializable;

/**
 * app全局参数设置
 */
public class AppConfigPB extends PreferenceBeanHelper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String TOKEN = "token";//账号
    private String token = "";
    public static final String DEVICE_TOKEN = "device_token";
    private String device_token = "";
    public static final String REFUSE_TYPE = "refuse_type";
    private int refuse_type = 0;
    public static final String IS_MANAGER = "is_manager";
    private boolean is_manager = false;
    public static final String IS_OPENDOOR = "is_opendoor";
    private boolean is_opendoor = false;
    private boolean is_chuyurefusebox_one = true;
    public static final String IS_WEIGHTING = "is_weighting";
    private boolean is_weighting = true;
    public static final String BANNERPOSITION = "bannerposition";
    private int bannerposition = 0;
    public static final String BANNERPOSITION_CUR = "bannerposition_cur";
    private int bannerposition_cur = 0;
    public static final String VIDEO_POSITION = "video_position";
    private int video_position = 0;
    public static final String VIDEOTIME_START = "videotime_start";
    private int videotime_start = 0;
    public static final String VIDEOTIME_END = "videotime_end";
    private int videotime_end = 0;
    public static final String WEIGHTLIMIT_ZHIZHANG1 = "weightlimit_zhizhang1";
    private float weightlimit_zhizhang1 = 40000;
    public static final String WEIGHTLIMIT_ZHIZHANG2 = "weightlimit_zhizhang2";
    private float weightlimit_zhizhang2 = 40000;
    public static final String WEIGHTLIMIT_BOLI = "weightlimit_boli";
    private float weightlimit_boli = 40000;
    public static final String WEIGHTLIMIT_FANGZHI = "weightlimit_fangzhi";
    private float weightlimit_fangzhi = 40000;
    public static final String WEIGHTLIMIT_JINSHU = "weightlimit_jinshu";
    private float weightlimit_jinshu = 40000;
    public static final String WEIGHTLIMIT_SULIAO = "weightlimit_suliao";
    private float weightlimit_suliao = 40000;
    public static final String WEIGHTLIMIT_CHUYU1 = "weightlimit_chuyu1";
    private float weightlimit_chuyu1 = 40000;
    public static final String WEIGHTLIMIT_CHUYU2 = "weightlimit_chuyu2";
    private float weightlimit_chuyu2 = 40000;
    public static final String WEIGHTLIMIT_DUHAI = "weightlimit_duhai";
    private float weightlimit_duhai = 30000;
    public static final String WEIGHTLIMIT_OTHER1 = "weightlimit_other1";
    private float weightlimit_other1 = 30000;
    public static final String WEIGHTLIMIT_OTHER2 = "weightlimit_other2";
    private float weightlimit_other2 = 30000;
    public static final String OVERTIME = "overtime";
    private int overtime = 30;
    public static final String VIDEO_INTERVAL = "video_interval";
    private int video_interval = 60;
    public static final String RATE_ZHIZHANG = "rate_zhizhang";
    private float rate_zhizhang = 0.1f;
    public static final String RATE_BOLI = "rate_boli";
    private float rate_boli = 0.01f;
    public static final String RATE_FANGZHI = "rate_fangzhi";
    private float rate_fangzhi = 0.1f;
    public static final String RATE_JINSHU = "rate_jinshu";
    private float rate_jinshu = 0.1f;
    public static final String RATE_SULIAO = "rate_suliao";
    private float rate_suliao = 0.1f;
    public static final String RATE_CHUYU = "rate_chuyu";
    private float rate_chuyu = 0.004f;
    public static final String RATE_DUHAI = "rate_duhai";
    private float rate_duhai = 0.004f;
    public static final String RATE_OTHER = "rate_other";
    private float rate_other = 0.004f;
    public static final String FACE_SIMILAR = "face_similar";
    private float face_similar = 0.75f;
    public static final String CATE_BACKGROUND = "cate_background";
    private String cate_background = "";
    public static final String VOICE_VIDEO = "voice_video";
    private float voice_video = 0;
    public static final String VOICE_BREADCOST = "voice_breadcost";
    private float voice_breadcost = 0;
    public static final String LAST_REQUESTTIME = "last_requesttime";//人脸数据请求时间
    private String last_requesttime = "";

    public String getLast_requesttime() {
        return last_requesttime;
    }

    public void setLast_requesttime(String last_requesttime) {
        this.last_requesttime = last_requesttime;
    }

    public static final String LAST_REQUESTTIME_MANAGER = "last_requesttime_manager";//管理员人脸数据请求时间
    private String last_requesttime_manager = "";

    public String getLast_requesttime_manager() {
        return last_requesttime_manager;
    }

    public void setLast_requesttime_manager(String last_requesttime_manager) {
        this.last_requesttime_manager = last_requesttime_manager;
    }

    public void init(Context context) {
        super.init(context);
        try {
            loadFromPref();
        } catch (Exception e) {
            LogFactory.createLog().error(e);
        }
    }

    public void initNoSync(Context context) {
        super.init(context);
    }

    public boolean isDataInvalid() {
        if (Common.empty(this.token)) {
            return true;
        } else
            return false;
    }

    public void setDataInvalid() {//设置本地数据无效，清除本地数据（退出账号时）
        this.token = "";
        try {
            clearPref();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateAll() {//更新所有本地数据
        try {
            super.updatePreferAll();
        } catch (Exception e) {
            LogFactory.createLog().error(e);
        }
    }

    public float getVoice_video() {
        return voice_video;
    }

    public void setVoice_video(float voice_video) {
        this.voice_video = voice_video;
    }

    public float getVoice_breadcost() {
        return voice_breadcost;
    }

    public void setVoice_breadcost(float voice_breadcost) {
        this.voice_breadcost = voice_breadcost;
    }

    public String getCate_background() {
        return cate_background;
    }

    public void setCate_background(String cate_background) {
        this.cate_background = cate_background;
    }

    public float getFace_similar() {
        return face_similar;
    }

    public void setFace_similar(float face_similar) {
        this.face_similar = face_similar;
    }

    public int getVideo_interval() {
        return video_interval;
    }

    public void setVideo_interval(int video_interval) {
        this.video_interval = video_interval;
    }

    public float getRate_zhizhang() {
        return rate_zhizhang;
    }

    public void setRate_zhizhang(float rate_zhizhang) {
        this.rate_zhizhang = rate_zhizhang;
    }

    public float getRate_boli() {
        return rate_boli;
    }

    public void setRate_boli(float rate_boli) {
        this.rate_boli = rate_boli;
    }

    public float getRate_fangzhi() {
        return rate_fangzhi;
    }

    public void setRate_fangzhi(float rate_fangzhi) {
        this.rate_fangzhi = rate_fangzhi;
    }

    public float getRate_jinshu() {
        return rate_jinshu;
    }

    public void setRate_jinshu(float rate_jinshu) {
        this.rate_jinshu = rate_jinshu;
    }

    public float getRate_suliao() {
        return rate_suliao;
    }

    public void setRate_suliao(float rate_suliao) {
        this.rate_suliao = rate_suliao;
    }

    public float getRate_chuyu() {
        return rate_chuyu;
    }

    public void setRate_chuyu(float rate_chuyu) {
        this.rate_chuyu = rate_chuyu;
    }

    public float getRate_duhai() {
        return rate_duhai;
    }

    public void setRate_duhai(float rate_duhai) {
        this.rate_duhai = rate_duhai;
    }

    public float getRate_other() {
        return rate_other;
    }

    public void setRate_other(float rate_other) {
        this.rate_other = rate_other;
    }

    public int getOvertime() {
        return overtime;
    }

    public void setOvertime(int overtime) {
        this.overtime = overtime;
    }

    public float getWeightlimit_zhizhang1() {
        return weightlimit_zhizhang1;
    }

    public void setWeightlimit_zhizhang1(float weightlimit_zhizhang1) {
        this.weightlimit_zhizhang1 = weightlimit_zhizhang1;
    }

    public float getWeightlimit_zhizhang2() {
        return weightlimit_zhizhang2;
    }

    public void setWeightlimit_zhizhang2(float weightlimit_zhizhang2) {
        this.weightlimit_zhizhang2 = weightlimit_zhizhang2;
    }

    public float getWeightlimit_boli() {
        return weightlimit_boli;
    }

    public void setWeightlimit_boli(float weightlimit_boli) {
        this.weightlimit_boli = weightlimit_boli;
    }

    public float getWeightlimit_fangzhi() {
        return weightlimit_fangzhi;
    }

    public void setWeightlimit_fangzhi(float weightlimit_fangzhi) {
        this.weightlimit_fangzhi = weightlimit_fangzhi;
    }

    public float getWeightlimit_jinshu() {
        return weightlimit_jinshu;
    }

    public void setWeightlimit_jinshu(float weightlimit_jinshu) {
        this.weightlimit_jinshu = weightlimit_jinshu;
    }

    public float getWeightlimit_suliao() {
        return weightlimit_suliao;
    }

    public void setWeightlimit_suliao(float weightlimit_suliao) {
        this.weightlimit_suliao = weightlimit_suliao;
    }

    public float getWeightlimit_chuyu1() {
        return weightlimit_chuyu1;
    }

    public void setWeightlimit_chuyu1(float weightlimit_chuyu1) {
        this.weightlimit_chuyu1 = weightlimit_chuyu1;
    }

    public float getWeightlimit_chuyu2() {
        return weightlimit_chuyu2;
    }

    public void setWeightlimit_chuyu2(float weightlimit_chuyu2) {
        this.weightlimit_chuyu2 = weightlimit_chuyu2;
    }

    public float getWeightlimit_duhai() {
        return weightlimit_duhai;
    }

    public void setWeightlimit_duhai(float weightlimit_duhai) {
        this.weightlimit_duhai = weightlimit_duhai;
    }

    public float getWeightlimit_other1() {
        return weightlimit_other1;
    }

    public void setWeightlimit_other1(float weightlimit_other1) {
        this.weightlimit_other1 = weightlimit_other1;
    }

    public float getWeightlimit_other2() {
        return weightlimit_other2;
    }

    public void setWeightlimit_other2(float weightlimit_other2) {
        this.weightlimit_other2 = weightlimit_other2;
    }

    public int getVideotime_start() {
        return videotime_start;
    }

    public void setVideotime_start(int videotime_start) {
        this.videotime_start = videotime_start;
    }

    public int getVideotime_end() {
        return videotime_end;
    }

    public void setVideotime_end(int videotime_end) {
        this.videotime_end = videotime_end;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public int getRefuse_type() {
        return refuse_type;
    }

    public void setRefuse_type(int refuse_type) {
        this.refuse_type = refuse_type;
    }

    public boolean isIs_manager() {
        return is_manager;
    }

    public void setIs_manager(boolean is_manager) {
        this.is_manager = is_manager;
    }


    public boolean isIs_opendoor() {
        return is_opendoor;
    }

    public void setIs_opendoor(boolean is_opendoor) {
        this.is_opendoor = is_opendoor;
    }

    public boolean isIs_chuyurefusebox_one() {
        return is_chuyurefusebox_one;
    }

    public void setIs_chuyurefusebox_one(boolean is_chuyurefusebox_one) {
        this.is_chuyurefusebox_one = is_chuyurefusebox_one;
    }

    public boolean isIs_weighting() {
        return is_weighting;
    }

    public void setIs_weighting(boolean is_weighting) {
        this.is_weighting = is_weighting;
    }

    public int getBannerposition() {
        return bannerposition;
    }

    public void setBannerposition(int bannerposition) {
        this.bannerposition = bannerposition;
    }

    public int getBannerposition_cur() {
        return bannerposition_cur;
    }

    public void setBannerposition_cur(int bannerposition_cur) {
        this.bannerposition_cur = bannerposition_cur;
    }

    public int getVideo_position() {
        return video_position;
    }

    public void setVideo_position(int video_position) {
        this.video_position = video_position;
    }
}
