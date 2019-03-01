package com.hanmei.aafont.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hanmei.aafont.R;
import com.hanmei.aafont.ui.adapter.PageAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class HomeFragment extends BaseFragment {
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private static String FONT = "每日字体";
    private static String FOLLOW = "关注的人";
    private static String SPEAK = "字说字话";

    private FontFragment mFontFragment;
    private FollowFragment mFollowFragment;
    private SpeakFragment mSpeakFragment;
    private PageAdapter mPageAdapter;
    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void init() {
        super.init();
        mFontFragment = new FontFragment();
        mFollowFragment = new FollowFragment();
        mSpeakFragment = new SpeakFragment();
        mFragments.add(mFontFragment);
        mFragments.add(mFollowFragment);
        mFragments.add(mSpeakFragment);
        mTitles.add(FONT);
        mTitles.add(FOLLOW);
        mTitles.add(SPEAK);
        mPageAdapter = new PageAdapter(getChildFragmentManager(), mFragments, mTitles);
        mViewPager.setAdapter(mPageAdapter);
        //一次加载所有的页面,默认是一次加载两个页面，所以在切换的时候会有一点问题
        mViewPager.setOffscreenPageLimit(mTitles.size());
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mViewPager.setCurrentItem(1);
    }
}
