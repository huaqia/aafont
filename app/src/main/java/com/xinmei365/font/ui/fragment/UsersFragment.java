package com.xinmei365.font.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xinmei365.font.R;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.adapter.UserAdapter;
import com.xinmei365.font.utils.BackendUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class UsersFragment extends BaseFragment {
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

    private List<User> mUsers = new ArrayList<>();
    private UserAdapter mAdapter;
    private String mLastTime;
    private Context mContext;
    private String mKey;

    public void setKey(String key) {
        mKey = key;
    }

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void init() {
        super.init();
        mContext = getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), linearLayoutManager.getOrientation()));
        mAdapter = new UserAdapter(mContext, false);
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.autoRefresh();
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                fetchData(PULL_REFRESH);
            }
        });
        mSwipeRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                fetchData(LOAD_MORE);
            }
        });
        mEmptyIcon.setImageResource(R.drawable.icon_empty_search_result);
        mEmptyTitle.setText("无搜索结果");
        mEmptyInfo.setText("“换个关键词试试吧”");
    }

    private void fetchData(final int type) {
        BmobQuery<User> query = new BmobQuery<>();
        query.order("-createdAt");
        query.addWhereNotEqualTo("role", 1);
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
        if (!TextUtils.isEmpty(mKey)) {
//            query.addWhereEqualTo("nickName", mKey);
            query.addWhereContains("nickName", mKey);
        }
        final User currentUser = BmobUser.getCurrentUser(User.class);
        query.addWhereNotEqualTo("objectId", currentUser.getObjectId());
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    if (type == PULL_REFRESH) {
                        mUsers.clear();
                    }
                    if (list.size() > 0) {
                        mEmptyView.setVisibility(View.GONE);
                        mUsers.addAll(list);
                        if (list.size() < PAGE_LIMIT) {
                            mSwipeRefreshLayout.setEnableLoadmore(false);
                        } else {
                            mSwipeRefreshLayout.setEnableLoadmore(true);
                        }
                        mAdapter.setData(mUsers);
                        mLastTime = list.get(list.size() - 1).getCreatedAt();
                    } else {
                        mSwipeRefreshLayout.setEnableLoadmore(false);
                        mAdapter.notifyDataSetChanged();
                        mEmptyView.setVisibility(View.VISIBLE);
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
