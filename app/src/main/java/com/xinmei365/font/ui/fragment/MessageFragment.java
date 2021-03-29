package com.xinmei365.font.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.xinmei365.font.R;
import com.xinmei365.font.model.RefreshEvent;
import com.xinmei365.font.ui.activity.ContactFriendsActivity;
import com.xinmei365.font.ui.activity.LikeFavoriteActivity;
import com.xinmei365.font.ui.activity.NewCommentsActivity;
import com.xinmei365.font.ui.activity.NewFollowersActivity;
import com.xinmei365.font.ui.adapter.ConversationAdapter;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;

public class MessageFragment extends BaseFragment {
    @BindView(R.id.root_view)
    LinearLayout mRootView;
    @BindView(R.id.iv_chat)
    AppCompatImageView mIvChat;
    @BindView(R.id.like_favorite)
    LinearLayout mLikeFavorite;
    @BindView(R.id.new_follow)
    LinearLayout mFollow;
    @BindView(R.id.new_comment)
    LinearLayout mComment;
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
    @BindView(R.id.sw_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    private ConversationAdapter mAdapter;
    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void init() {
        super.init();
        mIvChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ContactFriendsActivity.class));
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation()));
        mAdapter = new ConversationAdapter(getContext());
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefresh.setEnabled(true);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mSwipeRefresh.setRefreshing(true);
                updateConversation();
            }
        });
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateConversation();
            }
        });
        mLikeFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LikeFavoriteActivity.class));
            }
        });
        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), NewFollowersActivity.class));
            }
        });
        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), NewCommentsActivity.class));
            }
        });
        mEmptyIcon.setImageResource(R.drawable.icon_empty_message);
        mEmptyTitle.setText("暂无消息");
        mEmptyInfo.setText("“快去找个好友来聊天吧”");
    }

    @Override
    public void onResume() {
        super.onResume();
        mSwipeRefresh.setRefreshing(true);
        updateConversation();
    }

    private void updateConversation() {
        mAdapter.setData(getConversations());
        mAdapter.notifyDataSetChanged();
        mSwipeRefresh.setRefreshing(false);
    }

    @Subscribe
    public void onEventMainThread(RefreshEvent event) {
        //重新刷新列表
        mAdapter.setData(getConversations());
        mAdapter.notifyDataSetChanged();
        mSwipeRefresh.setRefreshing(false);
    }

    @Subscribe
    public void onEventMainThread(OfflineMessageEvent event) {
        //重新刷新列表
        mAdapter.setData(getConversations());
        mAdapter.notifyDataSetChanged();
        mSwipeRefresh.setRefreshing(false);
    }

    @Subscribe
    public void onEventMainThread(MessageEvent event) {
        //重新刷新列表
        mAdapter.setData(getConversations());
        mAdapter.notifyDataSetChanged();
        mSwipeRefresh.setRefreshing(false);
    }

    private List<BmobIMConversation> getConversations() {
        //添加会话
        List<BmobIMConversation> conversationList = new ArrayList<>();
        conversationList.clear();
        List<BmobIMConversation> list = BmobIM.getInstance().loadAllConversation();
        if (list != null && list.size() > 0) {
            mEmptyView.setVisibility(View.GONE);
            for (BmobIMConversation item : list) {
                if (item.getConversationType() == 1) {
                    conversationList.add(item);
                }
            }
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }

        Collections.sort(conversationList, new Comparator<BmobIMConversation>() {
            @Override
            public int compare(BmobIMConversation o1, BmobIMConversation o2) {
                List<BmobIMMessage> msgs1 = o1.getMessages();
                BmobIMMessage lastMsg1 = null;
                if (msgs1 != null && msgs1.size() > 0) {
                    lastMsg1 = msgs1.get(0);
                }
                List<BmobIMMessage> msgs2 = o2.getMessages();
                BmobIMMessage lastMsg2 = null;
                if (msgs2 != null && msgs2.size() > 0) {
                    lastMsg2 = msgs2.get(0);
                }
                if (lastMsg1 != null && lastMsg2 != null) {
                    long timeGap = lastMsg2.getCreateTime() - lastMsg1.getCreateTime();
                    if (timeGap > 0) {
                        return 1;
                    } else if (timeGap < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else {
                    return 0;
                }
            }
        });
        return conversationList;
    }
}
