package com.xinmei365.font.ui.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.EffectData;
import com.xinmei365.font.ui.widget.TagImageView;
import com.xinmei365.font.utils.DensityUtils;
import com.xinmei365.font.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;

public class DetailPicFragment extends BaseFragment {
    @BindView(R.id.pic_main)
    FrameLayout mPicMain;

    @BindView(R.id.pic_image)
    AppCompatImageView mImage;

    @BindView(R.id.tag_view)
    TagImageView mTagView;

    private String mPath;
    private Context mContext;
    private int mHeight;
    private ArrayList<EffectData.TagData> mTagDatas;

    public void setPath(String path) {
        mPath = path;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public void setTagDatas(ArrayList<EffectData.TagData> tagDatas) {
        mTagDatas = tagDatas;
    }

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail_pic, container,false);
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void init() {
        super.init();
        Glide.with(MyApplication.getInstance())
                .load(mPath)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mPicMain.getLayoutParams();
                        int maxWidth = DensityUtils.getScreenW(mContext);
                        int maxHeight = mHeight;
                        int imageWidth = bitmap.getWidth();
                        int imageHeight = bitmap.getHeight();
                        float imageRatio = (float)imageHeight / imageWidth;
                        params.width = maxWidth;
                        if (imageRatio > (float)maxHeight / imageWidth) {
                            params.height = maxHeight;
                        } else {
                            params.height = (int)(imageRatio * maxWidth);
                            params.bottomMargin = (maxHeight - params.height) / 2;
                            params.topMargin = (maxHeight - params.height) / 2;
                        }
                        mPicMain.setLayoutParams(params);
                        if (params.topMargin > 0) {
                            Glide.with(MyApplication.getInstance())
                                    .load(mPath)
                                    .fitCenter()
                                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                            if (mTagDatas != null) {
                                                for (EffectData.TagData tagData : mTagDatas) {
                                                    addTag(tagData.name, tagData.isLeft, new int[]{tagData.x, tagData.y}, new int[]{mImage.getWidth(), mImage.getHeight()});
                                                }
                                            }
                                            return false;
                                        }
                                    })
                                    .into(mImage);
                        } else {
                            Glide.with(MyApplication.getInstance())
                                    .load(mPath)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                            if (mTagDatas != null) {
                                                for (EffectData.TagData tagData : mTagDatas) {
                                                    addTag(tagData.name, tagData.isLeft, new int[]{tagData.x, tagData.y}, new int[]{mImage.getWidth(), mImage.getHeight()});
                                                }
                                            }
                                            return false;
                                        }
                                    })
                                    .into(mImage);
                        }
                    }
                });
    }

    public void addTag(String name, boolean isLeft, int[] xy, int[] wh) {
        mTagView.addTag(name, false, isLeft, xy, wh);
    }

    public Bitmap getBitmap() {
        return ((GlideBitmapDrawable)mImage.getDrawable().getCurrent()).getBitmap();
    }
}
