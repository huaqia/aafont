package com.hanmei.aafont.utils;

import android.content.Context;
import android.os.Environment;

import com.hanmei.aafont.ui.widget.BaseSurfaceView;

public class MagicParams {
    public static Context context;
    public static BaseSurfaceView magicBaseView;

    public static String videoPath = Environment.getExternalStorageDirectory().getPath();
    public static String videoName = "MagicCamera_test.mp4";

    public static int beautyLevel = 5;

    public MagicParams() {

    }
}

