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
public class PostBoxModel extends BaseModel {
    private boolean isCanUse = true;//快递箱是否可用
    private String size = "";//快递箱规格
    private String number = "";//快递箱编号

    public boolean isCanUse() {
        return isCanUse;
    }

    public void setCanUse(boolean canUse) {
        isCanUse = canUse;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}