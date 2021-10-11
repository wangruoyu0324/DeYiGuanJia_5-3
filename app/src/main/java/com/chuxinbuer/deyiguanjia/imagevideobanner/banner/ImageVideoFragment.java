package com.chuxinbuer.deyiguanjia.imagevideobanner.banner;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.database.AppConfigManager;
import com.chuxinbuer.deyiguanjia.database.AppConfigPB;
import com.chuxinbuer.deyiguanjia.fresco.FrescoUtil;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.imagevideobanner.PlayBannerActivity;
import com.chuxinbuer.deyiguanjia.imagevideobanner.bean.BannerBean;
import com.chuxinbuer.deyiguanjia.mvp.model.ConfigModel;
import com.chuxinbuer.deyiguanjia.mvp.model.EventMessage;
import com.chuxinbuer.deyiguanjia.mvp.presenter.HttpsPresenter;
import com.chuxinbuer.deyiguanjia.mvp.view.iface.IBaseView;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.chuxinbuer.deyiguanjia.utils.LogUtils;
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by jay on 2018/6/5.
 */

public class ImageVideoFragment extends Fragment implements IBaseView {

    private static final String TAG = ImageVideoFragment.class.getSimpleName();
    private OnVideoCompletionListener listener;
    private VideoView mVideoView;
    private RelativeLayout mVideoViewContainer;
    private BannerBean bannerBean;
    private int curPosition = 0;
    private FrameLayout waitLoading;
    private boolean playerPaused;
    private String mUrl;
    private final int STOP_PLAYER = 0x2000;
    private final int START_PLAYER = 0x2001;
    private final int PAUSE_PLAYER = 0x2002;
    private final int SET_VIDEO_URL = 0x2003;


    //手势调节音量的大小
    protected int mGestureDownVolume;
    /**
     * 使用Handler是为了避免出现ANR异常
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_PLAYER:
                    stopPlayer();
                    break;
                case START_PLAYER:
                    startPlayer();
                    break;
                case PAUSE_PLAYER:
                    pausePlayer();
                    break;
                case SET_VIDEO_URL:
                    setVideoUrl();
                    startPlayer();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        bannerBean = (BannerBean) bundle.getSerializable("bannerBean");
        curPosition = bundle.getInt("position", 0);
        Log.e(TAG, "type=" + bannerBean.getType() + ",url=" + bannerBean.getUrl());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;
        if (bannerBean != null) {
            int type = bannerBean.getType();
            if (type == 0) {
                if (!Common.empty(AppConfigManager.getInitedAppConfig().getBannerposition_cur())) {
                    try {
                        AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION_CUR, 0);
                        AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VIDEO_POSITION, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!((PlayBannerActivity) getActivity()).isFinishing()) {
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.item_image_view, container, false);
                    RelativeLayout mRootView = view.findViewById(R.id.mRootView);
                    SimpleDraweeView imageView = view.findViewById(R.id.iv);
                    FrescoUtil.display(imageView, new File(bannerBean.getUrl()));
                    mRootView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            try {
                                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION_CUR, curPosition);
                                AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION, curPosition + AppConfigManager.getInitedAppConfig().getBannerposition());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            EventBus.getDefault().post(new EventMessage("image", ""));
                            return false;
                        }
                    });
                }
//                imageView.setImageBitmap(BitmapFactory.decodeFile(bannerBean.getUrl()));
            } else {
                if (!((PlayBannerActivity) getActivity()).isFinishing()) {
                    if (mVideoViewContainer != null) {
                        mVideoViewContainer.removeAllViews();
                    }
                    view = LayoutInflater.from(getActivity()).inflate(R.layout.item_video_view, container, false);
                    mVideoViewContainer = view.findViewById(R.id.video_view_container);
                    if (mVideoView == null) {
                        mVideoView = new VideoView(getActivity().getApplicationContext());
                        mVideoViewContainer.addView(mVideoView);
                    }
                    waitLoading = view.findViewById(R.id.wait_loading_layout);
                }
                initData();
            }
        } else {
            if (!((PlayBannerActivity) getActivity()).isFinishing()) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.item_image_view2, container, false);
            }
        }
        return view;
    }

    private void initData() {
        if (null != mVideoView) {
            sendSetVideoUrlMsg();
//            mVideoView.setMediaController(new MediaController(getActivity()));
            mVideoView.requestFocus();
            mVideoView.setVideoURI(Uri.parse(bannerBean.getUrl()));
            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mVideoView != null) {
                        mVideoView.stopPlayback();
                        if (null != listener) {
                            listener.onVideoCompletion(mp);
                        }
                    }
                }
            });

            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (!Common.empty(AppConfigManager.getInitedAppConfig().getVideotime_start()) && !Common.empty(AppConfigManager.getInitedAppConfig().getVideotime_end())) {
                        if (System.currentTimeMillis() / 1000 > AppConfigManager.getInitedAppConfig().getVideotime_start() && System.currentTimeMillis() / 1000 < AppConfigManager.getInitedAppConfig().getVideotime_end()) {
                            int max = ((PlayBannerActivity) getActivity()).getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                            ((PlayBannerActivity) getActivity()).getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, (int) (max * AppConfigManager.getInitedAppConfig().getVoice_video()), 0);
//                            ((PlayBannerActivity) getActivity()).getAudioManager().setStreamMute(AudioManager.STREAM_MUSIC, false);
                        } else {
                            ((PlayBannerActivity) getActivity()).getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC,0, 0);
//                            ((PlayBannerActivity) getActivity()).getAudioManager().setStreamMute(AudioManager.STREAM_MUSIC, true);
                        }
                    } else {
                        ((PlayBannerActivity) getActivity()).getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
//                        ((PlayBannerActivity) getActivity()).getAudioManager().setStreamMute(AudioManager.STREAM_MUSIC, true);
                    }
                    if (mVideoView != null) {
                        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                            @Override
                            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                if (mVideoView != null) {
                                    waitLoading.setVisibility(View.GONE);
                                    mVideoView.setVisibility(View.VISIBLE);
                                }
                                LogUtils.e("total=" + mVideoView.getDuration());
                                try {
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VIDEO_POSITION, 0);
                                    AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION_CUR, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return false;
                            }
                        });
                    }

                    Map<String, String> map = new HashMap<>();

                    new HttpsPresenter(ImageVideoFragment.this, (PlayBannerActivity) getActivity()).request(map, Constant.GETCONFATTR, false);
                }
            });

            mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e(TAG, "视频播放出错了-what=" + what + ",extra=" + extra);
                    mVideoView.stopPlayback();
                    if (null != listener) {
                        listener.onError(mp);
                    }
                    if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
                        //媒体服务器挂掉了。此时，程序必须释放MediaPlayer 对象，并重新new 一个新的。
                        ToastUtil.showLong("媒体服务器挂掉了");
                    } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                        if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                            //文件不存在或错误，或网络不可访问错误
                            ToastUtil.showLong("文件不存在或错误，或网络不可访问错误");
                        } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
                            //超时
                            ToastUtil.showLong("超时");
                        }
                    }
                    return true;
                }
            });
//            startPlayer();

            mVideoViewContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (null != mVideoView) {
                        mVideoView.stopPlayback();
                        try {
                            AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.VIDEO_POSITION, mVideoView.getCurrentPosition());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION_CUR, curPosition);
                        AppConfigManager.getInitedAppConfig().updatePrefer(AppConfigPB.BANNERPOSITION, curPosition + AppConfigManager.getInitedAppConfig().getBannerposition());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    EventBus.getDefault().post(new EventMessage("video", ""));
                    return false;
                }
            });
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView.setOnTouchListener(null);
            mVideoView.setOnCompletionListener(null);
            mVideoView.setOnErrorListener(null);
            mVideoView.setOnPreparedListener(null);
        }
    }

    private void setVideoUrl() {
        String url = bannerBean.getUrl();
        mUrl = url;
        //播放本地视频
        mVideoView.setVideoURI(Uri.parse(url));
    }

    public void startPlayer() {
        if (null != mVideoView) {
            mVideoView.start();
        }
    }

    public void circulationPlayer() {
        /*if (null != mVideoView) {
            mVideoView.setVideoPath(bannerBean.getUrl());
            mVideoView.start();
        }*/
        sendStartVideoMsg(true);
    }

    private void stopPlayer() {
        if (null != mVideoView) {
            mVideoView.stopPlayback();
            handler.removeCallbacksAndMessages(null);
        }
    }

    public boolean isPlaying() {
        if (null != mVideoView) {
            return mVideoView.isPlaying();
        }
        return false;
    }

    private void pausePlayer() {
        if (null != mVideoView) {
            playerPaused = true;
            mVideoView.pause();
        }
    }

    private void sendStartVideoMsg() {
        sendStartVideoMsg(false);
    }

    private void sendStartVideoMsg(boolean isHasUrl) {
        if (!handler.hasMessages(START_PLAYER)) {
            if (null != mVideoView) {
                if (isHasUrl) {
                    try {
                        mVideoView.setVideoURI(Uri.parse(mUrl));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                handler.sendEmptyMessage(START_PLAYER);
            }
        }
    }

//    private void sendStopVideoMsg() {
//        if (!handler.hasMessages(STOP_PLAYER)) {
//            if (null != mVideoView) {
//                handler.sendEmptyMessage(STOP_PLAYER);
//            }
//        }
//    }

    private void sendPauseVideoMsg() {
        if (!handler.hasMessages(PAUSE_PLAYER)) {
            if (null != mVideoView) {
                handler.sendEmptyMessage(PAUSE_PLAYER);
            }
        }
    }

    private void sendSetVideoUrlMsg() {
        if (!handler.hasMessages(SET_VIDEO_URL)) {
            if (null != mVideoView) {
                Log.e(TAG, "sendSetVideoUrlMsg------");
                handler.sendEmptyMessage(SET_VIDEO_URL);
            }
        }
    }

    private void removeMessages() {
        if (handler.hasMessages(START_PLAYER)) {
            handler.removeMessages(START_PLAYER);
        }
        if (handler.hasMessages(STOP_PLAYER)) {
            handler.removeMessages(STOP_PLAYER);
        }
        if (handler.hasMessages(PAUSE_PLAYER)) {
            handler.removeMessages(PAUSE_PLAYER);
        }
        if (handler.hasMessages(SET_VIDEO_URL)) {
            handler.removeMessages(SET_VIDEO_URL);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        sendStopVideoMsg();
        removeMessages();

        mVideoView = null;
        if (mVideoViewContainer != null)
            mVideoViewContainer.removeAllViews();
        Log.e(TAG, "onDestroy=" + bannerBean.getUrl());
    }

    @Override
    public void showResult(String status, String pRows, String url) {
        if (status.equals(ExceptionEngine._SUCCESS)) {
            if (url.equals(Constant.GETCONFATTR)) {
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
            }
        }
    }

    public interface OnVideoCompletionListener {
        void onVideoCompletion(MediaPlayer mp);

        void onError(MediaPlayer mp);

    }

    public void setOnVideoCompletionListener(OnVideoCompletionListener listener) {
        this.listener = listener;
    }
}
