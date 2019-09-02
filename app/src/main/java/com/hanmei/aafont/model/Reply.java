package com.hanmei.aafont.model;

import cn.bmob.v3.BmobObject;

public class Reply extends BmobObject {
    private User user;
    private Comment comment;
    private String content;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
