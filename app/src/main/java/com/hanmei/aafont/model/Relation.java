package com.hanmei.aafont.model;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;

public class Relation extends BmobObject {
    private User user;
    private ArrayList<String> focusIds;
    private ArrayList<String> followIds;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<String> getFocusIds() {
        return focusIds;
    }

    public void setFocusIds(ArrayList<String> focusIds) {
        this.focusIds = focusIds;
    }

    public ArrayList<String> getFollowIds() {
        return followIds;
    }

    public void setFollowIds(ArrayList<String> followIds) {
        this.followIds = followIds;
    }
}
