package com.xinmei365.font.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.text.TextUtilsCompat;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.view.View;
import android.view.ViewGroup;

import com.xinmei365.font.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FlowLayout extends ViewGroup {

    private static final String TAG = "FlowLayout" ;
    private static final int LEFT = -1;
    private static final int CENTER = 0;
    private static final int RIGHT = 1;

    //显示行数
    private int limitLineCount = 3;
    //是否有行限制
    private boolean isLimit;
    //是否允许溢出
    private boolean isOverFlow;

    private int mGravity;

    protected List<List<View>> mAllViews = new ArrayList<List<View>>();
    protected List<Integer> mLineHeight = new ArrayList<Integer>();//每行的高
    protected List<Integer> mLineWidth = new ArrayList<Integer>();//每行的宽
    private List<View> lineViews = new ArrayList<>();//每行view数量

    public boolean isOverFlow(){
        return isOverFlow;
    }

    private void setOverFlow(boolean isOverFlow){
        this.isOverFlow = isOverFlow;
    }

    public boolean isLimit(){
        return isLimit;
    }

    public void setLimit(boolean isLimit){
        if (!isLimit){
            setOverFlow(false);
        }
        this.isLimit = isLimit;
    }

    public FlowLayout(Context context){
        super(context);
    }

    public FlowLayout(Context context ,AttributeSet attrs){
        super(context , attrs);
        initAttrs(context, attrs);
    }

    public FlowLayout(Context context, AttributeSet attrs , int defStyle) {
        super(context , attrs , defStyle);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray type = context.obtainStyledAttributes(attrs , R.styleable.TabLayout);
        mGravity = type.getInt(R.styleable.TagFlowLayout_tag_gravity , LEFT);
        limitLineCount = type.getInt(R.styleable.TagFlowLayout_limit_line_count , 3);
        isLimit = type.getBoolean(R.styleable.TagFlowLayout_is_limit , false);
        int layoutDirection = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault());//布局默认方向
        if (layoutDirection == LayoutDirection.RTL){
            if (mGravity == LEFT){
                mGravity = RIGHT;
            }else {
                mGravity = LEFT;
            }
        }
        type.recycle();
    }
    @Override
    protected void onLayout(boolean changed, int l, int t , int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();
        mLineWidth.clear();
        lineViews.clear();

        //ViewGroup的高度
        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        //超过规定行数不进行绘制
        int lineCount = 0;
        //当前layout下子view的数量
        int cCount = getChildCount();

        for (int i = 0 ; i < cCount ; i++){
            View child = getChildAt(i);
            if (child.getVisibility() ==  View.GONE){
                continue;
            }
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            //子view的宽度占满整个父view时向下添加一行
            if (childWidth + lineWidth + params.leftMargin + params.rightMargin > width - getPaddingLeft() - getPaddingRight()){
                if (isLimit){
                    if (lineCount == limitLineCount){
                        break;
                    }
                }

                mLineWidth.add(lineWidth);
                mLineHeight.add(lineHeight);
                mAllViews.add(lineViews);

                lineWidth = 0;
                lineHeight = childHeight + params.bottomMargin + params.topMargin;
                lineViews = new ArrayList<View>();
                lineCount++;
            }
            lineWidth += childWidth + params.leftMargin + params.rightMargin;
            lineHeight = Math.max(lineHeight , childHeight+params.topMargin + params.bottomMargin);
            lineViews.add(child);
        }
        mLineHeight.add(lineHeight);
        mLineWidth.add(lineWidth);
        mAllViews.add(lineViews);

        //设置子view的位置
        int left = getPaddingLeft();
        int top = getPaddingTop();

        for (int i = 0 ; i < mAllViews.size(); i++) {
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);

            int currentLineWidth = this.mLineWidth.get(i);
            //选择对齐方式
            switch (mGravity) {
                case LEFT:
                    left = getPaddingLeft();
                    break;
                case CENTER:
                    left = (width - currentLineWidth) / 2 + getPaddingLeft();
                    break;
                case RIGHT:
                    left = width - (currentLineWidth + getPaddingLeft()) - getPaddingRight();
                    Collections.reverse(lineViews);
                    break;
            }
            for (int j = 0 ; j<lineViews.size();j++){
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE){
                    continue;
                }
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
                int lc = left + params.leftMargin;
                int tc = top + params.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();
                child.layout(lc , tc , rc , bc);

                left += child.getMeasuredWidth() + params.leftMargin
                        + params.rightMargin;
            }
            top += lineHeight;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // 在wrap_content情况下，记录宽跟高
        int width = 0;
        int height = 0;

        //每一行的宽高
        int lineWidth = 0;
        int lineHeight = 0;

        //记录当前的行数
        int lineCount = 0;

        //内部View个数
        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++) {
            //通过索引拿到每一个子View
            View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) {
                if (i == cCount - 1) {
                    if (isLimit) {
                        if (lineCount == limitLineCount) {
                            setOverFlow(true);
                            break;
                        } else {
                            setOverFlow(false);
                        }
                    }

                    width = Math.max(lineWidth, width);
                    height += lineHeight;
                    lineCount++;
                }
                continue;
            }
            //测量子view的宽和高，系统提供的measureChild
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            //得到LayoutParams
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            //子view占据的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin
                    + lp.rightMargin;
            //子view占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;

            //view的行数达到上限就不再绘制
            if (lineWidth + childWidth > sizeWidth - getPaddingLeft() - getPaddingRight()) {
                //判断是否超出限制
                if (isLimit) {
                    if (lineCount == limitLineCount) {
                        setOverFlow(true);
                        break;
                    } else {
                        setOverFlow(false);
                    }
                }
                width = Math.max(width, lineWidth);
                lineWidth = childWidth;
                height += lineHeight;
                lineHeight = childHeight;
                lineCount++;
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            if (i == cCount - 1) {
                if (isLimit) {
                    if (lineCount == limitLineCount) {
                        setOverFlow(true);
                        break;
                    }  else {
                        setOverFlow(false);
                    }
                }
                width = Math.max(lineWidth, width);
                height += lineHeight;
                lineCount++;
            }
        }
        setMeasuredDimension(
                modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width + getPaddingLeft() + getPaddingRight(),
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height + getPaddingTop() + getPaddingBottom()//
        );
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }
}
