package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.ActivityCollector;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.LoginActivity;
import com.xinmei365.font.ui.activity.UserActivity;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private int mFollowColor;
    private int mFollowedColor;

    public UserAdapter(Context context, boolean fromFollowers) {
        mContext = context;
        mFollowColor = context.getResources().getColor(R.color.colorNormalState);
        mFollowedColor = context.getResources().getColor(R.color.colorActiveState);
    }

    public void setData(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new UserViewHolder(inflater.inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof UserViewHolder) {
            final UserViewHolder viewHolder = (UserViewHolder)holder;
            final User user = mUsers.get(position);
            if (user.getAvatar() == null) {
                viewHolder.mUserIcon.setImageResource(R.drawable.avatar);
            } else {
                Glide.with(MyApplication.getInstance())
                        .load(user.getAvatar())
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(viewHolder.mUserIcon);
            }
            viewHolder.mUserName.setText(user.getNickName());
            if (!TextUtils.isEmpty(user.getIntro())) {
                viewHolder.mUserIntro.setText(user.getIntro());
            }
            final String userId = user.getObjectId();
            final User currentUser = BmobUser.getCurrentUser(User.class);
            if (!userId.equals(currentUser.getObjectId())) {
                BmobQuery<User> userQuery = new BmobQuery<>();
                userQuery.addWhereEqualTo("objectId" , currentUser.getObjectId());
                userQuery.findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> list, BmobException e) {
                        if (e == null) {
                            boolean follow = true;
                            if (list.size() == 1) {
                                User user = list.get(0);
                                ArrayList<String> relations = user.getFocusIds();
                                if (relations != null && relations.contains(userId)) {
                                    follow = false;
                                }
                            }
                            if (follow) {
                                setFollowAction(viewHolder.mFocusAction, true, user);
                            } else {
                                setFollowAction(viewHolder.mFocusAction, false, user);
                            }
                        } else {
                            BackendUtils.handleException(e, mContext);
                        }
                    }
                });
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, UserActivity.class);
                        intent.putExtra("id", userId);
                        mContext.startActivity(intent);
                    }
                });
                viewHolder.mFocusAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final User currentUser = BmobUser.getCurrentUser(User.class);
                        BmobQuery<User> query = new BmobQuery<>();
                        query.addWhereEqualTo("objectId" , currentUser.getObjectId());
                        query.findObjects(new FindListener<User>() {
                            @Override
                            public void done(List<User> list, BmobException e) {
                                if (e == null) {
                                    final ArrayList<String> focusIdList = new ArrayList<>();
                                    if (list.size() == 1) {
                                        final User user = list.get(0);
                                        if (user.getFocusIds() != null) {
                                            focusIdList.addAll(user.getFocusIds());
                                        }
                                        if (user.getFocusIds() == null || !user.getFocusIds().contains(userId)) {
                                            focusIdList.add(userId);
                                            setFollowAction(viewHolder.mFocusAction, false, user);
                                            BackendUtils.pushMessage(mContext, user, "FOLLOW", null);
                                            user.setFocusIds(focusIdList);
                                            user.update(new UpdateListener() {
                                                @Override
                                                public void done(BmobException e) {
                                                    BackendUtils.handleException(e, mContext);
                                                }
                                            });
                                        } else {
                                            MiscUtils.showAskDialog(mContext, "确定不再关注这位了吗？" , new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    focusIdList.remove(userId);
                                                    setFollowAction(viewHolder.mFocusAction, true, user);
                                                    BackendUtils.pushMessage(mContext, user, "FOLLOW", null);
                                                    user.setFocusIds(focusIdList);
                                                    user.update(new UpdateListener() {
                                                        @Override
                                                        public void done(BmobException e) {
                                                            BackendUtils.handleException(e, mContext);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    BackendUtils.handleException(e, mContext);
                                }
                            }
                        });
                    }
                });
            } else {
                setFollowAction(viewHolder.mFocusAction, true, user);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mContext, "是你自己啦",Toast.LENGTH_SHORT).show();
                    }
                });
                viewHolder.mFocusAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mContext, "自己不能关注自己哦",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mUsers == null) {
            return 0;
        } else {
            return mUsers.size();
        }
    }

    private void setFollowAction(AppCompatTextView view, boolean follow, User user) {
        if (follow) {
            view.setText(R.string.follow);
            view.setTextColor(mFollowedColor);
            view.setBackgroundResource(R.drawable.ic_follow);
        } else {
            view.setText(R.string.unfollow);
            view.setTextColor(mFollowColor);
            view.setBackgroundResource(R.drawable.ic_followed);
            BackendUtils.pushMessage(mContext, user, "FOLLOW", null);
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mUserIcon;
        public AppCompatTextView mUserName;
        public AppCompatTextView mUserIntro;
        public AppCompatTextView mFocusAction;

        public UserViewHolder(View itemView) {
            super(itemView);
            mUserIcon = (CircleImageView) itemView.findViewById(R.id.profile_image);
            mUserName = (AppCompatTextView) itemView.findViewById(R.id.user_name);
            mUserIntro = (AppCompatTextView) itemView.findViewById(R.id.user_intro);
            mFocusAction = (AppCompatTextView) itemView.findViewById(R.id.focus_action);
        }
    }

}