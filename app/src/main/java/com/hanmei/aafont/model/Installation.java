package com.hanmei.aafont.model;

import cn.bmob.v3.BmobInstallation;

/**
 * Created on 17/8/24 13:03
 */

public class Installation extends BmobInstallation {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}