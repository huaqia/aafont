package com.xinmei365.font.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
    private final static long minute = 60 * 1000;// 1分钟
    private final static long hour = 60 * minute;// 1小时
    private final static long day = 24 * hour;// 1天
    private final static long month = 31 * day;// 月
    private final static long year = 12 * month;// 年

    public final static String FORMAT_TIME = "HH:mm";
    public final static String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm";
    public final static String FORMAT_DATE_TIME_SECOND = "yyyy-MM-dd HH:mm:ss";
    public final static String FORMAT_MONTH_DAY_TIME = "MM-dd HH:mm";

    /**
     * 返回文字描述的日期
     *
     * @param date
     * @return
     */
    public static String getTimeFormatText(Date date) {
        if (date == null) {
            return null;
        }
        long diff = new Date().getTime() - date.getTime();
        long r = 0;
        if (diff > year) {
            r = (diff / year);
            return r + "年前";
        }
        if (diff > month) {
            r = (diff / month);
            return r + "个月前";
        }
        if (diff > day) {
            r = (diff / day);
            return r + "天前";
        }
        if (diff > hour) {
            r = (diff / hour);
            return r + "小时前";
        }
        if (diff > minute) {
            r = (diff / minute);
            return r + "分钟前";
        }
        return "刚刚";
    }

    public static Date getSimpleDateFormat(String date) {
        Date mdate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            mdate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mdate;
    }

    public static String getChatTime(long timesamp) {
        long clearTime = timesamp;
        String result;
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        Date today = new Date(System.currentTimeMillis());
        Date otherDay = new Date(clearTime);
        int temp = Integer.parseInt(sdf.format(today))
                - Integer.parseInt(sdf.format(otherDay));
        switch (temp) {
            case 0:
                result = "今天 " + getHourAndMin(clearTime);
                break;
            case 1:
                result = "昨天 " + getHourAndMin(clearTime);
                break;
            case 2:
                result = "前天 " + getHourAndMin(clearTime);
                break;
            default:
                result = getTime(clearTime);
                break;
        }
        return result;
    }

    public static String getTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_DATE_TIME);
        return format.format(new Date(time));
    }

    public static String getHourAndMin(long time) {
        SimpleDateFormat format = new SimpleDateFormat(FORMAT_TIME);
        return format.format(new Date(time));
    }
}