package com.hanmei.aafont.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.Relation;
import com.hanmei.aafont.model.User;
import com.hanmei.aafont.ui.activity.ContactFriendsActivity;
import com.hanmei.aafont.utils.BackendUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class AttentionFragment extends BaseFragment {
    @BindView(R.id.to_hot_users)
    LinearLayout mToHotUsers;
    @BindView(R.id.to_known_users)
    LinearLayout mToKnownUsers;
    @BindView(R.id.to_official_accounts)
    LinearLayout mToOfficialAccounts;
    @BindView(R.id.hot_users)
    ScrollView mHotUsers;
    @BindView(R.id.known_users)
    ScrollView mKnownUsers;
    @BindView(R.id.official_accounts)
    ScrollView mOfficialAccounts;
    @BindView(R.id.hot_users_icon)
    AppCompatImageView mHotUsersIcon;
    @BindView(R.id.known_users_icon)
    AppCompatImageView mKnownUsersIcon;
    @BindView(R.id.official_accounts_icon)
    AppCompatImageView mOfficialAccountsIcon;
    @BindView(R.id.famous_users)
    LinearLayout mFamousUsers;
    @BindView(R.id.outstanding_users)
    LinearLayout mOutstandingUsers;
    @BindView(R.id.famous_users_recycler_view)
    RecyclerView mFamousUsersRecyclerView;
    @BindView(R.id.outstanding_users_recycler_view)
    RecyclerView mOutstandingUsersRecyclerView;
    @BindView(R.id.recommend_users_recycler_view)
    RecyclerView mRecommendUsersRecyclerView;
    @BindView(R.id.official_accounts_recycler_view)
    RecyclerView mOfficialAccountRecyclerView;
    @BindView(R.id.contact_friends)
    LinearLayout mContactFriends;

    private OAAdapter mOfficialAccountAdapter;

    private List<User> mUsers = new ArrayList<>();

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attention, container, false);
    }

    @Override
    public void init() {
        super.init();
        mToHotUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHotUsers.setVisibility(View.VISIBLE);
                mKnownUsers.setVisibility(View.GONE);
                mOfficialAccounts.setVisibility(View.GONE);
                mHotUsersIcon.setImageResource(R.drawable.create);
                mKnownUsersIcon.setImageResource(R.drawable.liked);
                mOfficialAccountsIcon.setImageResource(R.drawable.liked);
            }
        });
        mToKnownUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHotUsers.setVisibility(View.GONE);
                mKnownUsers.setVisibility(View.VISIBLE);
                mOfficialAccounts.setVisibility(View.GONE);
                mHotUsersIcon.setImageResource(R.drawable.liked);
                mKnownUsersIcon.setImageResource(R.drawable.create);
                mOfficialAccountsIcon.setImageResource(R.drawable.liked);
            }
        });
        mToOfficialAccounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHotUsers.setVisibility(View.GONE);
                mKnownUsers.setVisibility(View.GONE);
                mOfficialAccounts.setVisibility(View.VISIBLE);
                mHotUsersIcon.setImageResource(R.drawable.liked);
                mKnownUsersIcon.setImageResource(R.drawable.liked);
                mOfficialAccountsIcon.setImageResource(R.drawable.create);
            }
        });
        mFamousUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFamousUsersRecyclerView.getVisibility() == View.GONE) {
                    mFamousUsersRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mFamousUsersRecyclerView.setVisibility(View.GONE);
                }
            }
        });
        mOutstandingUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOfficialAccountRecyclerView.getVisibility() == View.GONE) {
                    mOfficialAccountRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    mOfficialAccountRecyclerView.setVisibility(View.GONE);
                }
            }
        });

        mOfficialAccountAdapter = new OAAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mOfficialAccountRecyclerView.setLayoutManager(linearLayoutManager);
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("official", true);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    mUsers = list;
                    mOfficialAccountRecyclerView.setAdapter(mOfficialAccountAdapter);
                }
            }
        });
        mContactFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ContactFriendsActivity.class));
            }
        });
    }

    class OAAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new OAViewHolder(inflater.inflate(R.layout.item_official_account, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof OAViewHolder) {
                OAViewHolder viewHolder = (OAViewHolder)holder;
                AppCompatTextView name = viewHolder.mName;
                AppCompatImageView preview = viewHolder.mPreview;
                AppCompatButton button = viewHolder.mButton;
                final User user = mUsers.get(position);
                name.setText(user.getUsername());
                if (user.getAvatar() != null) {
                    Glide.with(holder.itemView.getContext())
                            .load(user.getAvatar().getUrl())
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(preview);
                } else {
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.avatar)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(preview);
                }
                preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final User currentUser = BmobUser.getCurrentUser(User.class);
                        {
                            BmobQuery<Relation> query = new BmobQuery<>();
                            query.addWhereEqualTo("user", currentUser);
                            query.findObjects(new FindListener<Relation>() {
                                @Override
                                public void done(List<Relation> list, BmobException e) {
                                    if (e == null) {
                                        ArrayList<String> focusIdList = new ArrayList<>();
                                        if (list.size() == 0) {
                                            Relation relation = new Relation();
                                            focusIdList.add(user.getObjectId());
                                            relation.setFocusIds(focusIdList);
                                            relation.setUser(currentUser);
                                            relation.save(new SaveListener<String>() {
                                                @Override
                                                public void done(String s, BmobException e) {
                                                }
                                            });
                                        } else if (list.size() == 1) {
                                            Relation relation = list.get(0);
                                            if (relation.getFocusIds() != null) {
                                                focusIdList.addAll(relation.getFocusIds());
                                            }
                                            focusIdList.add(user.getObjectId());
                                            relation.setFocusIds(focusIdList);
                                            relation.update(new UpdateListener() {
                                                @Override
                                                public void done(BmobException e) {
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }
                        {
                            BmobQuery<Relation> query = new BmobQuery<>();
                            query.addWhereEqualTo("user", user);
                            query.findObjects(new FindListener<Relation>() {
                                @Override
                                public void done(List<Relation> list, BmobException e) {
                                    if (e == null) {
                                        ArrayList<String> followIdList = new ArrayList<>();
                                        if (list.size() == 0) {
                                            Relation relation = new Relation();
                                            followIdList.add(currentUser.getObjectId());
                                            relation.setFocusIds(followIdList);
                                            relation.setUser(user);
                                            relation.save(new SaveListener<String>() {
                                                @Override
                                                public void done(String s, BmobException e) {
                                                }
                                            });
                                        } else if (list.size() == 1) {
                                            Relation relation = list.get(0);
                                            if (relation.getFollowIds() != null) {
                                                followIdList.addAll(relation.getFollowIds());
                                            }
                                            followIdList.add(currentUser.getObjectId());
                                            relation.setFollowIds(followIdList);
                                            relation.update(new UpdateListener() {
                                                @Override
                                                public void done(BmobException e) {
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                            if (user.getObjectId() != currentUser.getObjectId()) {
                                BackendUtils.pushMessage(user, "FOLLOW", "消息内容");
                            }
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

    class OAViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView mName;
        public AppCompatImageView mPreview;
        public AppCompatButton mButton;

        public OAViewHolder(View itemView) {
            super(itemView);
            mName = (AppCompatTextView) itemView.findViewById(R.id.name);
            mPreview = (AppCompatImageView) itemView.findViewById(R.id.preview);
            mButton = (AppCompatButton) itemView.findViewById(R.id.button);
        }
    }
}
