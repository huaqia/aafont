package com.xinmei365.font.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.xinmei365.font.R;
import com.xinmei365.font.model.Note;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xinmei365.font.model.User;
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
    private String mLastTime;
    private Context mContext;
    private String mDataType;
    private String mKey;
    private String mId;
    private String mFavoriteId;
    private String mLikeId;
    private boolean mNeedEmptyView;
    private BroadcastReceiver mNoteListChangeReceiver;

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
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new NoteAdapter(mContext, 2);
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.autoRefresh();
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
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

    private void fetchData(final boolean needEmptyView, final int type) {
        BmobQuery<Note> query = new BmobQuery<>();
        query.include("user");
        query.order("-createdAt");
        query.setLimit(PAGE_LIMIT);
        if (type == LOAD_MORE && mLastTime != null) {
            Date date = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                date = dateFormat.parse(mLastTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (date != null) {
                query.addWhereLessThanOrEqualTo("createdAt", new BmobDate(date));
            }
        }
        if (!TextUtils.isEmpty(mDataType)) {
            if (mDataType.equals("font")) {
                query.addWhereEqualTo("type", "字体");
            } else if (mDataType.equals("wallpaper")) {
                query.addWhereEqualTo("type", "壁纸");
            } else if (mDataType.equals("theme")) {
                query.addWhereEqualTo("type", "主题");
            } else if (mDataType.equals("recommend")) {
                query.addWhereGreaterThan("priority", 0);
                query.order("-priority");
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
                        } else {
                            mSwipeRefreshLayout.setEnableLoadmore(true);
                        }
                        mAdapter.setData(mNotes);
                        mLastTime = list.get(list.size() - 1).getCreatedAt();
                    } else {
                        mSwipeRefreshLayout.setEnableLoadmore(false);
                        mAdapter.notifyDataSetChanged();
                        if (needEmptyView) {
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