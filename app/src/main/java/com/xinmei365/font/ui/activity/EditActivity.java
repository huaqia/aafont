package com.xinmei365.font.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xinmei365.font.R;
import com.xinmei365.font.filter.FilterFactory;
import com.xinmei365.font.filter.FilterItem;
import com.xinmei365.font.model.EffectData;
import com.xinmei365.font.model.StickerFactory;
import com.xinmei365.font.ui.adapter.EffectFilterAdapter;
import com.xinmei365.font.ui.adapter.FragmentViewPagerAdapter;
import com.xinmei365.font.ui.fragment.EffectFragment;
import com.xinmei365.font.ui.widget.ImageSticker;
import com.xinmei365.font.utils.DensityUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class EditActivity extends BaseActivity {
    @BindView(R.id.image_select_close)
    AppCompatImageView mClose;

    @BindView(R.id.page_idx)
    AppCompatTextView mPageIdx;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @BindView(R.id.content_container)
    RelativeLayout mContentContainer;

    @BindView(R.id.filter_recycler_view)
    RecyclerView mFilterRecycleView;

    @BindView(R.id.sticker_area)
    LinearLayout mStickerArea;

    @BindView(R.id.sticker_text_recycler_view)
    RecyclerView mStickerTextRecycleView;

    @BindView(R.id.sticker_image_recycler_view)
    RecyclerView mStickerImageRecycleView;

    @BindView(R.id.filter_text)
    AppCompatTextView mFilterText;
    @BindView(R.id.filter_text_highlight)
    AppCompatImageView mFilterTextHighlight;

    @BindView(R.id.label_text)
    AppCompatTextView mLabelText;

    @BindView(R.id.sticker_text)
    AppCompatTextView mStickerText;
    @BindView(R.id.sticker_text_highlight)
    AppCompatImageView mStickerTextHighlight;

    @BindView(R.id.press_next)
    AppCompatButton mPressNext;

    private EffectFilterAdapter mFilterAdapter;
    private StickerTextAdapter mStickerTextAdapter;
    private StickerImageAdapter mStickerImageAdapter;
    private FragmentViewPagerAdapter mFragmentAdapter;

    private ArrayList<String> mUrls;
    private ArrayList<String> mSavedUrls;

    private List<FilterItem> mFilters;
    private List<Pair<String, List<Pair<String, Integer>>>> mStickerss;
    private ArrayList<Fragment> mFragments;
    private int mCurrentPageIndex;
    private int mScreenWidth;

    private Activity mActivity;
    private boolean mIsAdd;
    private boolean mIsModify;
    private int mStartIndex;
    private ArrayList<EffectData> mEffectDatas;

    private int mTabNormalColor;
    private int mTabSelectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTabNormalColor = getResources().getColor(R.color.main_tab_normal_color);
        mTabSelectedColor = getResources().getColor(R.color.main_tab_selected_color);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mActivity = this;
        mFilters = FilterFactory.getFilterItems();
        mStickerss = StickerFactory.getStickerss();
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mUrls = getIntent().getExtras().getStringArrayList("urls");
        mSavedUrls = getIntent().getExtras().getStringArrayList("savedUrls");
        mIsAdd = getIntent().getExtras().getBoolean("add", false);
        mIsModify = getIntent().getExtras().getBoolean("modify", false);
        mStartIndex = getIntent().getExtras().getInt("startIdx", 0);
        mEffectDatas = (ArrayList<EffectData>)getIntent().getSerializableExtra("effect");
        mFilterAdapter = new EffectFilterAdapter(mFilters, new EffectFilterAdapter.OnClickListener() {
            @Override
            public void onClick(int index) {
                FilterItem filter = mFilters.get(index);
                ((EffectFragment) mFragments.get(mCurrentPageIndex)).setEffect(filter.instantiate(getApplicationContext()), filter.mName, index);
            }
        });
        mFilterRecycleView.setAdapter(mFilterAdapter);
        mFilterRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mFragments = new ArrayList<>();
        for (int i = 0; i < mUrls.size(); i++) {
            EffectFragment fragment = new EffectFragment();
            fragment.setPath(mUrls.get(i));
            fragment.setContext(getApplicationContext());
            fragment.setFilterAdapter(mFilterAdapter);
            if (i == mStartIndex) {
                fragment.setIsCurrent();
            }
            mFragments.add(fragment);
        }
        if (mEffectDatas != null && mEffectDatas.size() > 0) {
            for (int i = 0; i < mFragments.size(); i++) {
                if (mEffectDatas.size() > i) {
                    ((EffectFragment)mFragments.get(i)).setEffectData(mEffectDatas.get(i));
                }
            }
        }
        mFragmentAdapter = new FragmentViewPagerAdapter(getSupportFragmentManager(), mViewPager, mFragments);
        mPageIdx.setText(String.format("编辑照片(%d/%d)", mCurrentPageIndex + 1, mUrls.size()));
        mFragmentAdapter.setOnExtraPageChangeListener(new FragmentViewPagerAdapter.OnExtraPageChangeListener() {
            public void onExtraPageScrolled(int i, float v, int i2){}
            public void onExtraPageSelected(int i) {
                mCurrentPageIndex = i;
                EffectFragment fragment = (EffectFragment)mFragments.get(mCurrentPageIndex);
                fragment.setIsCurrent();
                mPageIdx.setText(String.format("编辑照片(%d/%d)", mCurrentPageIndex + 1, mUrls.size()));
                if (mFilterAdapter != null) {
                    int filterIndex = fragment.getFilterIndex();
                    mFilterAdapter.setCurrentFilterIndex(filterIndex);
                    mFilterAdapter.notifyDataSetChanged();
                }
            }
            public void onExtraPageScrollStateChanged(int i){}
        });
        ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
        params.height = (int)(DensityUtils.getScreenW(getApplicationContext()) * 4 / 3);
        mViewPager.setLayoutParams(params);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(9);
        mCurrentPageIndex = mStartIndex;
        mViewPager.setCurrentItem(mStartIndex);

//        mFilterText.setTextColor(getResources().getColor(R.color.colorActiveState));
//        mFilterTextHighlight.setVisibility(View.VISIBLE);
        mFilterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFilterTextHighlight.getVisibility() == View.GONE) {
                    mContentContainer.setVisibility(View.VISIBLE);
                    mStickerText.setTextColor(getResources().getColor(R.color.colorNormalState));
                    mStickerTextHighlight.setVisibility(View.GONE);
                    mFilterRecycleView.setVisibility(View.VISIBLE);
                    mStickerArea.setVisibility(View.INVISIBLE);
                    mFilterText.setTextColor(getResources().getColor(R.color.colorActiveState));
                    mFilterTextHighlight.setVisibility(View.VISIBLE);
                } else {
                    mContentContainer.setVisibility(View.GONE);
                    mFilterRecycleView.setVisibility(View.GONE);
                    mFilterText.setTextColor(getResources().getColor(R.color.colorNormalState));
                    mFilterTextHighlight.setVisibility(View.GONE);
                }
            }
        });
        mLabelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LabelActivity.class);
                startActivityForResult(intent, 100);
            }
        });
        mStickerTextAdapter = new StickerTextAdapter();
        mStickerTextRecycleView.setAdapter(mStickerTextAdapter);
        mStickerTextRecycleView.setLayoutManager((new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)));
        mStickerImageAdapter = new StickerImageAdapter();
        mStickerImageRecycleView.setAdapter(mStickerImageAdapter);
        mStickerImageAdapter.setData(mStickerss.get(0).second);
        mStickerImageRecycleView.setLayoutManager((new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)));
        mStickerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStickerTextHighlight.getVisibility() == View.GONE) {
                    mContentContainer.setVisibility(View.VISIBLE);
                    mFilterText.setTextColor(getResources().getColor(R.color.colorNormalState));
                    mFilterTextHighlight.setVisibility(View.GONE);
                    mFilterRecycleView.setVisibility(View.INVISIBLE);
                    mStickerArea.setVisibility(View.VISIBLE);
                    mStickerText.setTextColor(getResources().getColor(R.color.colorActiveState));
                    mStickerTextHighlight.setVisibility(View.VISIBLE);
                } else {
                    mContentContainer.setVisibility(View.GONE);
                    mStickerArea.setVisibility(View.GONE);
                    mStickerText.setTextColor(getResources().getColor(R.color.colorNormalState));
                    mStickerTextHighlight.setVisibility(View.GONE);
                }
            }
        });
        mPressNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, ArrayList<String>>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        showProgressBar(true, "图片处理中...");
                    }
                    @Override
                    protected ArrayList<String> doInBackground(Void... params) {
                        ArrayList<String> urls = new ArrayList<>();
                        for (int i = 0; i < mFragments.size(); i++) {
                            EffectFragment fragment = (EffectFragment)mFragments.get(i);
                            String savedUrl = null;
                            if (mSavedUrls != null && mSavedUrls.size() > i) {
                                savedUrl = mSavedUrls.get(i);
                            }
                            urls.add(fragment.savePicture(savedUrl));
                        }
                        return urls;
                    }

                    @Override
                    protected void onPostExecute(ArrayList<String> urls) {
                        ArrayList<EffectData> datas = new ArrayList<>();
                        for (Fragment fragment : mFragments) {
                            EffectData data = ((EffectFragment)fragment).getEffectData();
                            datas.add(data);
                        }
                        if (mIsAdd || mIsModify) {
                            Intent intent = new Intent();
                            intent.putStringArrayListExtra("urls", mUrls);
                            intent.putStringArrayListExtra("savedUrls", urls);
                            intent.putExtra("effect", datas);
                            setResult(RESULT_OK, intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), PublishActivity.class);
                            intent.putStringArrayListExtra("urls", mUrls);
                            intent.putStringArrayListExtra("savedUrls", urls);
                            intent.putExtra("effect", datas);
                            startActivity(intent);
                        }
                        showProgressBar(false);
                        finish();
                    }
                }.execute();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == 100) {
            String text = data.getStringExtra("text");
            ((EffectFragment)mFragments.get(mCurrentPageIndex)).addTag(text);
        }
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_edit);
    }

    class StickerTextAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private int mCurrentIndex = 0;
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new StickerTextViewHolder(inflater.inflate(R.layout.item_edit_sticker_text, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof StickerTextViewHolder) {
                StickerTextViewHolder viewHolder = (StickerTextViewHolder)holder;
                AppCompatTextView textView = viewHolder.mTextView;
                textView.setText(mStickerss.get(position).first);
                if (position == mCurrentIndex) {
                    textView.setTextColor(mTabSelectedColor);
                } else {
                    textView.setTextColor(mTabNormalColor);
                }
                viewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCurrentIndex = position;
                        notifyDataSetChanged();
                        mStickerImageAdapter.setData(mStickerss.get(position).second);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mStickerss.size();
        }
    }

    static class StickerTextViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mTextView;

        public StickerTextViewHolder(View itemView) {
            super(itemView);
            mTextView = (AppCompatTextView) itemView.findViewById(R.id.sticker_text);
        }
    }

    class StickerImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Pair<String, Integer>> mStickers;

        public void setData(List<Pair<String, Integer>> stickers) {
            mStickers = stickers;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new StickerImageViewHolder(inflater.inflate(R.layout.item_edit_sticker_image, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof StickerImageViewHolder) {
                StickerImageViewHolder viewHolder = (StickerImageViewHolder)holder;
                AppCompatImageView preview = viewHolder.mPreview;
                final Pair<String, Integer> stickerInfo = mStickers.get(position);
                preview.setImageResource(stickerInfo.second);
                preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((EffectFragment)mFragments.get(mCurrentPageIndex)).addSticker(new ImageSticker(mActivity, stickerInfo.first, stickerInfo.second, mScreenWidth));
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mStickers.size();
        }
    }

    static class StickerImageViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView mPreview;

        public StickerImageViewHolder(View itemView) {
            super(itemView);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.sticker_preview);
        }
    }
}
