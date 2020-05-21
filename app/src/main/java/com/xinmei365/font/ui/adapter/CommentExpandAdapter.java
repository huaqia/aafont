package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.R;
import com.xinmei365.font.model.Comment;
import com.xinmei365.font.model.Reply;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.UserActivity;
import com.xinmei365.font.utils.BackendUtils;
import com.xinmei365.font.utils.MiscUtils;

import java.util.ArrayList;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentExpandAdapter extends BaseExpandableListAdapter {

    private static final String TAG = "CommentExpandAdapter";
    private ArrayList<Comment> commentList;
    private Context context;

    public CommentExpandAdapter(Context context) {
        this.context = context;
    }

    public void setData(ArrayList<Comment> commentList) {
        this.commentList = commentList;
    }
    //评论数量
    @Override
    public int getGroupCount() {
        if (commentList == null) {
            return 0;
        } else {
            return commentList.size();
        }
    }

    //当前评论下的回复数
    @Override
    public int getChildrenCount(int groupPosition) {
        if (commentList.get(groupPosition).getReplyIds() == null) {
            return 0;
        } else {
            return commentList.get(groupPosition).getReplyIds().size();
        }
    }

    //返回评论数据
    @Override
    public Object getGroup(int groupPosition) {
        return commentList.get(groupPosition);
    }

    //返回当前评论下的回复数据
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (commentList.get(groupPosition).getReplyIds() == null) {
            return 0;
        } else {
            return commentList.get(groupPosition).getReplyIds().get(childPosition);
        }
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getCombinedChildId(groupPosition, childPosition);
    }

    //评论和对应的回复是否持有稳定的Id
    @Override
    public boolean hasStableIds() {
        return true;
    }

    //返回group视图
    @Override
    public View getGroupView(final int groupPosition, boolean isExpand, View converView, ViewGroup viewGroup) {
        final GroupHolder groupHolder;
        if (converView == null) {
            converView = LayoutInflater.from(context).inflate(R.layout.item_comment_layout, viewGroup, false);
            groupHolder = new GroupHolder(converView);
            converView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) converView.getTag();
        }
        final Comment comment = commentList.get(groupPosition);
        final User user = comment.getUser();
        final User currentUser = BmobUser.getCurrentUser(User.class);
        if (user.getObjectId().equals(currentUser.getObjectId())) {
            groupHolder.tv_remove.setVisibility(View.VISIBLE);
        } else {
            groupHolder.logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, UserActivity.class);
                    intent.putExtra("id", user.getObjectId());
                    context.startActivity(intent);
                }
            });
            groupHolder.tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, UserActivity.class);
                    intent.putExtra("id", user.getObjectId());
                    context.startActivity(intent);
                }
            });
        }
        groupHolder.tv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscUtils.showAskDialog(context, "确定要删除该条评论？" , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        comment.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    commentList.remove(comment);
                                    notifyDataSetChanged();
                                } else {
                                    BackendUtils.handleException(e, context);
                                }
                            }
                        });
                    }
                });
            }
        });
        if (user.getAvatar() != null) {
            Glide.with(context)
                    .load(user.getAvatar())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(groupHolder.logo);
        }
        groupHolder.tv_name.setText(user.getNickName());
        groupHolder.tv_time.setText(comment.getCreatedAt());
        groupHolder.tv_content.setText(comment.getContent());
        if (groupPosition == 0) {
            groupHolder.gang.setVisibility(View.GONE);
        } else {
            groupHolder.gang.setVisibility(View.VISIBLE);
        }
        return converView;
    }

    //返回评论对应回复的视图
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean b, View converView, ViewGroup viewGroup) {
        final ChildHolder childHolder;
        if (converView == null){
            converView = LayoutInflater.from(context).inflate(R.layout.item_comment_reply,viewGroup,false);
            childHolder = new ChildHolder(converView);
            converView.setTag(childHolder);
        }else {
            childHolder = (ChildHolder)converView.getTag();
        }

        final Reply reply = commentList.get(groupPosition).getReplyIds().get(childPosition);
        final User user = reply.getUser();
        Glide.with(context).load(user.getAvatar())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(childHolder.reply_logo);
        childHolder.reply_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra("id", user.getObjectId());
                context.startActivity(intent);
            }
        });
        childHolder.tv_time.setText(reply.getCreatedAt());
        String userNickName = user.getNickName();
        if (!TextUtils.isEmpty(userNickName)){
            childHolder.tv_name.setText(userNickName);
        }else{
            childHolder.tv_name.setText("无名");
        }
        childHolder.tv_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra("id", user.getObjectId());
                context.startActivity(intent);
            }
        });
        if (reply.getReplyUser() != null) {
            final User replyUser = reply.getReplyUser();
            childHolder.replied_info.setVisibility(View.VISIBLE);
            childHolder.replied_username.setText(replyUser.getNickName());
            childHolder.replied_username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, UserActivity.class);
                    intent.putExtra("id", replyUser.getObjectId());
                    context.startActivity(intent);
                }
            });
        }
        childHolder.tv_content.setText(commentList.get(groupPosition).getReplyIds().get(childPosition).getContent());
        final User currentUser = BmobUser.getCurrentUser(User.class);
        if (currentUser.getObjectId().equals(currentUser.getObjectId())) {
            childHolder.tv_remove.setVisibility(View.VISIBLE);
        }
        childHolder.tv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscUtils.showAskDialog(context, "确定要删除该条回复？" , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reply.delete(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    commentList.get(groupPosition).getReplyIds().remove(reply);
                                    notifyDataSetChanged();
                                } else {
                                    BackendUtils.handleException(e, context);
                                }
                            }
                        });
                    }
                });
            }
        });
        return converView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class GroupHolder {
        private CircleImageView logo;
        private AppCompatTextView tv_name, tv_content, tv_time, tv_remove;
        private View gang;

        private GroupHolder(View view) {
            logo = (CircleImageView) view.findViewById(R.id.comment_logo);
            tv_name = (AppCompatTextView) view.findViewById(R.id.comment_username);
            tv_time = (AppCompatTextView) view.findViewById(R.id.comment_time);
            tv_content = (AppCompatTextView) view.findViewById(R.id.comment_content);
            tv_remove = (AppCompatTextView) view.findViewById(R.id.remove);
            gang = (View) view.findViewById(R.id.gang);
        }
    }

    private static class ChildHolder{
        private CircleImageView reply_logo;
        private AppCompatTextView tv_name , tv_content, tv_time, tv_remove, replied_username;
        private LinearLayout replied_info;

        private ChildHolder(View view){
            reply_logo = (CircleImageView)view.findViewById(R.id.reply_logo);
            tv_name = (AppCompatTextView)view.findViewById(R.id.reply_username);
            tv_time = (AppCompatTextView) view.findViewById(R.id.reply_time);
            tv_content = (AppCompatTextView)view.findViewById(R.id.reply_content);
            tv_remove = (AppCompatTextView) view.findViewById(R.id.remove);
            replied_info = (LinearLayout) view.findViewById(R.id.replied_info);
            replied_username = (AppCompatTextView) view.findViewById(R.id.replied_username);
        }

    }
}
