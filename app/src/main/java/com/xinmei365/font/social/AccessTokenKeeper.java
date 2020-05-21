package com.xinmei365.font.social;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;


public final class AccessTokenKeeper {

    private static final String PREFERENCES_NAME = "com_xinmei_app_social";

    private static final String KEY_UID = "uid";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_EXPIRES_IN = "expires_in";

    public static void writeSinaAccessToken(Context context, Oauth2AccessToken token) {
        if (null == context || null == token) {
            return;
        }
        writeAccessToken(context, token, "sina");
    }

    private static void writeAccessToken(Context context, Oauth2AccessToken token, String type) {
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.putString(type + "_" + KEY_UID, token.getUid());
        editor.putString(type + "_" + KEY_ACCESS_TOKEN, token.getToken());
        editor.putLong(type + "_" + KEY_EXPIRES_IN, token.getExpiresTime());
        editor.commit();
    }

    public static Oauth2AccessToken readSinaAccessToken(Context context) {
        if (null == context) {
            return null;
        }
        return readAccessToken(context, "sina");
    }


    public static Oauth2AccessToken readAccessToken(Context context, String type) {
        if (null == context) {
            return null;
        }

        Oauth2AccessToken token = new Oauth2AccessToken();
        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        token.setUid(pref.getString(type + "_" + KEY_UID, ""));
        token.setToken(pref.getString(type + "_" + KEY_ACCESS_TOKEN, ""));
        token.setExpiresTime(pref.getLong(type + "_" + KEY_EXPIRES_IN, 0));
        return token;
    }


    public static void clear(Context context) {
        if (null == context) {
            return;
        }

        SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
        Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }
}