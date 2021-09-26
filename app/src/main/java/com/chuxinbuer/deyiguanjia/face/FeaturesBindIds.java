package com.chuxinbuer.deyiguanjia.face;
import java.util.ArrayList;
import java.util.List;

public class FeaturesBindIds {
    List<String> token=new ArrayList<>();
    List<byte[]> features=new ArrayList<>();

    public List<String> getToken() {
        return token;
    }

    public void setToken(List<String> token) {
        this.token = token;
    }

    public List<byte[]> getFeatures() {
        return features;
    }

    public void setFeatures(List<byte[]> features) {
        this.features = features;
    }
}
