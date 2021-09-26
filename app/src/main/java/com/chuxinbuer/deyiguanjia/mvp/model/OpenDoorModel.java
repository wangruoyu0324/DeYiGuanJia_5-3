/**
 * Copyright 2018 bejson.com
 */
package com.chuxinbuer.deyiguanjia.mvp.model;


/**
 * Auto-generated: 2018-02-23 17:30:44
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class OpenDoorModel extends BaseModel {
    private int type = 0;
    private boolean isClose = true;
    private boolean isCurClose = false;//是否本次关闭了这个门

    public boolean isCurClose() {
        return isCurClose;
    }

    public void setCurClose(boolean curClose) {
        isCurClose = curClose;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isClose() {
        return isClose;
    }

    public void setClose(boolean close) {
        isClose = close;
    }
}