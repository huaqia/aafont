package com.hanmei.aafont.ui.adapter;

import android.util.Log;
import android.view.View;

import com.hanmei.aafont.ui.widget.FlowLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TagAdapter<T> {
    private static final String TAG = "TagAdapter";
    private List<T> mTagDatas;
    private OnDataChangedListener mOnDataChangedListener;

    private HashSet<Integer> mCheckedPosList = new HashSet<Integer>();

    public TagAdapter(List<T> mTagdatas){
        this.mTagDatas = mTagdatas;
    }

    public void setData(List<T> mTagDatas){
        this.mTagDatas = mTagDatas;
    }

    public TagAdapter(T[] datas){
        mTagDatas = new ArrayList<T>(Arrays.asList(datas));
    }

    public interface OnDataChangedListener{
        void onChanged();
    }

    public void setOnDataChangedListener(OnDataChangedListener listener){
        this.mOnDataChangedListener = listener ;
    }

    public void setSelectedList(int... poses){
        Set<Integer> set = new HashSet<>();
        for (int pos : poses){
            set.add(pos);
        }
        setSelectedList(set);
    }

    public void setSelectedList(Set<Integer> set){
        mCheckedPosList.clear();
        if (set != null) {
            mCheckedPosList.addAll(set);
        }
        notifyDataChanged();
    }

    //按下状态列表
    public HashSet<Integer> getPreCheckedList() {
        return mCheckedPosList;
    }

    public int getCount(){
        return mTagDatas == null ? 0 : mTagDatas.size();
    }

    public void notifyDataChanged(){
        if (mOnDataChangedListener != null){
            mOnDataChangedListener.onChanged();
        }
    }

    public T getItem(int position){
        return mTagDatas.get(position);
    }

    public abstract View getView(FlowLayout parent , int position , T t);

    public void onSelected(int position, View view) {
        Log.e(TAG, "onSelected " + position);
    }

    public void unSelected(int position, View view) {
        Log.e(TAG, "unSelected " + position);
    }

    public boolean setSelected(int position, T t) {
        return false;
    }

}
