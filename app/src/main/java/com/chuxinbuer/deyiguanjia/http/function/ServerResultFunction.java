package com.chuxinbuer.deyiguanjia.http.function;


import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.http.exception.ServerException;
import com.chuxinbuer.deyiguanjia.http.retrofit.HttpResponseBody;
import com.chuxinbuer.deyiguanjia.utils.Common;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 服务器结果处理函数
 */
public class ServerResultFunction implements Function<HttpResponseBody, Object> {
    private String mTag;//请求地址标识

    public ServerResultFunction(String url) {
        mTag = url;
    }

    @Override
    public Object apply(@NonNull HttpResponseBody response) throws Exception {
        //打印服务器回传结果
        if (!response.getCode().equals(ExceptionEngine._SUCCESS)) {
            throw new ServerException(response.getCode(), response.getMsg());
        }
        if (Common.empty(response.getData())) {
            return "";//返回空对象
        } else {
            String result = response.getData();
            return result;//返回对象
        }
    }
}
