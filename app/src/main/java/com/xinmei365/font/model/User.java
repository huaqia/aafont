package com.xinmei365.font.model;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {
    private String nickName;
    private String avatar;
    private int gender;
    private String intro;
    private String installationId;
    private Integer appId;
    private int role;
    private ArrayList<String> channels;
    private ArrayList<String> focusIds;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public int getAppId() {
        return appId;
    }

    public int getRole() {
        return role;
    }

    public ArrayList<String> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<String> channels) {
        this.channels = channels;
    }

    public ArrayList<String> getFocusIds() {
        return focusIds;
    }

    public void setFocusIds(ArrayList<String> focusIds) {
        this.focusIds = focusIds;
    }
}
