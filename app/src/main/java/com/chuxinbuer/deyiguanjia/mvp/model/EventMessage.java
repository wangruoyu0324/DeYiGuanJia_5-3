package com.chuxinbuer.deyiguanjia.mvp.model;


/**
 * Created by Administrator on 2016/3/8 0008.
 */
public class EventMessage extends BaseModel {
    private String type = "";
    private String result = "";

    public EventMessage(String type, String result) {
        this.type = type;
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
