package com.xinmei365.font.utils;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

public final class DensityUtils {
    static float sDensity;
    static float sScaledDensity;
    static int sDensityDpi;
    static int sWidth = -1;
    static int sHeight = -1;

    public static void getDensity(Application application) {
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        sDensity = displayMetrics.widthPixels / 360;
        sScaledDensity =  sDensity * (displayMetrics.scaledDensity / displayMetrics.density);
        sDensityDpi =  (int) (sDensity * DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void setNewDensity(DisplayMetrics dm) {
        dm.density = sDensity;
        dm.scaledDensity = sScaledDensity;
        dm.densityDpi = sDensityDpi;
    }
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public static float dip2pxForFloat(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }


    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 sp
     */
    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px
     */
    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取dialog宽度
     */
    public static int getDialogW(Context aty) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = aty.getResources().getDisplayMetrics();
        int w = dm.widthPixels - 100;
        // int w = aty.getWindowManager().getDefaultDisplay().getWidth() - 100;
        return w;
    }

    /**
     * 获取屏幕宽度
     */
    public static int getScreenW(Context aty) {
        if (sWidth == -1) {
            DisplayMetrics dm = new DisplayMetrics();
            dm = aty.getResources().getDisplayMetrics();
            sWidth = dm.widthPixels;
        }
        return sWidth;
    }


    /**
     * 获取屏幕高度
     */
    public static int getScreenH(Context aty) {
        if (sHeight == -1) {
            DisplayMetrics dm = new DisplayMetrics();
            dm = aty.getResources().getDisplayMetrics();
            int sHeight = dm.heightPixels;
        }
        return sHeight;
    }
}
