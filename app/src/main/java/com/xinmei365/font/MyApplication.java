package com.xinmei365.font;

import android.app.Application;

import com.xinmei365.font.filter.FilterSDK;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.DensityUtils;
import com.xinmei365.font.utils.TrackerUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import cn.bmob.newim.BmobIM;

public class MyApplication extends Application {
    private static MyApplication sApp;
    @Override
    public void onCreate() {
        super.onCreate();
        DensityUtils.getDensity(this);
        BackendUtils.init(this);
        FilterSDK.init(this);
        TrackerUtils.initUmeng(this);
        sApp = this;
        if (getApplicationInfo().packageName.equals(getMyProcessName())){
            BmobIM.init(this);
            BmobIM.registerDefaultMessageHandler(new DemoMessageHandler(this));
        }
    }

    public static MyApplication getInstance() {
        return sApp;
    }

    public static String getMyProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
