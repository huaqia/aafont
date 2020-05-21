package com.xinmei365.font.model;

import cn.bmob.v3.BmobObject;

public class Label extends BmobObject {
    private String name;
    private String type;
    private int count;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
