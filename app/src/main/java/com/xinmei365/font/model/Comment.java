package com.xinmei365.font.model;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;

public class Comment extends BmobObject {

    private User user;
    private String noteId;
    private String content;
    private ArrayList<Reply> replyIds;

    public ArrayList<Reply> getReplyIds() {
        return replyIds;
    }

    public void setReplyIds(ArrayList<Reply> replyId) {
        this.replyIds = replyId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
