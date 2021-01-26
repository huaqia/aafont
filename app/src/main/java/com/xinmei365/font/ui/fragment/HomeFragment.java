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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Banner;
import com.xinmei365.font.model.Note;
import com.xinmei365.font.ui.activity.MainActivity;
import com.xinmei365.font.ui.activity.NoteDetailActivity;
import com.xinmei365.font.ui.activity.SearchActivity;
import com.xinmei365.font.ui.activity.WebViewActivity;
import com.xinmei365.font.ui.adapter.PageAdapter;
import com.xinmei365.font.utils.BackendUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class HomeFragment extends BaseFragment {
    private static final String TAG = "HomeFragment";

    private static String RECOMMEND = "推荐";
    private static String FONT = "字体";
    private static String WALLPAPER = "壁纸";
    private static String THEME = "主题";
    @BindView(R.id.frame_search)
    LinearLayout mSearch;
    @BindView(R.id.recommend_slider_banner)
    SliderLayout mSliderLayout;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.create_new)
    AppCompatImageView mCreate;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    private NoteFragment mRecommendFragment;
    private NoteFragment mFontFragment;
    private NoteFragment mWallpaperFragment;
    private NoteFragment mThemeFragment;
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
        mRecommendFragment = new NoteFragment();
        mRecommendFragment.setDateType("recommend");
        mFontFragment = new NoteFragment();
        mFontFragment.setDateType("font");
        mWallpaperFragment = new NoteFragment();
        mWallpaperFragment.setDateType("wallpaper");
        mThemeFragment = new NoteFragment();
        mThemeFragment.setDateType("theme");
        mFragments.add(mRecommendFragment);
        mFragments.add(mFontFragment);
        mFragments.add(mWallpaperFragment);
        mFragments.add(mThemeFragment);
        mTitles.add(RECOMMEND);
        mTitles.add(FONT);
        mTitles.add(WALLPAPER);
        mTitles.add(THEME);
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
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
        BmobQuery<Banner> query = new BmobQuery<>();
        query.order("-createdAt");
        query.findObjects(new FindListener<Banner>() {
                              @Override
                              public void done(List<Banner> list, BmobException e) {
                                  if (e == null) {
                                      for (final Banner banner : list) {
                                          BaseSliderView view = new DefaultSliderView(getContext()).image(banner.getImage());
                                          view.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                              @Override
                                              public void onSliderClick(BaseSliderView slider) {
                                                  String url = banner.getUrl();
                                                  if (!TextUtils.isEmpty(url)) {
                                                      if (url.startsWith("http://")) {
                                                          Uri uri = Uri.parse(url);
                                                          startActivity(new Intent(Intent.ACTION_VIEW, uri));
//                                                          Uri uri = Uri.parse("http://zhuti.xiaomi.com/detail/d26f65c6-11e0-4b98-a91e-5f0b587bb541");
//                                                          startActivity(new Intent(Intent.ACTION_VIEW, uri));
//                                                          Intent intent = new Intent(getContext(), WebViewActivity.class);
//                                                          intent.putExtra("url", "http://zhuti.xiaomi.com/detail/d26f65c6-11e0-4b98-a91e-5f0b587bb541");
//                                                          getContext().startActivity(intent);
//                                                          Intent new_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("newthemedetail://newthemehost?pkg=com.bbk.theme&restype=4&id=400014747")); //oaps://theme/detail?rtp=font&id=2246947&openinsystem=true&from=h5
//                                                          startActivity(new_intent);
                                                      } else {
                                                          BmobQuery<Note> query = new BmobQuery<>();
                                                          query.addWhereEqualTo("objectId", url);
                                                          query.findObjects(new FindListener<Note>() {
                                                              @Override
                                                              public void done(List<Note> list, BmobException e) {
                                                                  if (e == null) {
                                                                      if (list.size() == 1) {
                                                                          final Note note = list.get(0);
                                                                          Intent intent = new Intent(getContext(), NoteDetailActivity.class);
                                                                          intent.putExtra("note", note);
                                                                          getContext().startActivity(intent);
                                                                      }
                                                                  } else {
                                                                      BackendUtils.handleException(e, getActivity());
                                                                  }
                                                              }
                                                          });


                                                      }
                                                  }
                                              }
                                          });
                                          mSliderLayout.addSlider(view);
                                      }
                                  } else {
                                      BackendUtils.handleException(e, getActivity());
                                  }
                              }
                          });
    }
}
