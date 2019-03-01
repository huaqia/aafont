package com.hanmei.aafont.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesUtils {
    public static final String PREF_PUSH_NOTICE = "push_notice";
    public static final String PREF_PRIVATE_SETTING = "private_setting";
    public static final String PREF_LIKE_SETTING = "like_setting";
    public static final String PREF_COMMENT_SETTING = "comment_setting";
    public static final String PREF_AT_SETTING = "at_setting";
    public static final String PREF_FANS_SETTING = "fans_setting";

    public static boolean getBoolean(Context context, String str, boolean def) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp != null) {
            return sp.getBoolean(str, def);
        }
        return def;
    }

    public static void setBoolean(Context context, String str, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(str, value);
            editor.apply();
        }
    }

    public static int getInt(Context context, String str, int def) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp != null) {
            return sp.getInt(str, def);
        }
        return def;
    }

    public static void setInt(Context context, String str, int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(str, value);
            editor.apply();
        }
    }
}
