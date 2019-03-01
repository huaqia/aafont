package com.hanmei.aafont.model;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class Filter extends BmobObject {
    private String name;
    private BmobFile preview;

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
}
