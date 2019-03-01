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

public class FindFragment extends BaseFragment {
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private static String CHOICE = "编辑精选";
    private static String SHOP = "创意商店";
    private static String ATTENTION = "值得关注";

    private ChoiceFragment mChoiceFragment;
    private ShopFragment mShopFragment;
    private AttentionFragment mAttentionFragment;
    private PageAdapter mPageAdapter;
    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find, container, false);
    }

    @Override
    public void init() {
        super.init();
        mChoiceFragment = new ChoiceFragment();
        mShopFragment = new ShopFragment();
        mAttentionFragment = new AttentionFragment();
        mFragments.add(mChoiceFragment);
        mFragments.add(mShopFragment);
        mFragments.add(mAttentionFragment);
        mTitles.add(CHOICE);
        mTitles.add(SHOP);
        mTitles.add(ATTENTION);
        mPageAdapter = new PageAdapter(getChildFragmentManager(), mFragments, mTitles);
        mViewPager.setAdapter(mPageAdapter);
        //一次加载所有的页面,默认是一次加载两个页面，所以在切换的时候会有一点问题
        mViewPager.setOffscreenPageLimit(mTitles.size());
        mViewPager.setCurrentItem(1);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    public void goToChoiceFragment() {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(0);
        }
    }
}
