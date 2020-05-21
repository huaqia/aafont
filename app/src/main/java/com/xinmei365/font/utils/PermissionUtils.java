package com.xinmei365.font.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Set;

public class PermissionUtils {
    public static final int REQUEST_CODE_PERMISSIONS_ALL = 0x0000;

    /**
     * Check the permission is granted or not
     * @param context context can NOT be Null
     * @param permission the permission which you want to check
     * @return True is granted, False is not
     */
    public static boolean isPermissionGranted(@NonNull Context context, @NonNull String permission) {
        try{
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * Generate the Intent to open App's info Activity, which can modify permissions setting or others
     * @param context context Can NOT be Null
     * @return intent of application's info
     */
    public static Intent startInstalledAppDetailsIntent(@NonNull final Context context) {
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return i;
    }

    public static void showPermissionNeedGrantDialog(Context context, String content
            , final MaterialDialog.SingleButtonCallback onNegative
            , @NonNull final MaterialDialog.SingleButtonCallback onPositive) {
        Dialog dialog = new MaterialDialog.Builder(context)
                .content(content)
                .negativeText("dismiss")
                .positiveText("ok")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        if (onNegative != null) {
                            onNegative.onClick(dialog, which);
                        }
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        onPositive.onClick(dialog, which);
                    }
                })
                .build();
        dialog.show();
    }

    public static void requestPermissions(Activity activity, Set<String> permissions) {
        ActivityCompat.requestPermissions(activity,
                permissions.toArray(new String[] {}),
                REQUEST_CODE_PERMISSIONS_ALL);
    }
}