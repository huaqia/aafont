package com.xinmei365.font.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

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
}
