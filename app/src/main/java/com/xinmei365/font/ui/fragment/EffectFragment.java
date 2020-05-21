package com.xinmei365.font.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.xinmei365.font.R;
import com.xinmei365.font.filter.FilterFactory;
import com.xinmei365.font.filter.FilterItem;
import com.xinmei365.font.filter.GPUImageFilter;
import com.xinmei365.font.model.EffectData;
import com.xinmei365.font.ui.activity.LabelActivity;
import com.xinmei365.font.ui.adapter.EffectFilterAdapter;
import com.xinmei365.font.ui.widget.GPUImageView;
import com.xinmei365.font.ui.widget.ImageSticker;
import com.xinmei365.font.ui.widget.Sticker;
import com.xinmei365.font.ui.widget.StickerView;
import com.xinmei365.font.ui.widget.TagImageView;
import com.xinmei365.font.utils.DensityUtils;
import com.xinmei365.font.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.xinmei365.font.filter.GPUImage.ScaleType.CENTER_CROP;
import static com.xinmei365.font.filter.GPUImage.ScaleType.CENTER_INSIDE;

public class EffectFragment extends BaseFragment {
    @BindView(R.id.effect_main)
    FrameLayout mEffectMain;

    @BindView(R.id.effect_image)
    GPUImageView mEffectImage;

    @BindView(R.id.tag_view)
    TagImageView mTagView;

    @BindView(R.id.sticker_view)
    StickerView mStickerView;

    private String mPath;
    private String mFilterName;
    private int mFilterIndex;
    private EffectData mEffectData;
    private EffectFilterAdapter mFilterAdapter;
    private boolean mIsCurrent = false;
    private boolean mEffectDataApplied = false;
    private Context mContext;

    public void setPath(String path) {
        mPath = path;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setFilterAdapter(EffectFilterAdapter adapter) {
        mFilterAdapter = adapter;
    }

    public void setIsCurrent() {
        mIsCurrent = true;
    }

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_effect, container,false);
    }

    @Override
    public void init() {
        super.init();
        mFilterIndex = 0;
        mEffectImage.setOnMeasureListener(new GPUImageView.OnMeasureListener() {
            @Override
            public void onMeasure(int width, int height) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mEffectMain.getLayoutParams();
                final int screenWidth = DensityUtils.getScreenW(getContext());
                params.height = (int)(screenWidth * height / width);
                mEffectMain.setLayoutParams(params);
                List<FilterItem> filters = FilterFactory.getFilterItems();
                if (!mEffectDataApplied) {
                    if (mEffectData != null) {
                        String filter = mEffectData.getFilter();
                        if (filter != null) {
                            int filterIndex = 0;
                            for (int i = 0; i < filters.size(); i++) {
                                FilterItem item = filters.get(i);
                                if (item.mName.equals(filter)) {
                                    filterIndex = i;
                                    break;
                                }
                            }
                            if (filterIndex != 0) {
                                setEffect(filters.get(filterIndex).instantiate(mContext), filter, filterIndex);
                            }
                            if (mIsCurrent && mFilterAdapter != null) {
                                mFilterAdapter.setCurrentFilterIndex(filterIndex);
                                mFilterAdapter.notifyDataSetChanged();
                            }
                        }
                        ArrayList<EffectData.TagData> tagDatas = mEffectData.getTagDatas();
                        if (tagDatas != null) {
                            for (EffectData.TagData tagData : tagDatas) {
                                mTagView.addTag(tagData.name, true, tagData.isLeft, new int[]{tagData.x, tagData.y}, new int[]{width, height});
                            }
                        }
                        ArrayList<EffectData.StickerData> stickerDatas = mEffectData.getStickerDatas();
                        if (stickerDatas != null) {
                            ArrayList<Sticker> stickers = new ArrayList<>();
                            for (EffectData.StickerData stickerData : stickerDatas) {
                                if (stickerData.name.split("_").length == 2) {
                                    ImageSticker sticker = new ImageSticker(getActivity(), stickerData.name, -1, screenWidth);
                                    Matrix matrix = new Matrix();
                                    matrix.setValues(stickerData.matrixValues);
                                    sticker.setMatrix(matrix);
                                    stickers.add(sticker);
                                }
                            }
                            mStickerView.setStickers(stickers);
                        }
                    }
                    mEffectDataApplied = true;
                }
            }
        });
        mEffectImage.setImage(mPath);
        mEffectImage.setScaleType(CENTER_CROP);

        mTagView.setAddTagListener(new TagImageView.AddTagListener() {
            @Override
            public void addTag(int rawX, int rawY) {
                Intent intent = new Intent(getActivity(), LabelActivity.class);
                intent.putExtra("xy", new int[]{rawX, rawY});
                startActivityForResult(intent, 100);
            }
        });

        mStickerView.setLocked(false);
        mStickerView.setConstrained(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == 100) {
            String text = data.getStringExtra("text");
            int[] xy = data.getIntArrayExtra("xy");
            mTagView.addTag(text, true, true, xy, null);
        }
    }

    public void addTag(String name) {
        mTagView.addTag(name, true, true, null, null);
    }

    public void setEffect(GPUImageFilter filter, String name, int index){
        mFilterName = name;
        mFilterIndex = index;
        mEffectImage.setFilter(filter);
        mEffectImage.requestRender();
    }

    public void addSticker(Sticker sticker) {
        mStickerView.addSticker(sticker);
    }

    private void setSourceBitmap(Bitmap sourceBitmap){
        float width = sourceBitmap.getWidth();
        float height = sourceBitmap.getHeight();
        float ratio = width / height;

        mEffectImage.setRatio(ratio);
        mEffectImage.setImage(sourceBitmap);
    }

    public int getFilterIndex() {
        return mFilterIndex;
    }

    public void setEffectData(EffectData data) {
        mEffectData = data;
        mFilterName = data.getFilter();
    }

    public EffectData getEffectData() {
        EffectData data = new EffectData();
        if (mFilterName != null) {
            data.setFilter(mFilterName);
        }
        if (mTagView != null) {
            data.setTagDatas(mTagView.getTagDatas());
        }
        if (mStickerView != null) {
            data.setStickerDatas(mStickerView.getStickerDatas());
        }
        return data;
    }

    public String savePicture(String savedUrl) {
        File file;
        if (savedUrl == null) {
            file = new File(FileUtils.getFileDir(mContext, "pics"), System.currentTimeMillis() + ".png");
        } else {
            file = new File(savedUrl);
        }
        try {
            Bitmap image = mEffectImage.capture();
            if (image != null) {
                Canvas canvas = new Canvas(image);
                mStickerView.hideBorderIcons();
                mStickerView.draw(canvas);
                image.compress(Bitmap.CompressFormat.PNG, 80, new FileOutputStream(file));
            }
        } catch (InterruptedException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }
}