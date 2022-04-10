package com.chuxinbuer.deyiguanjia.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Administrator on 2017/8/21 0021.
 */

public class CountDownButton extends AppCompatTextView implements View.OnClickListener {
    /*默认倒计时长*/
    private long countDownLength = 60 * 1000;

    /*未点击之前的显示的文字*/
    private String beforeText = "";

    /*倒计时结束后获取*/
    private String refreshText = "";
    /**
     * 开始执行计时的类，可以在每秒实行间隔任务
     */
    private Timer timer;
    /**
     * 在开始倒计时之后那个秒数数字之后所要显示的字，默认是秒
     */
    private String afterText = "";
    /**
     * 按钮点击事件
     */
    private OnClickListener onClickListener;

    /**
     * 每秒时间到了之后所执行的任务
     */
    private TimerTask timerTask;

    private Context context;

    public CountDownButton(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CountDownButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CountDownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        if (!TextUtils.isEmpty(getText())) {
            beforeText = getText().toString().trim();
        }
        this.setText(beforeText);
        this.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    /**
     * 设置监听按钮点击事件
     *
     * @param onclickListener
     */
    @Override
    public void setOnClickListener(OnClickListener onclickListener) {
        if (onclickListener instanceof CountDownButton) {
            super.setOnClickListener(onclickListener);
        } else {
            this.onClickListener = onclickListener;
        }
    }

    /**
     * 开始倒计时
     */

    private boolean isShowSecond = true;

    public void start() {
        isShowSecond = false;
        initTimer();
        this.setText("倒计时：" + countDownLength / 1000 + "S");
        this.setEnabled(false);
        timer.schedule(timerTask, 0, 1000);
    }

    /**
     * 初始化时间
     */
    private void initTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                } while (isPause);
                if (handler != null) {
                    handler.sendEmptyMessage(1);
                }
            }
        };
    }

    /**
     * 设置倒计时时长
     *
     * @param length 默认毫秒
     */
    public void setLength(long length) {
        this.countDownLength = length;
    }

    /**
     * 设置未点击时显示的文字
     *
     * @param beforeText
     */
    public void setBeforeText(String beforeText) {
        this.beforeText = beforeText;
    }

    /**
     * 设置未点击后显示的文字
     *
     * @param afterText
     */
    public void setAfterText(String afterText) {
        this.afterText = afterText;
    }

    /**
     * 更新显示的文本
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CountDownButton.this.setText("倒计时：" + countDownLength / 1000 + "S");
            countDownLength -= 1000;
//            LogUtils.e("倒计时:--------"+countDownLength);
            if (countDownLength < 0) {
                CountDownButton.this.setEnabled(true);
                CountDownButton.this.setText(refreshText);
                clearTimer();
                countDownLength = 60 * 1000;

                if (mOnFinishTimeClick != null) {
                    mOnFinishTimeClick.onFinishTimeClick();
                }
            }
        }
    };


    public interface OnFinishTimeClick {
        void onFinishTimeClick();
    }

    public void setOnFinishTimeClick(OnFinishTimeClick onFinishTimeClick) {
        this.mOnFinishTimeClick = onFinishTimeClick;
    }

    private OnFinishTimeClick mOnFinishTimeClick;


    /**
     * 清除倒计时
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
            handler.removeMessages(1);
            handler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 在界面销毁后调用，否则有内存泄漏风险
     */
    @Override
    protected void onDetachedFromWindow() {
        clearTimer();
        super.onDetachedFromWindow();
    }

    public void clearCurTimer() {
        clearTimer();
    }

    public void pause() {
        isPause = true;
    }

    public void resume() {
        isPause = false;
    }

    private boolean isPause = false;
}
