package com.xinmei365.font.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.User;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.MiscUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactFriendsActivity extends BaseActivity {
    private static final int PULL_REFRESH = 0;
    private static final int LOAD_MORE = 1;
    private static final int PAGE_LIMIT = 20;

    @BindView(R.id.close)
    AppCompatImageView mClose;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeLayout)
    SmartRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.edit_query)
    AppCompatEditText mEdit;

    private ContactFriendsAdapter mAdapter;
    private String mLastTime;
    private ArrayList<User> mUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        mAdapter = new ContactFriendsAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mSwipeRefreshLayout.autoRefresh();
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                fetchData(PULL_REFRESH, null);
            }
        });
        mSwipeRefreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                fetchData(LOAD_MORE, null);
            }
        });
        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                fetchData(PULL_REFRESH, mEdit.getText().toString());
            }
        });
    }

    private void fetchData(final int type, final String name) {
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("objectId" , BackendUtils.getObjectId());
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    ArrayList<String> focusList = new ArrayList<>();
                    if (list.size() == 1) {
                        User user = list.get(0);
                        if (user.getFocusIds() != null) {
                            focusList.addAll(user.getFocusIds());
                        }
                    }
                    fetchToData(type, focusList, name);
                } else {
                    BackendUtils.handleException(e, ContactFriendsActivity.this);
                    if (type == PULL_REFRESH) {
                        mSwipeRefreshLayout.finishRefresh();
                    } else {
                        mSwipeRefreshLayout.finishLoadmore();
                    }
                }
            }
        });
    }

    private void fetchToData(final int type, final ArrayList<String> focusList, String name) {
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
        query.addWhereContainedIn("objectId", focusList);
        if (!TextUtils.isEmpty(name)) {
            query.addWhereEqualTo("nickName", name);
        }
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    if (type == PULL_REFRESH) {
                        mUsers.clear();
                    }
                    if (list.size() > 0) {
                        mUsers.addAll(list);
                        if (list.size() < PAGE_LIMIT) {
                            mSwipeRefreshLayout.setEnableLoadmore(false);
                        } else {
                            mSwipeRefreshLayout.setEnableLoadmore(true);
                        }
                        mLastTime = list.get(list.size() - 1).getCreatedAt();
                    } else {
                        mSwipeRefreshLayout.setEnableRefresh(false);
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    BackendUtils.handleException(e, ContactFriendsActivity.this);
                }
                if (type == PULL_REFRESH) {
                    mSwipeRefreshLayout.finishRefresh();
                } else {
                    mSwipeRefreshLayout.finishLoadmore();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setMyContentView() {
        setContentView(R.layout.activity_contact_friends);
    }

    class ContactFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ContactFriendsViewHolder(inflater.inflate(R.layout.item_contact_friend, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ContactFriendsViewHolder) {
                ContactFriendsViewHolder viewHolder = (ContactFriendsViewHolder)holder;
                final User user = mUsers.get(position);
                if (user.getAvatar() != null) {
                    Glide.with(MyApplication.getInstance())
                            .load(user.getAvatar())
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(viewHolder.mPreview);
                }
                viewHolder.mName.setText(user.getNickName());
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            BmobIMUserInfo info = new BmobIMUserInfo(user.getObjectId(), user.getNickName(), user.getAvatar());
                            BmobIMConversation conversationEntrance = BmobIM.getInstance().startPrivateConversation(info, null);
                            if (conversationEntrance != null) {
                                Intent intent = new Intent(ContactFriendsActivity.this, ChatActivity.class);
                                intent.putExtra("conversation", conversationEntrance);
                                startActivity(intent);
                            }
                        } catch (IllegalArgumentException e) {
                            MiscUtils.makeToast(ContactFriendsActivity.this, "连接服务器异常，请稍后再试！", false);
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }
    }

    class ContactFriendsViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mName;
        public CircleImageView mPreview;
        public ContactFriendsViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView)itemView.findViewById(R.id.name);
            mPreview = (CircleImageView) itemView.findViewById(R.id.preview);
        }
    }
}
