package com.chuxinbuer.deyiguanjia.http.observer;


import android.text.TextUtils;

import com.chuxinbuer.deyiguanjia.http.exception.ApiException;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.http.retrofit.HttpRequestListener;
import com.chuxinbuer.deyiguanjia.http.retrofit.RxActionManagerImpl;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;


/**
 * 适用Retrofit网络请求Observer(监听者)
 * 备注:
 * 1.重写onSubscribe，添加请求标识
 * 2.重写onError，封装错误/异常处理，移除请求
 * 3.重写onNext，移除请求
 * 4.重写cancel，取消请求
 */
public abstract class HttpRxObserver<T> implements Observer<T>, HttpRequestListener {

    private String mTag;//请求标识

    public HttpRxObserver() {
    }

    public HttpRxObserver(String tag) {
        this.mTag = tag;
    }

    @Override
    public void onError(Throwable e) {
        RxActionManagerImpl.getInstance().remove(mTag);
        if (e instanceof ApiException) {
            onError((ApiException) e, mTag);
        } else {
            onError(new ApiException(e, ExceptionEngine._CONNECT_EXCEPTION), mTag);
        }
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onNext(@NonNull T t) {
        if (!TextUtils.isEmpty(mTag)) {
            RxActionManagerImpl.getInstance().remove(mTag);
        }
        onSuccess(t, mTag);
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        if (!TextUtils.isEmpty(mTag)) {
            RxActionManagerImpl.getInstance().add(mTag, d);
        }
        onStart(d);
    }

    @Override
    public void cancel() {
        if (!TextUtils.isEmpty(mTag)) {
            RxActionManagerImpl.getInstance().cancel(mTag);
        }
    }

    /**
     * 是否已经处理
     *
     * @author ZhongDaFeng
     */
    public boolean isDisposed() {
        if (TextUtils.isEmpty(mTag)) {
            return true;
        }
        return RxActionManagerImpl.getInstance().isDisposed(mTag);
    }

    protected abstract void onStart(Disposable d);

    /**
     * 错误/异常回调
     *
     * @author ZhongDaFeng
     */
    protected abstract void onError(ApiException e, String mTag);

    /**
     * 成功回调
     *
     * @author ZhongDaFeng
     */
    protected abstract void onSuccess(T response, String mTag);

}
