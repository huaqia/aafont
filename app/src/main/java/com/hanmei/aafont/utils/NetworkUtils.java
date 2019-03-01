package com.hanmei.aafont.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkUtils {
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isMobileNumber(String number){
        String regex="^(1\\d{10}$)";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(number);
        return matcher.matches();
    }

    public static boolean isPasswordNumber(String password){
        if (password.length() == 6){
            return true;
        }
        return false;
    }

    public static boolean isNumber(String number){
        String regex="^[0-9]*$";
        Pattern pattern=Pattern.compile(regex);
        Matcher matcher=pattern.matcher(number);
        return matcher.matches();
    }
}
