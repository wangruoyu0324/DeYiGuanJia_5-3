package com.chuxinbuer.deyiguanjia.http.retrofit;


import com.chuxinbuer.deyiguanjia.mvp.model.BaseModel;

/**
 * Created by wry on 2017/9/29.
 */

public class HttpResponseBody extends BaseModel {
    private String data;
    private String code;
    private String msg;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
