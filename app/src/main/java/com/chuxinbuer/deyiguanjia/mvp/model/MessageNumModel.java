package com.chuxinbuer.deyiguanjia.mvp.model;


import com.chuxinbuer.deyiguanjia.utils.Common;

/**
 * Created by Administrator on 2016/3/8 0008.
 */
public class MessageNumModel extends BaseModel {
    public UnreadNumModel dynamic = new UnreadNumModel();//动态未读消息条数
    public UnreadNumModel itr = new UnreadNumModel();//互动未读消息条数
    public UnreadNumModel msg = new UnreadNumModel();//公告未读消息条数
    public UnreadNumModel other = new UnreadNumModel();//系统通知消息条数

    public UnreadNumModel getDynamic() {
        return dynamic;
    }

    public void setDynamic(UnreadNumModel dynamic) {
        if (Common.empty(dynamic)) {
            dynamic = new UnreadNumModel();
        }
        this.dynamic = dynamic;
    }

    public UnreadNumModel getItr() {
        return itr;
    }

    public void setItr(UnreadNumModel itr) {
        if (Common.empty(itr)) {
            itr = new UnreadNumModel();
        }
        this.itr = itr;
    }

    public UnreadNumModel getMsg() {
        return msg;
    }

    public void setMsg(UnreadNumModel msg) {
        if (Common.empty(msg)) {
            msg = new UnreadNumModel();
        }
        this.msg = msg;
    }

    public UnreadNumModel getOther() {
        return other;
    }

    public void setOther(UnreadNumModel other) {
        if (Common.empty(other)) {
            other = new UnreadNumModel();
        }
        this.other = other;
    }
}
