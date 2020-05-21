package com.xinmei365.font.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Label;
import com.xinmei365.font.ui.adapter.LabelAdapter;
import com.xinmei365.font.utils.BackendUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class LabelFragment extends BaseFragment {
    private static final int PULL_REFRESH = 0;
    private static final int LOAD_MORE = 1;

    private static final int PAGE_LIMIT = 10;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeLayout_follow)
    SmartRefreshLayout mSwipeRefreshLayout;

    private List<Label> mLabels = new ArrayList<>();
    private String mLastTime;
    private Context mContext;
    private String mDataType;
    private LabelAdapter mAdapter;
    private LabelAdapter.OnLabelClickListener mClickListener;

    public void setDateType(String type) {
        mDataType = type;
    }

    public void setClickListener(LabelAdapter.OnLabelClickListener clickListener) {
        mClickListener = clickListener;
    }
    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void init() {
        super.init();
        mContext = getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new LabelAdapter(mContext);
        mAdapter.setClickListener(mClickListener);
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
    }

    private void fetchData(final int type) {
        BmobQuery<Label> query = new BmobQuery<>();
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
            query.addWhereEqualTo("type", mDataType);
        }
        query.findObjects(new FindListener<Label>() {
            @Override
            public void done(List<Label> list, BmobException e) {
                if (e == null) {
                    if (type == PULL_REFRESH) {
                        mLabels.clear();
                    }
                    if (list.size() > 0) {
                        mLabels.addAll(list);
                        if (list.size() < PAGE_LIMIT) {
                            mSwipeRefreshLayout.setEnableLoadmore(false);
                        } else {
                            mSwipeRefreshLayout.setEnableLoadmore(true);
                        }
                        mAdapter.setData(mLabels);
                        mLastTime = list.get(list.size() - 1).getCreatedAt();
                    } else {
                        mSwipeRefreshLayout.setEnableLoadmore(false);
                        mAdapter.notifyDataSetChanged();
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
