package com.chuxinbuer.deyiguanjia.mvp.model;

public class KindModel extends BaseModel {
    private String title = "";
    private int resId = 0;
    private int backgroundId = 0;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
    }
}