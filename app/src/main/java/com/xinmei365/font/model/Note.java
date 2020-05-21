package com.xinmei365.font.model;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;

public class Note extends BmobObject {
    private User user;
    private String userId;
    private String type;
    private String title;
    private String intro;
    private String maxRatio;
    private String firstRatio;
    private String oppoFontId;
    private String vivoFontId;
    private String huaweiFontId;
    private String xiaomiFontId;
    private ArrayList<ArrayList<EffectData.TagData>> tagDatas;
    private ArrayList<String> pics;
    private ArrayList<String> likeIds;
    private ArrayList<String> favoriteIds;
    private int hot;
    private int priority;

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
        this.userId = user.getObjectId();
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getIntro() {
        return intro;
    }
    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getMaxRatio() {
        return maxRatio;
    }
    public void setMaxRatio(String maxRatio) {
        this.maxRatio = maxRatio;
    }

    public String getFirstRatio() {
        return firstRatio;
    }
    public void setFirstRatio(String firstRatio) {
        this.firstRatio = firstRatio;
    }

    public String getOppoFontId() {
        return oppoFontId;
    }
    public void setOppoFontId(String oppoFontId) {
        this.oppoFontId = oppoFontId;
    }

    public String getVivoFontId() {
        return vivoFontId;
    }
    public void setVivoFontId(String vivoFontId) {
        this.vivoFontId = vivoFontId;
    }

    public String getHuaweiFontId() {
        return huaweiFontId;
    }
    public void setHuaweiFontId(String huaweiFontId) {
        this.huaweiFontId = huaweiFontId;
    }

    public String getXiaomiFontId() {
        return xiaomiFontId;
    }
    public void setXiaomiFontId(String xiaomiFontId) {
        this.xiaomiFontId = xiaomiFontId;
    }

    public ArrayList<ArrayList<EffectData.TagData>> getTagDatas() {
        return tagDatas;
    }
    public void setTagData(ArrayList<ArrayList<EffectData.TagData>> tagDatas) {
        this.tagDatas = tagDatas;
    }

    public ArrayList<String> getPics() {
        return pics;
    }
    public void setPics(ArrayList<String> pics) {
        this.pics = pics;
    }

    public ArrayList<String> getLikeIds() {
        return likeIds;
    }
    public void setLikeIds(ArrayList<String> likeIds) {
        this.likeIds = likeIds;
        ArrayList<String> favoriteIds = getFavoriteIds();
        if (favoriteIds != null) {
            this.hot = likeIds.size() + favoriteIds.size();
        } else {
            this.hot = likeIds.size();
        }
    }

    public ArrayList<String> getFavoriteIds() {
        return favoriteIds;
    }
    public void setFavoriteIds(ArrayList<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
        ArrayList<String> likeIds = getLikeIds();
        if (likeIds != null) {
            this.hot = favoriteIds.size() + likeIds.size();
        } else {
            this.hot = favoriteIds.size();
        }
    }

    public int getHot() {
        return hot;
    }
    public void setHot(int hot) {
        this.hot = hot;
    }

    public int getPriority() {
        return priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
