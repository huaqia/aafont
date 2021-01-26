package com.xinmei365.font.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Note;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.MainActivity;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xinmei365.font.ui.activity.SearchActivity;
import com.xinmei365.font.ui.adapter.NoteAdapter;
import com.xinmei365.font.ui.adapter.RecommendAdapter;
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
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowFragment extends BaseFragment {
    private static final int PULL_REFRESH = 0;
    private static final int LOAD_MORE = 1;

    private static final int PAGE_LIMIT = 10;
    private static final int RECOMMEND_LIMIT = 20;

    private static final String TAG = "FollowFragment";

    @BindView(R.id.frame_search)
    LinearLayout mSearch;
    @BindView(R.id.create_new)
    LinearLayout mCreateNew;
    @BindView(R.id.profile_image)
    CircleImageView mUserIcon;
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
    @BindView(R.id.recommend_text)
    AppCompatTextView mRecommendText;
    @BindView(R.id.recommend_recycler_view)
    RecyclerView mRecommendRecyclerView;
    @BindView(R.id.swipeLayout_follow)
    SmartRefreshLayout mSwipeRefreshLayout;

    private List<Note> mNotes = new ArrayList<>();
    private NoteAdapter mAdapter;
    private RecommendAdapter mRecommendAdapter;
    private String mLastTime;
    private Context mContext;

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow, container, false);
    }

    @Override
    public void init() {
        super.init();
        mContext = getActivity();
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mAdapter = new NoteAdapter(mContext, 2);
        mRecyclerView.setAdapter(mAdapter);
        mRecommendRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRecommendAdapter = new RecommendAdapter(mContext);
        mRecommendRecyclerView.setAdapter(mRecommendAdapter);
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
        BmobQuery<User> queryU = new BmobQuery<>();
        final User currentUser = BmobUser.getCurrentUser(User.class);
        queryU.addWhereEqualTo("objectId" , currentUser.getObjectId());
        queryU.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null){
                    User user = list.get(0);
                    if (user.getAvatar() != null) {
                        Glide.with(MyApplication.getInstance())
                                .load(user.getAvatar())
                                .fitCenter()
                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                .into(mUserIcon);
                    }
                } else {
                    BackendUtils.handleException(e, mContext);
                }
            }
        });
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
        mCreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).showCreateDialog();
            }
        });
        mEmptyIcon.setImageResource(R.drawable.icon_empty_follow_result);
        mEmptyTitle.setText("还没有关注的人哦");
        mEmptyInfo.setText("“关注后，可以在这里查看对方的最新状态”");
    }

    private void fetchData(final int type) {
        final User currentUser = BmobUser.getCurrentUser(User.class);
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("objectId" , currentUser.getObjectId());
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    boolean hasContent = false;
                    if (list.size() == 1) {
                        User user = list.get(0);
                        if (user.getFocusIds() != null) {
                            hasContent = true;
                            fetchToData(type, user.getFocusIds());
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
                        fetchRecommendData();
                    }
                } else {
                    BackendUtils.handleException(e, mContext);
                    if (type == PULL_REFRESH) {
                        mSwipeRefreshLayout.finishRefresh();
                    } else {
                        mSwipeRefreshLayout.finishLoadmore();
                    }
                }
            }
        });
    }

    private void fetchToData(final int type, final ArrayList<String> focusList) {
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
        query.addWhereContainedIn("userId", focusList);
        query.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                if (e == null) {
                    if (type == PULL_REFRESH) {
                        mNotes.clear();
                    }
                    boolean hasContent = false;
                    if (list.size() > 0) {
                        hasContent = true;
                        mNotes.addAll(list);
                        if (list.size() < PAGE_LIMIT) {
                            mSwipeRefreshLayout.setEnableLoadmore(false);
                        } else {
                            mSwipeRefreshLayout.setEnableLoadmore(true);
                        }
                        mAdapter.setData(mNotes);
                        mLastTime = list.get(list.size() - 1).getCreatedAt();
                    } else if (type == LOAD_MORE) {
                        mSwipeRefreshLayout.setEnableLoadmore(false);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mSwipeRefreshLayout.setEnableLoadmore(false);
                    }
                    if (hasContent) {
                        mEmptyView.setVisibility(View.GONE);
                    } else {
                        mEmptyView.setVisibility(View.VISIBLE);
                        fetchRecommendData();
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

    private void fetchRecommendData() {
        BmobQuery<Note> query = new BmobQuery<>();
        query.include("user");
        query.order("-hot");
        final User currentUser = BmobUser.getCurrentUser(User.class);
        query.addWhereNotEqualTo("userId", currentUser.getObjectId());
        query.setLimit(RECOMMEND_LIMIT);
        query.findObjects(new FindListener<Note>() {
            @Override
            public void done(List<Note> list, BmobException e) {
                if (e == null) {
                    if (list.size() > 0) {
                        ArrayList<String> userIds = new ArrayList<>();
                        ArrayList<User> users = new ArrayList<>();
                        for (Note note : list) {
                            User user = note.getUser();
                            String userId = user.getObjectId();
                            if (!userIds.contains(userId)) {
                                userIds.add(userId);
                                users.add(user);
                            }
                        }
                        if (users.size() > 0) {
                            mRecommendAdapter.setData(users);
                            mRecommendAdapter.notifyDataSetChanged();
                            mRecommendRecyclerView.setVisibility(View.VISIBLE);
                            mRecommendText.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    BackendUtils.handleException(e, mContext);
                }
            }
        });
    }
}
