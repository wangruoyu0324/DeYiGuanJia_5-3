package com.chuxinbuer.deyiguanjia.mvp.view.iface;

/**
 * IBaseView
 */

public interface IBaseView {
    /**
     * status 请求返回状态码
     * pRows  请求返回解密后的数据
     * url    请求返回解密后的数据
     */
    void showResult(String status, String pRows, String url);
}
