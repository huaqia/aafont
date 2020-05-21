package com.xinmei365.font.filter;

import android.content.Context;

import com.xinmei365.font.R;

public class FilterItem {
    public int mIcon;

    public String mName;

    public FilterItem(String name, int icon) {
        mName = name;
        mIcon = icon;
    }

    public GPUImageFilter instantiate(Context context) {
        switch (mIcon) {
            case R.drawable.camerasdk_filter_in1977:
                return new IF1977Filter(context);
            case R.drawable.camerasdk_filter_amaro:
                return new IFAmaroFilter(context);
            case R.drawable.camerasdk_filter_brannan:
                return new IFBrannanFilter(context);
            case R.drawable.camerasdk_filter_early_bird:
                return new IFEarlybirdFilter(context);
            case R.drawable.camerasdk_filter_hefe:
                return new IFHefeFilter(context);
            case R.drawable.camerasdk_filter_hudson:
                return new IFHudsonFilter(context);
            case R.drawable.camerasdk_filter_inkwell:
                return new IFInkwellFilter(context);
            case R.drawable.camerasdk_filter_lomo:
                return new IFLomoFilter(context);
            case R.drawable.camerasdk_filter_lord_kelvin:
                return new IFLordKelvinFilter(context);
            case R.drawable.camerasdk_filter_nashville:
                return new IFNashvilleFilter(context);
            case R.drawable.camerasdk_filter_rise:
                return new IFRiseFilter(context);
            case R.drawable.camerasdk_filter_sierra:
                return new IFSierraFilter(context);
            case R.drawable.camerasdk_filter_sutro:
                return new IFSutroFilter(context);
            case R.drawable.camerasdk_filter_toaster:
                return new IFToasterFilter(context);
            case R.drawable.camerasdk_filter_valencia:
                return new IFValenciaFilter(context);
            case R.drawable.camerasdk_filter_walden:
                return new IFWaldenFilter(context);
            case R.drawable.camerasdk_filter_xproii:
                return new IFXprollFilter(context);
            default:
                return new EmptyGPUImageFilter();
        }
    }
}