package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class BrowsePageAdapter<T> extends PagerAdapter {

    private static final int MAX_LENGTH = 20;
    private LruCache<Integer, View> mBrowseViews;
    private int mConverId;
    private LayoutInflater mLayoutInflater;
    private List<T> mCardDataList;
    private List<T> mUserNameDataList;
    private ArrayList<T> mLikeIdList;

    public BrowsePageAdapter() {

    }

    public BrowsePageAdapter(Context context, int mConverId, List<T> mCardDataList,List<T> mUserNameDataList,ArrayList<T> mLikeIdList) {
        super();
        this.mCardDataList = mCardDataList;
        this.mUserNameDataList = mUserNameDataList;
        this.mLikeIdList = mLikeIdList;
        this.mConverId = mConverId;
        mLayoutInflater = LayoutInflater.from(context);
        mBrowseViews = new LruCache<>(MAX_LENGTH);
    }

    @Override
    public int getCount() {
        return mCardDataList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = getView(container, position);
        container.addView(view);
        bindView(view, mCardDataList.get(position) , mUserNameDataList.get(position) , mLikeIdList.get(position));
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

    public abstract void bindView(View view, T data1 , T data2 , T data3);
}
