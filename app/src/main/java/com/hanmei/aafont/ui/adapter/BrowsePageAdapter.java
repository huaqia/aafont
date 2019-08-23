package com.hanmei.aafont.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class BrowsePageAdapter<T> extends PagerAdapter {

    private static final int MAX_LENGTH = 20;
    private LruCache<Integer, View> mBrowseViews;
    private int mConverId;
    private LayoutInflater mLayoutInflater;
    private List<T> mDataList;

    public BrowsePageAdapter() {

    }

    public BrowsePageAdapter(Context context, int mConverId, List<T> mDataList) {
        super();
        this.mDataList = mDataList;
        this.mConverId = mConverId;
        mLayoutInflater = LayoutInflater.from(context);
        mBrowseViews = new LruCache<>(MAX_LENGTH);
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = getView(container, position);
        container.addView(view);
        bindView(view, mDataList.get(position));
        return view;
    }

    private View getView(ViewGroup container, int position) {
        View view = mBrowseViews.get(position);
        if (view == null) {
            view = mLayoutInflater.inflate(mConverId, container, false);
        }
        view.setTag(position);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(getView(container, position));
    }

    public abstract void bindView(View view, T data);
}
