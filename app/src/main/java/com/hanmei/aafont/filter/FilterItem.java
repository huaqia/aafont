package com.hanmei.aafont.filter;

import android.text.TextUtils;

public class FilterItem {

    private String mTablePath;

    public static final String LUT_ADORE = "filter/adore.png";
    public static final String LUT_AMATORKA = "filter/amatorka.png";
    public static final String LUT_FAIRYTALE = "filter/fairytale.png";
    public static final String LUT_FLOWER = "filter/flower.png";
    public static final String LUT_HEART = "filter/heart.png";
    public static final String LUT_HIGHKEY = "filter/highkey.png";
    public static final String LUT_PERFUME = "filter/perfume.png";
    public static final String LUT_PROCESSED = "filter/processed.png";
    public static final String LUT_RESPONSIBLE = "filter/responsible.png";

    public int progress;

    public int mIcon;

    public String mName;

    public FilterItem(String filterPath, String name, int icon, int progress) {
        mTablePath = filterPath;
        mName = name;
        mIcon = icon;
        this.progress = progress;
    }


    public boolean isNone() {
        return TextUtils.isEmpty(mTablePath);
    }

    public BaseOriginalFilter instantiate() {
        if (TextUtils.isEmpty(mTablePath)) {
            return new EmptyGPUImageFilter();
        }
        BaseOriginalFilter filter;
        filter = new ColorLookupFilter(mTablePath);
        if (filter != null && filter.isDegreeAdjustSupported()) {
            filter.setDegree(progress);
        }
        return filter;
    }

}