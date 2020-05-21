package com.xinmei365.font.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.xinmei365.font.R;

public class RatioImageView extends AppCompatImageView {
    // 宽和高的比例
    protected float ratio = 0.0f;
    ImageLoadCallback imageLoadCallback;

    public RatioImageView(Context context) {
        this(context, null);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatioImageView);
        ratio = a.getFloat(R.styleable.RatioImageView_image_ratio, 0.0f);
        a.recycle();
    }

    public void setImageLoadCallback(ImageLoadCallback callback) {
        this.imageLoadCallback = callback;
    }

    public void setRatio(float f) {
        ratio = f;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY && Float.compare(ratio, 0.0f) != 0) {
            height = (int) (width / ratio + 0.5f);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height + getPaddingTop() + getPaddingBottom(),
                    MeasureSpec.EXACTLY);
        } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY && Float.compare(ratio, 0.0f) != 0) {
            width = (int) (height * ratio + 0.5f);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width + getPaddingLeft() + getPaddingRight(),
                    MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (imageLoadCallback != null) {
            imageLoadCallback.onLoad(this, right - left, bottom - top);
        }
    }

    public interface ImageLoadCallback {
        void onLoad(RatioImageView view, int width, int height);
    }
}
