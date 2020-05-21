package com.hanmei.aafont.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.hanmei.aafont.ui.activity.SearchUserActivity;
import com.hanmei.aafont.utils.BackendUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

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
    @BindView(R.id.search_bar)
    LinearLayout mSearchLayout;

    private OAAdapter mOfficialAccountAdapter;
    private FamousAdapter mFamousAdapter;
    private RecommendAdapter mRecommendAdapter;

    private List<User> mOfficialUsers = new ArrayList<>();
    private List<User> mPopularUsers = new ArrayList<>();
    private List<User> mRecommendUsers = new ArrayList<>();

    @Override
    public View createMyView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_attention, container, false);
    }

    @Override
    public void init() {
        super.init();
        mSearchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchUserActivity.class));
            }
        });

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
                    mOutstandingUsersRecyclerView.setVisibility(View.GONE);
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
                    mFamousUsersRecyclerView.setVisibility(View.GONE);
                } else {
                    mOfficialAccountRecyclerView.setVisibility(View.GONE);
                }
            }
        });

        mOfficialAccountAdapter = new OAAdapter();
        mFamousAdapter = new FamousAdapter();
        mRecommendAdapter = new RecommendAdapter();
        LinearLayoutManager officalManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mOfficialAccountRecyclerView.setLayoutManager(officalManager);
        LinearLayoutManager famousManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mFamousUsersRecyclerView.setLayoutManager(famousManager);
        LinearLayoutManager recommendManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecommendUsersRecyclerView.setLayoutManager(recommendManager);
        BmobQuery<User> officalQuery = new BmobQuery<>();
        officalQuery.addWhereEqualTo("official", true);
        officalQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    mOfficialUsers = list;
                    mOfficialAccountRecyclerView.setAdapter(mOfficialAccountAdapter);
                }
            }
        });
        BmobQuery<User> popularQuery = new BmobQuery<>();
        popularQuery.addWhereEqualTo("popular", true);
        popularQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    mPopularUsers = list;
                    mFamousUsersRecyclerView.setAdapter(mFamousAdapter);
                }
            }
        });
        BmobQuery<User> recommendQuery = new BmobQuery<>();
        recommendQuery.addWhereEqualTo("recommend", true);
        recommendQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    mRecommendUsers = list;
                    mRecommendUsersRecyclerView.setAdapter(mRecommendAdapter);
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

    private void setForce(final User user) {
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
                            if (relation.getFocusIds() == null || !relation.getFocusIds().contains(user.getObjectId())) {
                                focusIdList.add(user.getObjectId());
                            }
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
                            relation.setFollowIds(followIdList);
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
                            if (relation.getFollowIds() == null || !relation.getFollowIds().contains(currentUser.getObjectId())) {
                                followIdList.add(currentUser.getObjectId());
                            }
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
            if (!user.getObjectId().equals(currentUser.getObjectId())) {
                BackendUtils.pushMessage(user, "FOLLOW", "消息内容");
            }

        }
    }

    private void setUnForce(final User user){

        final User currentUser = BmobUser.getCurrentUser(User.class);
        {
            BmobQuery<Relation> query = new BmobQuery<>();
            query.addWhereEqualTo("user", currentUser);
            query.findObjects(new FindListener<Relation>() {
                @Override
                public void done(List<Relation> list, BmobException e) {
                    if (e == null) {
                        if (list.size() == 1) {
                            Relation relation = list.get(0);
                            ArrayList<String> focusIdList = relation.getFocusIds();
                            if (relation.getFocusIds() != null && relation.getFocusIds().contains(user.getObjectId())) {
                                focusIdList.remove(user.getObjectId());
                                relation.setFocusIds(focusIdList);
                                relation.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {

                                    }
                                });
                            }
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
                        if (list.size() == 1) {
                            Relation relation = list.get(0);
                            ArrayList<String> followIdList = relation.getFollowIds();
                            followIdList.remove(currentUser.getObjectId());
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
            if (!user.getObjectId().equals(currentUser.getObjectId())) {
                BackendUtils.pushMessage(user, "FOLLOW", "消息内容");
            }


        }
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
                OAViewHolder viewHolder = (OAViewHolder) holder;
                AppCompatTextView name = viewHolder.name;
                CircleImageView preview = viewHolder.preview;
                final AppCompatButton force = viewHolder.force;
                final AppCompatButton unforce = viewHolder.unForce;
                final User user = mOfficialUsers.get(position);
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
                final User currentUser = BmobUser.getCurrentUser(User.class);
                BmobQuery<Relation> query = new BmobQuery<>();
                query.addWhereEqualTo("user", currentUser);
                query.findObjects(new FindListener<Relation>() {
                    @Override
                    public void done(List<Relation> list, BmobException e) {
                        if (e == null) {
                            if (list.size() == 1) {
                                Relation relation = list.get(0);
                                ArrayList<String> relations = relation.getFocusIds();
                                if (relations != null && relations.contains(user.getObjectId())) {
                                    unforce.setVisibility(View.VISIBLE);
                                    force.setVisibility(View.GONE);
                                } else {
                                    unforce.setVisibility(View.GONE);
                                    force.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });
                preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
                force.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        force.setVisibility(View.GONE);
                        unforce.setVisibility(View.VISIBLE);
                        setForce(user);
                    }
                });

                unforce.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        force.setVisibility(View.VISIBLE);
                        unforce.setVisibility(View.GONE);
                        setUnForce(user);
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return mOfficialUsers.size();
        }
    }

    class FamousAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new FamousHolder(inflater.inflate(R.layout.item_famous_user, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof FamousHolder) {
                FamousHolder famousHolder = (FamousHolder) viewHolder;
                AppCompatTextView userName = famousHolder.userName;
                CircleImageView userIcon = famousHolder.userIcon;
                final AppCompatButton force = famousHolder.force;
                final User user = mPopularUsers.get(position);
                userName.setText(user.getUsername());
                if (user.getAvatar() != null) {
                    Glide.with(viewHolder.itemView.getContext())
                            .load(user.getAvatar().getUrl())
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(userIcon);
                } else {
                    Glide.with(viewHolder.itemView.getContext())
                            .load(R.drawable.avatar)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(userIcon);
                }
                force.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setForce(user);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mPopularUsers.size();
        }
    }

    class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new RecommendHolder(inflater.inflate(R.layout.item_recommend_user, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof RecommendHolder) {
                RecommendHolder recommendHolder = (RecommendHolder) viewHolder;
                AppCompatTextView userName = recommendHolder.userName;
                CircleImageView userIcon = recommendHolder.userIcon;
                final AppCompatButton force = recommendHolder.force;
                final AppCompatButton unforce = recommendHolder.unforce;
                final AppCompatButton delete = recommendHolder.delete;
                final AppCompatTextView followContext = recommendHolder.followContext;
                final User user = mRecommendUsers.get(position);
                userName.setText(user.getUsername());
                if (user.getAvatar() != null) {
                    Glide.with(viewHolder.itemView.getContext())
                            .load(user.getAvatar().getUrl())
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(userIcon);
                } else {
                    Glide.with(viewHolder.itemView.getContext())
                            .load(R.drawable.avatar)
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(userIcon);
                }
                BmobQuery<Relation> queryu = new BmobQuery<>();
                queryu.addWhereEqualTo("user", user);
                queryu.findObjects(new FindListener<Relation>() {
                    @Override
                    public void done(List<Relation> list, BmobException e) {
                        if (e == null) {
                            if (list.size() == 1) {
                                Relation relation = list.get(0);
                                final ArrayList<String> followList = relation.getFollowIds();
                                if (followList.size() == 1) {
                                    BmobQuery<User> bmobUser = new BmobQuery<>();
                                    bmobUser.getObject(followList.get(0), new QueryListener<User>() {
                                        @Override
                                        public void done(User user, BmobException e) {
                                            if (e ==null){
                                                followContext.setText(user.getUsername()+"用户已关注");
                                            }
                                        }
                                    });
                                } else if (followList.size() > 1){
                                    BmobQuery<User> bmobUser = new BmobQuery<>();
                                    bmobUser.getObject(followList.get(0), new QueryListener<User>() {
                                        @Override
                                        public void done(User user, BmobException e) {
                                            if (e ==null){
                                                followContext.setText(user.getUsername()+"和其他" + followList.size() + "位用户已关注");
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
                final User currentUser = BmobUser.getCurrentUser(User.class);
                BmobQuery<Relation> query = new BmobQuery<>();
                query.addWhereEqualTo("user", currentUser);
                query.findObjects(new FindListener<Relation>() {
                    @Override
                    public void done(List<Relation> list, BmobException e) {
                        if (e == null) {
                            if (list.size() == 1) {
                                Relation relation = list.get(0);
                                ArrayList<String> relations = relation.getFocusIds();
                                if (relations != null && relations.contains(user.getObjectId())) {
                                    unforce.setVisibility(View.VISIBLE);
                                    force.setVisibility(View.GONE);
                                } else {
                                    unforce.setVisibility(View.GONE);
                                    force.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });
                force.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        force.setVisibility(View.GONE);
                        unforce.setVisibility(View.VISIBLE);
                        setForce(user);
                    }
                });
                unforce.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        force.setVisibility(View.VISIBLE);
                        unforce.setVisibility(View.GONE);
                        setUnForce(user);
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mRecommendUsers.size();
        }
    }

    class OAViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView name;
        public CircleImageView preview;
        public AppCompatButton force;
        public AppCompatButton unForce;

        public OAViewHolder(View itemView) {
            super(itemView);
            name = (AppCompatTextView) itemView.findViewById(R.id.name);
            preview = (CircleImageView) itemView.findViewById(R.id.preview);
            force = (AppCompatButton) itemView.findViewById(R.id.btn_force);
            unForce = (AppCompatButton) itemView.findViewById(R.id.btn_force_yet);
        }
    }

    class FamousHolder extends RecyclerView.ViewHolder {

        public AppCompatTextView userName;
        public CircleImageView userIcon;
        public AppCompatButton force;
        public AppCompatImageView preview;
        public AppCompatTextView cardName;

        public FamousHolder(@NonNull View itemView) {
            super(itemView);
            userName = (AppCompatTextView) itemView.findViewById(R.id.user_name);
            userIcon = (CircleImageView) itemView.findViewById(R.id.user_icon);
            force = (AppCompatButton) itemView.findViewById(R.id.btn_force);
            preview = (AppCompatImageView) itemView.findViewById(R.id.card_image);
            cardName = (AppCompatTextView) itemView.findViewById(R.id.card_name);

        }
    }

    class RecommendHolder extends RecyclerView.ViewHolder {

        public AppCompatButton delete;
        public CircleImageView userIcon;
        public AppCompatTextView userName;
        public AppCompatTextView followContext;
        public AppCompatButton force;
        public AppCompatButton unforce;

        public RecommendHolder(@NonNull View itemView) {
            super(itemView);
            delete = (AppCompatButton) itemView.findViewById(R.id.btn_delete);
            userIcon = (CircleImageView) itemView.findViewById(R.id.user_icon);
            userName = (AppCompatTextView) itemView.findViewById(R.id.user_name);
            followContext = (AppCompatTextView) itemView.findViewById(R.id.follow_content);
            force = (AppCompatButton) itemView.findViewById(R.id.btn_force);
            unforce = (AppCompatButton) itemView.findViewById(R.id.btn_force_yet);
        }
    }
}
