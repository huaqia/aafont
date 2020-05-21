package com.xinmei365.font.model;

import cn.bmob.v3.BmobObject;

public class Reply extends BmobObject {
    private User user;
    private String content;
    private User replyUser;

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

    public User getReplyUser() {
        return replyUser;
    }

    public void setReplyUser(User replyUser) {
        this.replyUser = replyUser;
    }
}