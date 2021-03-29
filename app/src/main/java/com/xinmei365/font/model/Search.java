package com.xinmei365.font.model;

import cn.bmob.v3.BmobObject;

public class Search extends BmobObject {
    private String name;
    private int count;
    private int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
