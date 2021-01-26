package com.xinmei365.font.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.Toast;

public class MiscUtils {
    public static void showAskDialog(Context context, String dialogTitle, DialogInterface.OnClickListener onClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(dialogTitle);
        builder.setPositiveButton("确定"  , onClickListener);
        builder.setNegativeButton("取消" , null);
        builder.create().show();
    }

    public static void showAlertDialog(Context context, String dialogTitle, DialogInterface.OnClickListener onClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(dialogTitle);
        builder.setPositiveButton("确定"  , onClickListener);
        builder.create().show();
    }

    public static void makeToast(Context context, CharSequence text, boolean isCenter) {
        if (context == null) {
            return;
        }
        if (context instanceof Activity) {
            if (((Activity) context).isDestroyed() || ((Activity) context).isDestroyed()) {
                return;
            }
        }
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        if (isCenter) {
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
        toast.show();
    }
}
