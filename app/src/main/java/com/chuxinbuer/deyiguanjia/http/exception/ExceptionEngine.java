package com.chuxinbuer.deyiguanjia.http.exception;

import android.util.MalformedJsonException;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.ParseException;

import retrofit2.HttpException;


/**
 * 错误/异常处理工具
 *
 */
public class ExceptionEngine {

    public static final String _SUCCESS = "200";//操作成功（只有返回 CX00F 才表示操作成功）
    public static final String _FAIL = "CX00C";//操作失败！证书签名验证失败
    public static final String _ERRORMSG = "CX00A";//操作失败！输出错误信息
    public static final String _PARAMSERROR = "CX00P";//操作失败！请求参数不完整
    public static final String _CONNECT_FAIL = "CXAAA";//连接服务器失败
    public static final String _CONNECT_TIMEOUT = "CXBBB";//连接服务器超时
    public static final String _CONNECT_EXCEPTION = "CXCCC";//连接服务器异常
    public static final String _ANALYTIC_SERVER_DATA_ERROR = "CXDDD";//解析(服务器)数据错误
    public static final String _TO_LOGIN = "CX00E";//账号在别处登录或者账号不存在

    public static ApiException handleException(Throwable e) {
        ApiException ex;
        if (e instanceof HttpException) {             //HTTP错误
            HttpException httpExc = (HttpException) e;
            ex = new ApiException(e, httpExc.code()+"");
            if(httpExc.code() == 500){
                ex.setMsg("服务器内部错误");
            }else if(httpExc.code() == 501){
                ex.setMsg("服务器不具备完成请求的功能");
            }else if(httpExc.code() == 502){
                ex.setMsg("错误网关");
            }else if(httpExc.code() == 503){
                ex.setMsg("服务不可用");
            }else if(httpExc.code() == 504){
                ex.setMsg("网关超时");
            }else if(httpExc.code() == 505){
                ex.setMsg("HTTP 版本不受支持");
            }else{
                ex.setMsg(httpExc.message());  //均视为网络错误
            }
            return ex;
        } else if (e instanceof ServerException) {    //服务器返回的错误
            ServerException serverExc = (ServerException) e;
            ex = new ApiException(serverExc, serverExc.getCode());
            ex.setMsg(serverExc.getMsg());
            return ex;
        } else if (e instanceof com.alibaba.fastjson.JSONException
                || e instanceof JSONException
                || e instanceof ParseException || e instanceof MalformedJsonException) {  //解析数据错误
            ex = new ApiException(e, _ANALYTIC_SERVER_DATA_ERROR);
            ex.setMsg("解析错误");
            return ex;
        } else if (e instanceof ConnectException) {//连接网络错误
            ex = new ApiException(e, _CONNECT_FAIL);
            ex.setMsg("连接失败");
            return ex;
        } else if (e instanceof SocketTimeoutException) {//网络超时
            ex = new ApiException(e, _CONNECT_TIMEOUT);
            ex.setMsg("网络超时");
            return ex;
        } else {  //未知错误
            ex = new ApiException(e, _CONNECT_EXCEPTION);
            ex.setMsg("未知错误");
            return ex;
        }
    }

}
