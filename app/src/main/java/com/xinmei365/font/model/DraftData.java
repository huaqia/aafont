package com.xinmei365.font.model;

import java.io.Serializable;
import java.util.ArrayList;

public class DraftData implements Serializable {
    private String title;
    private String intro;
    private String time;
    private String type;
    private ArrayList<String> urls;
    private ArrayList<String> savedUrls;
    private ArrayList<EffectData> effectDatas;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getIntro() {
        return intro;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setSavedUrls(ArrayList<String> savedUrls) {
        this.savedUrls = savedUrls;
    }

    public ArrayList<String> getSavedUrls() {
        return savedUrls;
    }

    public void setEffectDatas(ArrayList<EffectData> effectDatas) {
        this.effectDatas = effectDatas;
    }

    public ArrayList<EffectData> getEffectDatas() {
        return effectDatas;
    }
}
