package com.xinmei365.font.filter;

import com.xinmei365.font.R;

import java.util.ArrayList;

public class FilterFactory {
    private static ArrayList<FilterItem> sFilters;
    public static ArrayList<FilterItem> getFilterItems() {
        if (sFilters != null) {
            return sFilters;
        }
        sFilters = new ArrayList<FilterItem>();
        sFilters.add(new FilterItem("原图", R.drawable.camerasdk_filter_normal));
        sFilters.add(new FilterItem("创新", R.drawable.camerasdk_filter_in1977));
        sFilters.add(new FilterItem("流年", R.drawable.camerasdk_filter_amaro));
        sFilters.add(new FilterItem("淡雅", R.drawable.camerasdk_filter_brannan));
        sFilters.add(new FilterItem("怡尚", R.drawable.camerasdk_filter_early_bird));
        sFilters.add(new FilterItem("优格", R.drawable.camerasdk_filter_hefe));
        sFilters.add(new FilterItem("胶片", R.drawable.camerasdk_filter_hudson));
        sFilters.add(new FilterItem("黑白", R.drawable.camerasdk_filter_inkwell));
        sFilters.add(new FilterItem("个性", R.drawable.camerasdk_filter_lomo));
        sFilters.add(new FilterItem("回忆", R.drawable.camerasdk_filter_lord_kelvin));
        sFilters.add(new FilterItem("不羁", R.drawable.camerasdk_filter_nashville));
        sFilters.add(new FilterItem("森系", R.drawable.camerasdk_filter_rise));
        sFilters.add(new FilterItem("清新", R.drawable.camerasdk_filter_sierra));
        sFilters.add(new FilterItem("摩登", R.drawable.camerasdk_filter_sutro));
        sFilters.add(new FilterItem("绚丽", R.drawable.camerasdk_filter_toaster));
        sFilters.add(new FilterItem("优雅", R.drawable.camerasdk_filter_valencia));
        sFilters.add(new FilterItem("日系", R.drawable.camerasdk_filter_walden));
        sFilters.add(new FilterItem("新潮", R.drawable.camerasdk_filter_xproii));
        return sFilters;
    }
}
