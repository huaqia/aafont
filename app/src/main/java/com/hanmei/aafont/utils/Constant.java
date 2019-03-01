package com.hanmei.aafont.utils;

import android.Manifest;

public class Constant {
    public static String BMOB_KEY = "908e43327bcd05d6b582c5fdfb5c35f6";

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
