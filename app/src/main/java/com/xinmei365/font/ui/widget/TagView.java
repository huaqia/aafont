package com.xinmei365.font.ui.widget;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

public class TagView extends FrameLayout implements Checkable {

    private boolean isChecked;
    private static final int[] CHECK_STATE = new int[]{android.R.attr.state_checked};

    public TagView(Context context) {
        super(context);
    }

    public View getTagView()
    {
        return getChildAt(0);
    }

    //为view生成新的Drawable的状态
    public int[] onCreateDrawableState(int extraSpace)
    {
        int[] states = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked())
        {
            //将存储在additionalState中的状态和baseState中的状态合并到一起
            mergeDrawableStates(states, CHECK_STATE);
        }
        return states;
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.isChecked != checked){
            this.isChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }
}
