package com.hanmei.aafont.model;

import cn.bmob.v3.BmobObject;

public class Feedback extends BmobObject {
    private User user;

    private String content;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
