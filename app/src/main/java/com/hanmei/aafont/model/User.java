package com.hanmei.aafont.model;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;

public class User extends BmobUser {
    private BmobFile avatar;
    private Boolean gender;
    private String birthday;
    private String intro;
//    private BmobRelation focus;
//    private BmobRelation follow;
//    private ArrayList<String> focusIds;
//    private ArrayList<String> followIds;
    private String installationId;
    private boolean official;

    public BmobFile getAvatar() {
        return avatar;
    }

    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

//    public BmobRelation getFocus() {
//        return focus;
//    }
//
//    public void setFocus(BmobRelation focus) {
//        this.focus = focus;
//    }
//
//    public BmobRelation getFollow() {
//        return follow;
//    }
//
//    public void setFollow(BmobRelation follow) {
//        this.follow = follow;
//    }

//    public ArrayList<String> getFocusIds() {
//        return focusIds;
//    }
//
//    public void setFocusIds(ArrayList<String> focusIds) {
//        this.focusIds = focusIds;
//    }
//
//    public ArrayList<String> getFollowIds() {
//        return followIds;
//    }
//
//    public void setFollowIds(ArrayList<String> followIds) {
//        this.followIds = followIds;
//    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public boolean getOfficial() {
        return official;
    }

    public void setOfficial(boolean official) {
        this.official = official;
    }
}
