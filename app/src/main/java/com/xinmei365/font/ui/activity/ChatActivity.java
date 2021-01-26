package com.xinmei365.font.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xinmei365.font.R;
import com.xinmei365.font.ui.adapter.ChatAdapter;
import com.xinmei365.font.utils.MiscUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMImageMessage;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageListHandler;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.MessagesQueryListener;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.exception.BmobException;

public class ChatActivity extends BaseActivity implements MessageListHandler {
    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.root_view)
    LinearLayout mRootView;
    @BindView(R.id.conversation_title)
    AppCompatTextView mTitle;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.btn_chat_add)
    AppCompatImageView mChatAdd;
    @BindView(R.id.layout_more)
    LinearLayout mLayoutMore;
    @BindView(R.id.edit_msg)
    AppCompatEditText mEditMsg;
    @BindView(R.id.btn_chat_send)
    AppCompatImageView mChatSend;
    @BindView(R.id.to_picture)
    LinearLayout mToPicture;
    @BindView(R.id.to_camera)
    LinearLayout mToCamera;
    @BindView(R.id.sw_refresh)
    SwipeRefreshLayout mSwipeRefresh;


    private BmobIMConversation mConversationManager;
    private ChatAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private MessageSendListener listener = new MessageSendListener() {
        @Override
        public void onStart(BmobIMMessage msg) {
            super.onStart(msg);
            mAdapter.addMessage(msg);
            mEditMsg.setText("");
            scrollToBottom();
        }

        @Override
        public void done(BmobIMMessage msg, BmobException e) {
            mAdapter.notifyDataSetChanged();
            mEditMsg.setText("");
            //java.lang.NullPointerException: Attempt to invoke virtual method 'void android.widget.TextView.setText(java.lang.CharSequence)' on a null object reference
            scrollToBottom();
            if (e != null) {
                MiscUtils.makeToast(ChatActivity.this, e.getMessage(), false);
            }
        }
    };

    @Override
    public void onMessageReceive(List<MessageEvent> list) {
        for (int i = 0; i < list.size(); i++) {
            addMessage2Chat(list.get(i));
        }
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
        BmobIMConversation conversationEntrance = (BmobIMConversation) getIntent().getSerializableExtra("conversation");
        mConversationManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conversationEntrance);
        mTitle.setText(mConversationManager.getConversationTitle());
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ChatAdapter(this, mConversationManager);
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefresh.setEnabled(true);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mSwipeRefresh.setRefreshing(true);
                queryMessages(null);
            }
        });
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                BmobIMMessage msg = mAdapter.getFirstMessage();
                queryMessages(msg);
            }
        });
        mEditMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayoutMore.setVisibility(View.GONE);
            }
        });
        mEditMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    mChatSend.setImageResource(R.drawable.ic_chat_send);
                } else {
                    mChatSend.setImageResource(R.drawable.ic_chat_sended);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mChatAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLayoutMore.getVisibility() == View.VISIBLE) {
                    mLayoutMore.setVisibility(View.GONE);
                } else {
                    mLayoutMore.setVisibility(View.VISIBLE);
                    hideSoftInputView();
                }
            }
        });
        mChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BmobIM.getInstance().getCurrentStatus().getCode() != ConnectionStatus.CONNECTED.getCode()) {
                    MiscUtils.makeToast(ChatActivity.this, "尚未连接IM服务器", false);
                    return;
                }
                sendMessage();
            }
        });
        mToPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
                intent.putExtra("album", "Camera");//Environment.DIRECTORY_DCIM);
                intent.putExtra("chat", true);//Environment.DIRECTORY_DCIM);
                startActivityForResult(intent, 100);
            }
        });
        mToCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProduceActivity.class);
                intent.putExtra("chat", true);
                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == 100) {
            ArrayList<String> urls = data.getStringArrayListExtra("urls");
            if (urls != null) {
                for (String url : urls) {
                    BmobIMImageMessage image = new BmobIMImageMessage(url);
                    mConversationManager.sendMessage(image, listener);
                }
            }
        }
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_chat);
    }

    private void addMessage2Chat(MessageEvent event) {
        BmobIMMessage msg = event.getMessage();
        if (mConversationManager != null && mConversationManager.getConversationId().equals(event.getConversation().getConversationId())
                && !msg.isTransient()) {
            if (mAdapter.findPosition(msg) < 0) {
                mAdapter.addMessage(msg);
                mConversationManager.updateReceiveStatus(msg);
            }
            scrollToBottom();
        }
    }

    @Override
    protected void onResume() {
        addUnReadMessage();
        BmobIM.getInstance().addMessageListHandler(this);
        BmobNotificationManager.getInstance(this).cancelNotification();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //移除页面消息监听器
        BmobIM.getInstance().removeMessageListHandler(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConversationManager.updateLocalCache();
    }

    private void queryMessages(BmobIMMessage msg) {
        mConversationManager.queryMessages(msg, 10, new MessagesQueryListener() {
            @Override
            public void done(List<BmobIMMessage> list, BmobException e) {
                mSwipeRefresh.setRefreshing(false);
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        mAdapter.addMessages(list);
                        mLayoutManager.scrollToPositionWithOffset(list.size() - 1, 0);
                    }
                } else {
                    MiscUtils.makeToast(ChatActivity.this, e.getMessage() + "(" + e.getErrorCode() + ")", false);
                }
            }
        });
    }

    private void addUnReadMessage() {
        List<MessageEvent> cache = BmobNotificationManager.getInstance(this).getNotificationCacheList();
        if (cache.size() > 0) {
            int size = cache.size();
            for (int i = 0; i < size; i++) {
                MessageEvent event = cache.get(i);
                addMessage2Chat(event);
            }
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        mLayoutManager.scrollToPositionWithOffset(mAdapter.getItemCount() - 1, 0);
    }

    private void sendMessage() {
        String text = mEditMsg.getText().toString();
        if (TextUtils.isEmpty(text.trim())) {
            MiscUtils.makeToast(this, "请输入内容", false);
            return;
        }
        BmobIMTextMessage msg = new BmobIMTextMessage();
        msg.setContent(text);
        mConversationManager.sendMessage(msg, listener);
    }
}

