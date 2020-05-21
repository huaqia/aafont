package com.xinmei365.font.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperToast;
import com.xinmei365.font.BuildConfig;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;

public final class ToastUtils {

    public interface Style {
        int INFO = R.drawable.supertoast_blue;
        int ALERT = R.drawable.supertoast_red;
    }

    public static void show(String msg, int style) {
        Context context = MyApplication.getInstance();
        if (context != null) {
            cancelAllToast();
            SuperToast superToast = SuperToast.create(context, msg, SuperToast.Duration.SHORT);
            TextView superToastTextView = superToast.getTextView();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, context.getResources().getDisplayMetrics()));
            superToastTextView.setLayoutParams(layoutParams);
            superToastTextView.setGravity(Gravity.CENTER);
            superToastTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            superToast.setBackground(style);
            superToast.setAnimations(SuperToast.Animations.FADE);
            superToast.show();
        }
    }

    public static void showInfo(String msg) {
        show(msg, Style.INFO);
    }

    public static void showAlert(String msg) {
        show(msg, Style.ALERT);
    }

    public static void showDebug(String msg) {
        if (BuildConfig.DEBUG) {
            show(msg, Style.ALERT);
        }
    }

    public static void cancelAllToast() {
        SuperToast.cancelAllSuperToasts();
    }
}
