package com.hanmei.aafont.model;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;

public class Product extends BmobObject {
    private User user;
    private BmobFile content;
    private ArrayList<String> likeId;
    private BmobRelation like;
    private boolean choice;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BmobFile getContent() {
        return content;
    }
    public void setContent(BmobFile content) {
        this.content = content;
    }

    public ArrayList<String> getLikeId() {
        return likeId;
    }

    public void setLikeId(ArrayList<String> likeId) {
        this.likeId = likeId;
    }

    public BmobRelation getLike() {
        return like;
    }

    public void setLike(BmobRelation like) {
        this.like = like;
    }

    public boolean getChoice() {
        return choice;
    }

    public void setChoice(boolean choice) {
        this.choice = choice;
    }
}
