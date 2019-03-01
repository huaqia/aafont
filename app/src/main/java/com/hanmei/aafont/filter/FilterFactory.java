package com.hanmei.aafont.filter;

import com.hanmei.aafont.R;

import java.util.ArrayList;

public class FilterFactory {

    public static ArrayList<FilterItem> getPortraitFilterItem() {
        ArrayList<FilterItem> filters = new ArrayList<FilterItem>();
        filters.add(new FilterItem(null, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 0));
        filters.add(new FilterItem(FilterItem.LUT_ADORE, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 70));
        filters.add(new FilterItem(FilterItem.LUT_AMATORKA, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 100));
        filters.add(new FilterItem(FilterItem.LUT_FAIRYTALE, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 100));
        filters.add(new FilterItem(FilterItem.LUT_FLOWER, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 100));
        filters.add(new FilterItem(FilterItem.LUT_HEART, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 100));
        filters.add(new FilterItem(FilterItem.LUT_HIGHKEY, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 70));
        filters.add(new FilterItem(FilterItem.LUT_PERFUME, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 100));
        filters.add(new FilterItem(FilterItem.LUT_PROCESSED, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 100));
        filters.add(new FilterItem(FilterItem.LUT_RESPONSIBLE, FilterSDK.sContext.getString(R.string.fans), R.drawable.notice, 100));
        return filters;
    }
}
