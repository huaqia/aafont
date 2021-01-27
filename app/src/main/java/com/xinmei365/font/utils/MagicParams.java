package com.xinmei365.font.utils;

import android.content.Context;
import android.os.Environment;

import com.xinmei365.font.ui.widget.BaseSurfaceView;

public class MagicParams {
    public static Context context;

    public static String videoPath = Environment.getExternalStorageDirectory().getPath();
    public static String videoName = "MagicCamera_test.mp4";

    public MagicParams() {

    }
}

