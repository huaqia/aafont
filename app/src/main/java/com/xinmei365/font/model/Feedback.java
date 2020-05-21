package com.xinmei365.font.model;

import cn.bmob.v3.BmobObject;

public class Feedback extends BmobObject {
    private User user;

    private String content;

    private String contactInfo;

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

    public String getContactInfo() {
        return contactInfo;
    }
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
