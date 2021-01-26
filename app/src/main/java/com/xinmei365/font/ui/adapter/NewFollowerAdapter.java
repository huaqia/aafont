package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.UserActivity;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.DatabaseUtils;
import com.xinmei365.font.utils.MiscUtils;
import com.xinmei365.font.utils.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class NewFollowerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "NewFollowerAdapter";

    private Context mContext;
    private ArrayList<HashMap<String, String>> mMsgs;
    private int mFollowColor;
    private int mFollowedColor;

    public NewFollowerAdapter(Context context) {
        mContext = context;
        mFollowColor = context.getResources().getColor(R.color.colorNormalState);
        mFollowedColor = context.getResources().getColor(R.color.colorActiveState);
    }

    public void setData(ArrayList<HashMap<String, String>> msgs) {
        mMsgs = msgs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new NewFollowerViewHolder(inflater.inflate(R.layout.item_new_follower, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof NewFollowerViewHolder) {
            final NewFollowerViewHolder viewHolder = (NewFollowerViewHolder)holder;
            final HashMap<String, String> msg = mMsgs.get(position);
            final String userId = msg.get("userId");
            if (userId != null) {
                BmobQuery<User> userQuery = new BmobQuery<>();
                userQuery.addWhereEqualTo("objectId", userId);
                userQuery.findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> list, BmobException e) {
                        if (e == null) {
                            if (list.size() == 1) {
                                User user = list.get(0);
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
                            }
                        } else {
                            BackendUtils.handleException(e, mContext);
                        }
                    }
                });
                final User currentUser = BmobUser.getCurrentUser(User.class);
                BmobQuery<User> query = new BmobQuery<>();
                query.addWhereEqualTo("objectId" , currentUser.getObjectId());
                query.findObjects(new FindListener<User>() {
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
                                setFollowAction(viewHolder.mFocusAction, follow, user);
                            }
                        } else {
                            BackendUtils.handleException(e, mContext);
                        }
                    }
                });
            }
            String timeStr = msg.get("time");
            long time = -1;
            if (timeStr != null) {
                time = Long.parseLong(timeStr);
            }
            if (time != -1) {
                viewHolder.mNotice.setText("开始关注你了 " + TimeUtils.getTime(time));
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, UserActivity.class);
                    intent.putExtra("id", userId);
                    mContext.startActivity(intent);
                }
            });
            String idStr = msg.get("id");
            if (idStr != null) {
                final int id = Integer.parseInt(idStr);
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        MiscUtils.showAskDialog(mContext, "确定要删除这个纪录？", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mMsgs.remove(position);
                                DatabaseUtils.deleteMessage(mContext, id);
                                notifyDataSetChanged();
                            }
                        });
                        return false;
                    }
                });
            }
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
        }
    }

    private void setFollowAction(AppCompatTextView view, boolean follow, User user) {
        if (follow) {
            view.setText("回粉");
            view.setTextColor(mFollowedColor);
            view.setBackgroundResource(R.drawable.ic_follow);
        } else {
            view.setText(R.string.unfollow);
            view.setTextColor(mFollowColor);
            view.setBackgroundResource(R.drawable.ic_followed);
            BackendUtils.pushMessage(mContext, user, "FOLLOW", null);
        }
    }

    @Override
    public int getItemCount() {
        if (mMsgs == null) {
            return 0;
        } else {
            return mMsgs.size();
        }
    }

    static class NewFollowerViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mUserIcon;
        public AppCompatTextView mUserName;
        public AppCompatTextView mNotice;
        public AppCompatTextView mFocusAction;

        public NewFollowerViewHolder(View itemView) {
            super(itemView);
            mUserIcon = (CircleImageView) itemView.findViewById(R.id.profile_image);
            mUserName = (AppCompatTextView) itemView.findViewById(R.id.user_name);
            mNotice = (AppCompatTextView) itemView.findViewById(R.id.notice);
            mFocusAction = (AppCompatTextView) itemView.findViewById(R.id.focus_action);
        }
    }
}