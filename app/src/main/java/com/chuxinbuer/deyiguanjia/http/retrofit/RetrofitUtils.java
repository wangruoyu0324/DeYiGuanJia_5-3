package com.chuxinbuer.deyiguanjia.http.retrofit;

import com.chuxinbuer.deyiguanjia.MyApplication;
import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.chuxinbuer.deyiguanjia.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

/**
 * RetrofitUtils工具类
 */
public class RetrofitUtils {
    /**
     * 接口地址
     */
    public static final int CONNECT_TIME_OUT = 30;//连接超时时长x秒
    public static final int READ_TIME_OUT = 30;//读数据超时时长x秒
    public static final int WRITE_TIME_OUT = 30;//写数据接超时时长x秒

    private static boolean isDebug = true;//测试模式打开log

    private static RetrofitUtils mInstance = null;

    private RetrofitUtils() {
    }

    public static RetrofitUtils get() {
        if (mInstance == null) {
            synchronized (RetrofitUtils.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitUtils();
                }
            }
        }
        return mInstance;
    }

    /**
     * 设置okHttp
     *
     */
    private static OkHttpClient okHttpClient() {
        //缓存机制  无网络时，也能显示数据。
        File cacheFile = new File(MyApplication.mContext.getExternalCacheDir(), Constant.OKHTTP_CACHE_DIR);
        Cache cache = new Cache(cacheFile, Constant.MAX_CACHE_SIZE_INBYTES);
        Interceptor cacheInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                if (!Common.isNetWorkConnected(MyApplication.mContext)) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                Response response = chain.proceed(request);
                if (Common.isNetWorkConnected(MyApplication.mContext)) {
                    int maxAge = 0;
                    // 有网络时 设置缓存超时时间0个小时
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                            .build();
                } else {
                    // 无网络时，设置超时为4周
                    int maxStale = 60 * 60 * 24 * 28;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }
                return response;
            }
        };
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                //开启Log
                if (isDebug) {
                    LogUtils.e("okHttp:" + message);
                }
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true) //错误重连
                .cache(cache).addInterceptor(cacheInterceptor)//网络缓存
                .addInterceptor(logging)
                .build();
        return client;
    }

    /**
     * 获取Retrofit
     *
     */
    public Retrofit retrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient())
                .baseUrl(Constant.BASE_URL)
                .build();
        return retrofit;
    }

}
