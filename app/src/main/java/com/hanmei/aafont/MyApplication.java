package com.hanmei.aafont;

import android.app.Application;

import com.hanmei.aafont.utils.BackendUtils;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BackendUtils.init(this);
    }
}
