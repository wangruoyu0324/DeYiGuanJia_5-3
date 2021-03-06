package com.chuxinbuer.deyiguanjia.http.exception;

/**
 * 自定义服务器错误
 *
 */
public class ServerException extends RuntimeException {
    private String code;
    private String msg;

    public ServerException(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
