package com.xinmei365.font.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Banner;
import com.xinmei365.font.model.Note;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.NoteDetailActivity;
import com.xinmei365.font.ui.adapter.NoteAdapter;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.DensityUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class NoteFragment extends BaseFragment {
    private static final int PULL_REFRESH = 0;
    private static final int LOAD_MORE = 1;

    private static final int PAGE_LIMIT = 10;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.empty_view)
    LinearLayout mEmptyView;
    @BindView(R.id.empty_icon)
    AppCompatImageView mEmptyIcon;
    @BindView(R.id.empty_title)
    AppCompatTextView mEmptyTitle;
    @BindView(R.id.empty_info)
    AppCompatTextView mEmptyInfo;
    @BindView(R.id.swipeLayout_follow)
    SmartRefreshLayout mSwipeRefreshLayout;

    private List<Note> mNotes = new ArrayList<>();
    private NoteAdapter mAdapter;
    private int mOffset;
    private Context mContext;
    private String mDataType;
    private String mKey;
    private String mId;
    private String mFavoriteId;
    private String mLikeId;
    private boolean mNeedEmptyView;
    private BroadcastReceiver mNoteListChangeReceiver;
    private View mHeaderView;
    private SliderLayout mSliderLayout;
    private int mOfficialType;

    public void setDateType(String type) {
        mDataType = type;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setFavoriteId(String id) {
        mFavoriteId = id;
    }

    public void setLikeId(String id) {
        mLikeId = id;
    }

    public void setOfficialType(int officialType) {
        mOfficialType = officialType;
    }

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void init() {
        super.init();
        mContext = getActivity();
        User currentUser = BmobUser.getCurrentUser(User.class);
        String currentId = currentUser.getObjectId();
        if (!TextUtils.isEmpty(mKey)) {
            mNeedEmptyView = true;
            mEmptyIcon.setImageResource(R.drawable.icon_empty_search_result);
            mEmptyTitle.setText("无搜索结果");
            mEmptyInfo.setText("“换个关键词试试吧”");
        } else if (currentId.equals(mId) || currentId.equals(mFavoriteId) || currentId.equals(mLikeId)) {
            mNeedEmptyView = true;
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mEmptyView.getLayoutParams();
            params.topMargin = - DensityUtils.dip2px(mContext, 125);
            mEmptyView.setLayoutParams(params);
            if (!TextUtils.isEmpty(mId)) {
                mEmptyIcon.setImageResource(R.drawable.icon_empty_note_result);
                mEmptyTitle.setText("无笔记");
                mEmptyInfo.setText("“美好的一天从发笔记开始”");
            } else if (!TextUtils.isEmpty(mFavoriteId)) {
                mEmptyIcon.setImageResource(R.drawable.icon_empty_favorite_result);
                mEmptyTitle.setText("无收藏");
                mEmptyInfo.setText("“收藏夹里空空如也”");
            } else if (!TextUtils.isEmpty(mLikeId)) {
                mEmptyIcon.setImageResource(R.drawable.icon_empty_like_result);
                mEmptyTitle.setText("无点赞");
                mEmptyInfo.setText("“用心发现身边的美”");
            }
        } else {
            mNeedEmptyView = false;
        }
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    layoutManager.invalidateSpanAssignments();
                    mRecyclerView.invalidateItemDecorations();
                }
            }
        });
        mRecyclerView.getItemAnimator().setAddDuration(0);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.getItemAnimator().setMoveDuration(0);
        mRecyclerView.getItemAnimator().setRemoveDuration(0);
        ((SimpleItemAnimator)mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new NoteAdapter(mContext, 2);
        if (!TextUtils.isEmpty(mDataType)) {
            mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.item_banner_header, mRecyclerView, false);
            mAdapter.setHeaderView(mHeaderView);
            mSliderLayout = (SliderLayout) mHeaderView.findViewById(R.id.recommend_slider_banner);
        }
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.autoRefresh();
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshRecommendHeader();
                fetchData(mNeedEmptyView, PULL_REFRESH);
            }
        });
        mSwipeRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                fetchData(mNeedEmptyView, LOAD_MORE);
            }
        });
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.NOTE_LIST_CHANGE");
        mNoteListChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                fetchData(mNeedEmptyView, PULL_REFRESH);
            }
        };
        broadcastManager.registerReceiver(mNoteListChangeReceiver, intentFilter);
    }

    private void refreshRecommendHeader() {
        if (!TextUtils.isEmpty(mDataType)) {
            BmobQuery<Banner> query = new BmobQuery<>();
            query.order("-createdAt");
            query.addWhereEqualTo("type", mDataType);
            query.findObjects(new FindListener<Banner>() {
                @Override
                public void done(List<Banner> list, BmobException e) {
                    boolean hasData = false;
                    mSliderLayout.removeAllSliders();
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
                                            query.include("user");
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
                            hasData = true;
                            mSliderLayout.addSlider(view);
                        }
                    } else {
                        BackendUtils.handleException(e, getActivity());
                    }
                    if (hasData) {
                        mAdapter.setHeaderView(mHeaderView);
                    } else {
                        mAdapter.removeHeaderView();
                    }
                }
            });
        }
    }

    private void fetchData(final boolean needEmptyView, final int type) {
        BmobQuery<Note> query = new BmobQuery<>();
        query.include("user");
        query.setLimit(PAGE_LIMIT);
        if (type == PULL_REFRESH) {
            mOffset = 0;
        }
        query.setSkip(mOffset);
        String order = "hide,-updatedAt";
        if (!TextUtils.isEmpty(mDataType)) {
            if (mDataType.equals("font")) {
                query.addWhereEqualTo("type", "字体");
            } else if (mDataType.equals("wallpaper")) {
                query.addWhereEqualTo("type", "壁纸");
            } else if (mDataType.equals("theme")) {
                query.addWhereEqualTo("type", "主题");
            } else if (mDataType.equals("recommend")) {
                query.addWhereGreaterThan("priority", 0);
                order = "-priority,-createdAt";
            }
        } else if (!TextUtils.isEmpty(mKey)) {
//            query.addWhereEqualTo("title", mKey);
            query.addWhereContains("title", mKey);
        } else if (!TextUtils.isEmpty(mId)) {
            query.addWhereEqualTo("userId", mId);
        } else if (!TextUtils.isEmpty(mFavoriteId)) {
            query.addWhereContainsAll("favoriteIds", Collections.singletonList(mFavoriteId));
        } else if (!TextUtils.isEmpty(mLikeId)) {
            query.addWhereContainsAll("likeIds", Collections.singletonList(mLikeId));
        }
        BmobQuery<User> innerQuery = new BmobQuery<>();
        if (mOfficialType == 1) {
            innerQuery.addWhereEqualTo("role", 1);
        } else if (mOfficialType == -1) {
            innerQuery.addWhereNotEqualTo("role", 1);
        }
        query.addWhereMatchesQuery("user", "_User", innerQuery);
        query.order(order);
        query.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                if (e == null) {
                    if (type == PULL_REFRESH) {
                        mNotes.clear();
                    }
                    if (list.size() > 0) {
                        mEmptyView.setVisibility(View.GONE);
                        mNotes.addAll(list);
                        if (list.size() < PAGE_LIMIT) {
                            mSwipeRefreshLayout.setEnableLoadmore(false);
                            mOffset += list.size();
                        } else {
                            mSwipeRefreshLayout.setEnableLoadmore(true);
                            mOffset += PAGE_LIMIT;
                        }
                        mAdapter.setData(mNotes);
                    } else {
                        mSwipeRefreshLayout.setEnableLoadmore(false);
                        mAdapter.notifyDataSetChanged();
                        if (needEmptyView && mNotes.isEmpty()) {
                            mEmptyView.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    BackendUtils.handleException(e, mContext);
                }
                if (type == PULL_REFRESH) {
                    mSwipeRefreshLayout.finishRefresh();
                } else {
                    mSwipeRefreshLayout.finishLoadmore();
                }
            }
        });
    }
}