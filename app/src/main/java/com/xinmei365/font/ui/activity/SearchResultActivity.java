package com.xinmei365.font.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.LinearLayout;

import com.xinmei365.font.R;
import com.xinmei365.font.ui.adapter.PageAdapter;
import com.xinmei365.font.ui.fragment.NoteFragment;
import com.xinmei365.font.ui.fragment.UsersFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class SearchResultActivity extends BaseActivity {
    private static String NOTE = "笔记";
    private static String USER = "用户";

    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.result_name)
    AppCompatTextView mResultName;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    private NoteFragment mNoteFragment;
    private UsersFragment mUserFragment;
    private PageAdapter mPageAdapter;
    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_search_result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        final String key = getIntent().getStringExtra("key");
        mResultName.setText(key);
        mNoteFragment = new NoteFragment();
        mNoteFragment.setKey(key);
        mUserFragment = new UsersFragment();
        mUserFragment.setKey(key);
        mFragments.add(mNoteFragment);
        mFragments.add(mUserFragment);
        mTitles.add(NOTE);
        mTitles.add(USER);
        mPageAdapter = new PageAdapter(getSupportFragmentManager(), mFragments, mTitles);
        mViewPager.setAdapter(mPageAdapter);
        //一次加载所有的页面,默认是一次加载两个页面，所以在切换的时候会有一点问题
        mViewPager.setOffscreenPageLimit(mTitles.size());
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
    }
}