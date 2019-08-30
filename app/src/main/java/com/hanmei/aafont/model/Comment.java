package com.hanmei.aafont.model;

import java.util.ArrayList;

import cn.bmob.v3.BmobObject;

public class Comment extends BmobObject {

    private User user;
    private Product product;
    private String content;
    private ArrayList<Reply> replyId;

    public ArrayList<Reply> getReplyId() {
        return replyId;
    }

    public void setReplyId(ArrayList<Reply> replyId) {
        this.replyId = replyId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
