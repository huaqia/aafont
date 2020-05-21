package com.xinmei365.font.utils;

import android.Manifest;

public class Constant {
    public static String BMOB_KEY = "86832a3960f6fdb0149a9cef72ce3e50";
    public final static String WEIXIN_APP_ID = "wx811ab8420924dda8";
    public final static String WEIXIN_APP_SECRET = "55712d06e494fafd4801c9efc344415e";
    public static final String WEIBO_APP_KEY = "3690340808";
    public static final String WEIBO_REDIRECT_URL = "http://sina.com.cn";
    public static final String WEIBO_SCOPE = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read," + "follow_app_official_microblog," + "invitation_write";

    public static final String[] PERMISSIONS_SHOULD_GRANT = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
    };

    public static final int FETCH_STARTED = 2001;
    public static final int FETCH_COMPLETED = 2002;
    public static final int PERMISSION_GRANTED = 2003;
    public static final int PERMISSION_DENIED = 2004;
    public static final int ERROR = 2005;
}
