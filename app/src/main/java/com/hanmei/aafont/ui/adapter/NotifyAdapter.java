package com.hanmei.aafont.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanmei.aafont.R;
import com.hanmei.aafont.model.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class NotifyAdapter extends RecyclerView.Adapter<NotifyAdapter.NotifyViewHolder> {

    private List<User> userList = new ArrayList<>();
    private Context context;
    private onInfoClickListener onInfoClickListener;
    private onAgreeClickListener onAgreeClickListener;
    private onDisagreeClickListener onDisagreeClickListener;

    public NotifyAdapter(List<User> users, Context context) {
        this.userList = users;
        this.context = context;
    }

    public interface onInfoClickListener {
        void onClick(int position);
    }

    public void setOnInfoClickListener(onInfoClickListener listener) {
        this.onInfoClickListener = listener;
    }

    public interface onAgreeClickListener {
        void onClick(int position);
    }

    public void setOnAgreeClickListener(onAgreeClickListener listener) {
        this.onAgreeClickListener = listener;
    }

    public interface onDisagreeClickListener {
        void onClick(int position);
    }

    public void setOnDisagreeClickListener(onDisagreeClickListener listener) {
        this.onDisagreeClickListener = listener;
    }


    @Override
    public NotifyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        NotifyViewHolder viewHolder = null;
        View view = LayoutInflater.from(context).inflate(R.layout.item_notify, parent, false);
        viewHolder = new NotifyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NotifyViewHolder holder, final int position) {
        User user = userList.get(position);
        if (onDisagreeClickListener != null) {
            holder.follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDisagreeClickListener.onClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class NotifyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.action_icon)
        ImageView actionIcon;
        @BindView(R.id.iv_avatar)
        CircleImageView ivAvatar;
        @BindView(R.id.profile_name)
        TextView profileName;
        @BindView(R.id.action_info)
        TextView actionInfo;
        @BindView(R.id.action_time)
        TextView actionTime;
        @BindView(R.id.product_pic)
        ImageView productPic;
        @BindView(R.id.btn_follow)
        Button follow;

        public NotifyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}