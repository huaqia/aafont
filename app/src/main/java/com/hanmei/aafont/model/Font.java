package com.hanmei.aafont.model;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class Font extends BmobObject {
    private String name;
    private BmobFile preview;
    private BmobFile ttf;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public BmobFile getPreview() {
        return preview;
    }
    public void setPreview(BmobFile preview) {
        this.preview = preview;
    }

    public BmobFile getTtf() {
        return ttf;
    }
    public void setTtf(BmobFile ttf) {
        this.ttf = ttf;
    }
}
