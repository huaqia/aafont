package com.xinmei365.font.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import com.xinmei365.font.R;
import com.xinmei365.font.ui.adapter.LabelAdapter;
import com.xinmei365.font.ui.adapter.PageAdapter;
import com.xinmei365.font.ui.fragment.LabelFragment;
import com.xinmei365.font.ui.fragment.NoteFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class LabelActivity extends BaseActivity implements LabelAdapter.OnLabelClickListener {
    private static String RECOMMEND = "推荐";
    private static String FONT = "字体";
    private static String WALLPAPER = "壁纸";
    private static String THEME = "主题";

    @BindView(R.id.cancel)
    AppCompatTextView mCancel;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private LabelFragment mRecommendFragment;
    private LabelFragment mFontFragment;
    private LabelFragment mWallpaperFragment;
    private LabelFragment mThemeFragment;
    private PageAdapter mPageAdapter;
    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();
    private int[] mXy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mXy = intent.getIntArrayExtra("xy");
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onBackPressed();
                finish();
            }
        });
        mRecommendFragment = new LabelFragment();
        mRecommendFragment.setDateType("recommend");
        mRecommendFragment.setClickListener(this);
        mFontFragment = new LabelFragment();
        mFontFragment.setDateType("font");
        mFontFragment.setClickListener(this);
        mWallpaperFragment = new LabelFragment();
        mWallpaperFragment.setDateType("wallpaper");
        mWallpaperFragment.setClickListener(this);
        mThemeFragment = new LabelFragment();
        mThemeFragment.setDateType("theme");
        mThemeFragment.setClickListener(this);
        mFragments.add(mRecommendFragment);
        mFragments.add(mFontFragment);
        mFragments.add(mWallpaperFragment);
        mFragments.add(mThemeFragment);
        mTitles.add(RECOMMEND);
        mTitles.add(FONT);
        mTitles.add(WALLPAPER);
        mTitles.add(THEME);
        mPageAdapter = new PageAdapter(getSupportFragmentManager(), mFragments, mTitles);
        mViewPager.setAdapter(mPageAdapter);
        //一次加载所有的页面,默认是一次加载两个页面，所以在切换的时候会有一点问题
        mViewPager.setOffscreenPageLimit(mTitles.size());
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_label);
    }

    @Override
    public void onClick(String name) {
        Intent intent = new Intent();
        intent.putExtra("text", name);
        if (mXy != null) {
            intent.putExtra("xy", mXy);
        }
        setResult(RESULT_OK, intent);
        finish();
    }
}
