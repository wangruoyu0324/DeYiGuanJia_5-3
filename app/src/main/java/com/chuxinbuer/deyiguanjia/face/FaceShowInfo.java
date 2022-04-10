package com.chuxinbuer.deyiguanjia.face;

import android.graphics.Bitmap;

public class FaceShowInfo {
    private long id;
    private String name;
    private Bitmap imgData;
    private float simi;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImgData() {
        return imgData;
    }

    public void setImgData(Bitmap imgData) {
        this.imgData = imgData;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getSimi() {
        return simi;
    }

    public void setSimi(float simi) {
        this.simi = simi;
    }
}
