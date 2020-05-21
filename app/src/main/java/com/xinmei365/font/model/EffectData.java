package com.xinmei365.font.model;

import java.io.Serializable;
import java.util.ArrayList;

public class EffectData implements Serializable {
    private String filter;
    private ArrayList<TagData> tagDatas;
    private ArrayList<StickerData> stickerDatas;

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    public ArrayList<TagData> getTagDatas() {
        return tagDatas;
    }

    public void setTagDatas(ArrayList<TagData> tagDatas) {
        this.tagDatas = tagDatas;
    }

    public ArrayList<StickerData> getStickerDatas() {
        return stickerDatas;
    }

    public void setStickerDatas(ArrayList<StickerData> stickerDatas) {
        this.stickerDatas = stickerDatas;
    }

    public static class TagData implements Serializable {
        public int x;
        public int y;
        public boolean isLeft;
        public String name;
        public TagData(int x, int y, boolean isLeft, String name) {
            this.x = x;
            this.y = y;
            this.isLeft = isLeft;
            this.name = name;
        }
    }

    public static class StickerData implements Serializable {
        public String name;
        public float[] matrixValues;
        public StickerData(String name, float[] matrixValues) {
            this.name = name;
            this.matrixValues = matrixValues;
        }
    }
}
