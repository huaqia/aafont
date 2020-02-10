package com.hanmei.aafont.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanmei.aafont.R;
import com.hanmei.aafont.model.Comment;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentExpandAdapter extends BaseExpandableListAdapter {

    private static final String TAG = "CommentExpandAdapter";
    private ArrayList<Comment> commentList;
    private Context context;
    private Handler handler;

    public CommentExpandAdapter(Context context, ArrayList<Comment> commentList) {
        this.commentList = commentList;
        this.context = context;
        handler = new Handler(){

            @Override
            public void handleMessage(Message msg){
                notifyDataSetChanged();
                super.handleMessage(msg);
            }
        };
    }

    public void refresh(){
        handler.sendMessage(new Message());
    }
    //评论数量
    @Override
    public int getGroupCount() {
        return commentList.size();
    }

    //当前评论下的回复数
    @Override
    public int getChildrenCount(int groupPosition) {
        if (commentList.get(groupPosition).getReplyId() == null) {
            return 0;
        } else {
            return commentList.get(groupPosition).getReplyId().size();
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
        if (commentList.get(groupPosition).getReplyId() == null) {
            return 0;
        } else {
            return commentList.get(groupPosition).getReplyId().get(childPosition);
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
        Glide.with(context).load(R.drawable.user_logo)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(groupHolder.logo);
        groupHolder.tv_name.setText(commentList.get(groupPosition).getUser().getUsername());
        groupHolder.tv_time.setText(commentList.get(groupPosition).getCreatedAt());
        groupHolder.tv_content.setText(commentList.get(groupPosition).getContent());
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

        String replyUser = commentList.get(groupPosition).getReplyId().get(childPosition).getUser().getUsername();
        if (!TextUtils.isEmpty(replyUser)){
            childHolder.tv_name.setText(replyUser + ":");
        }else{
            childHolder.tv_name.setText("无名" + ":");
        }

        childHolder.tv_content.setText(commentList.get(groupPosition).getReplyId().get(childPosition).getContent());
        return converView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private class GroupHolder {
        private CircleImageView logo;
        private AppCompatTextView tv_name, tv_content, tv_time;

        private GroupHolder(View view) {
            logo = (CircleImageView) view.findViewById(R.id.comment_logo);
            tv_name = (AppCompatTextView) view.findViewById(R.id.comment_username);
            tv_time = (AppCompatTextView) view.findViewById(R.id.comment_time);
            tv_content = (AppCompatTextView) view.findViewById(R.id.comment_content);

        }
    }

    private class ChildHolder{
        private AppCompatTextView tv_name , tv_content;

        private ChildHolder(View view){
            tv_name = (AppCompatTextView)view.findViewById(R.id.reply_username);
            tv_content = (AppCompatTextView)view.findViewById(R.id.reply_content);
        }

    }
}
