package com.xinmei365.font.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.Map;

public class TrackerUtils {
    private static String mChannel;

    public static String getChannel(Context context) {
        if (TextUtils.isEmpty(mChannel)) {
            mChannel = getMetaData(context, "UMENG_CHANNEL");
        }
        return mChannel;
    }

    private static String getMetaData(Context ctx, String key) {
        try {
            ApplicationInfo ai =
                    ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            return bundle.get(key) + "";
        } catch (Exception e) {
            return null;
        }
    }

    public static void initUmeng(Context context) {
        UMConfigure.init(context,  UMConfigure.DEVICE_TYPE_PHONE, null);
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }

    public static void onEvent(Context context, String key, String value) {
        MobclickAgent.onEvent(context, key, value);
    }

    public static void onEvent(Context context, String key, Map<String, String> value) {
        MobclickAgent.onEvent(context, key, value);
    }
}

