package com.xinmei365.font.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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

public class RelatedUsersActivity extends BaseActivity {
    private static final int PULL_REFRESH = 0;
    private static final int LOAD_MORE = 1;

    private static final int PAGE_LIMIT = 10;

    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.related_type)
    AppCompatTextView mRelatedType;
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

    private int mType;
    private String mUserId;

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_related_users);
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
        mType = getIntent().getIntExtra("type", 1);
        mUserId = getIntent().getStringExtra("userId");
        if (mType == 1) {
            mRelatedType.setText("我的关注");
            mEmptyIcon.setImageResource(R.drawable.icon_empty_follow_result);
            mEmptyTitle.setText("无关注");
            mEmptyInfo.setText("“快去添加关注吧”");
        } else if (mType == 2) {
            mRelatedType.setText("我的粉丝");
            mEmptyIcon.setImageResource(R.drawable.icon_empty_fans_result);
            mEmptyTitle.setText("无粉丝");
            mEmptyInfo.setText("“快去发布笔记让人关注你吧”");
        } else if (mType == 3) {
            mRelatedType.setText("TA的关注");
            mEmptyIcon.setImageResource(R.drawable.icon_empty_follow_result);
            mEmptyTitle.setText("无关注");
            mEmptyInfo.setText("“TA没有关注任何人”");
        } else if (mType == 4) {
            mRelatedType.setText("TA的粉丝");
            mEmptyIcon.setImageResource(R.drawable.icon_empty_fans_result);
            mEmptyTitle.setText("无粉丝");
            mEmptyInfo.setText("“关注TA即可成为TA的粉丝”");
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        mAdapter = new UserAdapter(this, false);
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
        final String objectId;
        if (mType == 1 || mType == 2) {
            final User currentUser = BmobUser.getCurrentUser(User.class);
            objectId = currentUser.getObjectId();
        } else {
            objectId = mUserId;
        }
        BmobQuery<User> query = new BmobQuery<>();
        if (mType == 1 || mType == 3) {
            query.addWhereEqualTo("objectId", objectId);
            query.findObjects(new FindListener<User>() {
                @Override
                public void done(List<User> list, BmobException e) {
                    if (e == null) {
                        boolean hasContent = false;
                        if (list.size() == 1) {
                            User user = list.get(0);
                            if (user.getFocusIds() != null) {
                                hasContent = true;
                                fetchToData(type, user.getFocusIds(), objectId);
                            }
                        }
                        if (hasContent) {
                            mEmptyView.setVisibility(View.GONE);
                        } else {
                            if (type == PULL_REFRESH) {
                                mSwipeRefreshLayout.finishRefresh();
                            } else {
                                mSwipeRefreshLayout.finishLoadmore();
                            }
                            mEmptyView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        BackendUtils.handleException(e, RelatedUsersActivity.this);
                        if (type == PULL_REFRESH) {
                            mSwipeRefreshLayout.finishRefresh();
                        } else {
                            mSwipeRefreshLayout.finishLoadmore();
                        }
                    }
                }
            });
        } else {
            ArrayList<String> ids = new ArrayList<>();
            ids.add(objectId);
            fetchToData(type, ids, objectId);
        }
    }

    private void fetchToData(final int type, final ArrayList<String> ids, final String objectId) {
        BmobQuery<User> query = new BmobQuery<>();
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
        if (mType == 1 || mType == 3) {
            query.addWhereContainedIn("objectId", ids);
        } else {
            query.addWhereContainsAll("focusIds" , ids);
        }
        query.addWhereNotEqualTo("objectId", objectId);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    if (type == PULL_REFRESH) {
                        mUsers.clear();
                    }
                    boolean hasContent = false;
                    if (list.size() > 0) {
                        hasContent = true;
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
                    }
                    if (hasContent) {
                        mEmptyView.setVisibility(View.GONE);
                    } else {
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
                } else {
                    BackendUtils.handleException(e, RelatedUsersActivity.this);
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
