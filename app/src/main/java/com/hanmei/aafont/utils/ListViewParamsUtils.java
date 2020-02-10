package com.hanmei.aafont.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class ListViewParamsUtils {
    public static ViewGroup.LayoutParams setListViewHeightBasedOnChild(ExpandableListView listView){
        ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
        if (listAdapter == null){
            return null;
        }

        int totalHeight = 0;
        //分割线数量
        int count = 0;
        for (int i = 0; i<listAdapter.getGroupCount(); i++){
            View listItem = listAdapter.getGroupView(i,true,null,listView);
            //计算子项View的宽高，注意listview所在的要是linearLayout布局
            listItem.measure(0 , 0);
            //统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
            count++;
            for (int j = 0; j < listAdapter.getChildrenCount(i); j++){
                View childItem = listAdapter.getChildView(i,j,true,null,listView);
                childItem.measure(0,0);
                totalHeight += childItem.getMeasuredHeight();
                count++;
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (count - 1));
        return params;
    }
}
