package com.xinmei365.font.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xinmei365.font.R;
import com.xinmei365.font.ui.activity.MainActivity;
import com.xinmei365.font.ui.activity.SearchActivity;
import com.xinmei365.font.ui.adapter.PageAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ForumFragment extends BaseFragment {
    private static final String TAG = "ForumFragment";

    private static String FOLLOW = "关注";
    private static String FORUM = "广场";
    @BindView(R.id.frame_search)
    LinearLayout mSearch;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.create_new)
    AppCompatImageView mCreate;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    private FollowFragment mFollowFragment;
    private NoteFragment mForumFragment;
    private PageAdapter mPageAdapter;
    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forum, container, false);
    }

    @Override
    public void init() {
        super.init();
        mFollowFragment = new FollowFragment();
        mForumFragment = new NoteFragment();
        mForumFragment.setDateType("forum");
        mForumFragment.setOfficialType(-1);
        mFragments.add(mFollowFragment);
        mFragments.add(mForumFragment);
        mTitles.add(FOLLOW);
        mTitles.add(FORUM);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("officialType", -1);
                startActivity(intent);
            }
        });
        mPageAdapter = new PageAdapter(getChildFragmentManager(), mFragments, mTitles);
        mViewPager.setAdapter(mPageAdapter);
        //一次加载所有的页面,默认是一次加载两个页面，所以在切换的时候会有一点问题
        mViewPager.setOffscreenPageLimit(mTitles.size());
        mCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).showCreateDialog();
            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mViewPager.setCurrentItem(0);
    }
}
