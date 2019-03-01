package com.hanmei.aafont.filter;

import android.content.Context;

public class FilterSDK {
    public static Context sContext;

    public static void init(Context context) {
        sContext = context;
    }

    private FilterSDK() {

    }
}