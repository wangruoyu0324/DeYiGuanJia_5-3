package com.chuxinbuer.deyiguanjia.http.observer;

import com.alibaba.fastjson.JSON;
import com.chuxinbuer.deyiguanjia.http.function.HttpResultFunction;
import com.chuxinbuer.deyiguanjia.http.function.ServerResultFunction;
import com.chuxinbuer.deyiguanjia.http.retrofit.HttpResponseBody;
import com.chuxinbuer.deyiguanjia.utils.LogUtils;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 适用Retrofit网络请求Observable(被监听者)
 *
 */
public class HttpRxObservable {

    /**
     * 获取被监听者
     * 备注:网络请求Observable构建
     * data:网络请求参数
     * <h1>补充说明</h1>
     * 无管理生命周期,容易导致内存溢出
     *
     * @author ZhongDaFeng
     */
    public static Observable getObservable(Observable<HttpResponseBody> apiObservable, String url) {
        // showLog(request);
        Observable observable = apiObservable
                .map(new ServerResultFunction(url))
                .onErrorResumeNext(new HttpResultFunction<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        return observable;
    }


    /**
     * 获取被监听者
     * 备注:网络请求Observable构建
     * data:网络请求参数
     * <h1>补充说明</h1>
     * 传入LifecycleProvider自动管理生命周期,避免内存溢出
     * 备注:需要继承RxActivity.../RxFragment...
     *
     * @author ZhongDaFeng
     */
    public static Observable getObservable(Observable<HttpResponseBody> apiObservable, LifecycleProvider lifecycle,String url) {
        //showLog(request);
        Observable observable;

        if (lifecycle != null) {
            //随生命周期自动管理.eg:onCreate(start)->onStop(end)
            observable =apiObservable
                    .map(new ServerResultFunction(url))
                    .compose(lifecycle.bindToLifecycle())//需要在这个位置添加
                    .onErrorResumeNext(new HttpResultFunction<>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            observable = getObservable(apiObservable,url);
        }
        return observable;
    }

    /**
     * 获取被监听者
     * 备注:网络请求Observable构建
     * data:网络请求参数
     * <h1>补充说明</h1>
     * 传入LifecycleProvider<ActivityEvent>手动管理生命周期,避免内存溢出
     * 备注:需要继承RxActivity,RxAppCompatActivity,RxFragmentActivity
     *
     * @author ZhongDaFeng
     */
    public static Observable getObservable(Observable<HttpResponseBody> apiObservable, LifecycleProvider<ActivityEvent> lifecycle,
                                           ActivityEvent event, String url) {
        // showLog(request);
        Observable observable;
        if (lifecycle != null) {
            //手动管理移除监听生命周期.eg:ActivityEvent.STOP
            observable = apiObservable
                    .map(new ServerResultFunction(url))
                    .compose(lifecycle.bindUntilEvent(event))//需要在这个位置添加
                    .onErrorResumeNext(new HttpResultFunction<>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            observable = getObservable(apiObservable,url);
        }
        return observable;
    }


    /**
     * 获取被监听者
     * 备注:网络请求Observable构建
     * data:网络请求参数
     * <h1>补充说明</h1>
     * 传入LifecycleProvider<FragmentEvent>手动管理生命周期,避免内存溢出
     * 备注:需要继承RxFragment,RxDialogFragment
     *
     * @author ZhongDaFeng
     */
    public static Observable getObservable(Observable<HttpResponseBody> apiObservable, LifecycleProvider<FragmentEvent> lifecycle,
                                           FragmentEvent event, String url) {
        //  showLog(request);
        Observable observable;
        if (lifecycle != null) {
            //手动管理移除监听生命周期.eg:FragmentEvent.STOP
            observable = apiObservable
                    .map(new ServerResultFunction(url))
                    .compose(lifecycle.bindUntilEvent(event))//需要在这个位置添加
                    .onErrorResumeNext(new HttpResultFunction<>())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            observable = getObservable(apiObservable,url);
        }
        return observable;
    }


    /**
     * 打印log
     *
     * @author ZhongDaFeng
     */
    private static void showLog(Map<String, Object> request) {
        if (request == null || request.size() == 0) {
            LogUtils.e("[http request]:");
        }
        LogUtils.e("[http request]:" + JSON.toJSONString(request));
    }

}
