package com.hanmei.aafont.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class HeadAdapter extends PagerAdapter {

    public ArrayList<AppCompatImageView> viewLists;

    public HeadAdapter(){}

    public HeadAdapter(ArrayList<AppCompatImageView> viewLists)
    {
        super();
        this.viewLists = viewLists;
    }

    @Override
    public int getCount() {
        return viewLists.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container , int position)
    {
        container.addView(viewLists.get(position));
        return viewLists.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container , int position , Object object)
    {
        container.removeView(viewLists.get(position));
    }
}
