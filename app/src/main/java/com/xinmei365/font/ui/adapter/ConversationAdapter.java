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
import com.xinmei365.font.ui.activity.ChatActivity;
import com.xinmei365.font.utils.MiscUtils;
import com.xinmei365.font.utils.TimeUtils;

import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;
import de.hdodenhof.circleimageview.CircleImageView;

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<BmobIMConversation> mConversations;

    public ConversationAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<BmobIMConversation> conversations) {
        mConversations = conversations;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ConversationViewHolder(inflater.inflate(R.layout.item_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ConversationViewHolder) {
            ConversationViewHolder viewHolder = (ConversationViewHolder)holder;
            final BmobIMConversation conversation = mConversations.get(position);
            String icon = conversation.getConversationIcon();
            if (icon != null) {
                Glide.with(MyApplication.getInstance())
                        .load(icon)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(viewHolder.iv_avatar);
            }
            String title = conversation.getConversationTitle();
            if (title == null) {
                title = conversation.getConversationId();
            }
            viewHolder.tv_name.setText(title);
            List<BmobIMMessage> msgs = conversation.getMessages();
            BmobIMMessage lastMsg = null;
            if (msgs != null && msgs.size() > 0) {
                lastMsg = msgs.get(0);
            }
            String messageContent = null;
            String messageTime = null;
            if (lastMsg != null) {
                messageTime = TimeUtils.getChatTime(lastMsg.getCreateTime());
                if (lastMsg.getMsgType().equals(BmobIMMessageType.TEXT.getType()) || lastMsg.getMsgType().equals("agree")) {
                    messageContent = lastMsg.getContent();
                } else if (lastMsg.getMsgType().equals(BmobIMMessageType.IMAGE.getType())) {
                    messageContent = "[图片]";
                } else if (lastMsg.getMsgType().equals(BmobIMMessageType.VOICE.getType())) {
                    messageContent = "[语音]";
                } else if (lastMsg.getMsgType().equals(BmobIMMessageType.LOCATION.getType())) {
                    messageContent = "[位置]";
                } else if (lastMsg.getMsgType().equals(BmobIMMessageType.VIDEO.getType())) {
                    messageContent = "[视频]";
                } else {
                    messageContent = "[未知]";
                }
            }
            if (messageContent != null) {
                viewHolder.tv_message.setText(messageContent);
            }
            if (messageTime != null) {
                viewHolder.tv_time.setText(messageTime);
            }
            long unread = BmobIM.getInstance().getUnReadCount(conversation.getConversationId());
            if(unread > 0){
                viewHolder.tv_unread.setVisibility(View.VISIBLE);
                viewHolder.tv_unread.setText(String.valueOf(unread));
            }else{
                viewHolder.tv_unread.setVisibility(View.GONE);
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClass(mContext, ChatActivity.class);
                    intent.putExtra("conversation", conversation);
                    mContext.startActivity(intent);
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    MiscUtils.showAskDialog(mContext, "确定要删除这条聊天记录？" , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BmobIM.getInstance().deleteConversation(conversation);
                            mConversations.remove(position);
                            notifyDataSetChanged();
                        }
                    });
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mConversations.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView iv_avatar;
        private AppCompatTextView tv_name;
        private AppCompatTextView tv_message;
        private AppCompatTextView tv_time;
        private AppCompatTextView tv_unread;

        public ConversationViewHolder(View itemView) {
            super(itemView);
            iv_avatar = (CircleImageView) itemView.findViewById(R.id.iv_avatar);
            tv_name = (AppCompatTextView) itemView.findViewById(R.id.tv_name);
            tv_message = (AppCompatTextView) itemView.findViewById(R.id.tv_message);
            tv_time = (AppCompatTextView) itemView.findViewById(R.id.tv_time);
            tv_unread = (AppCompatTextView) itemView.findViewById(R.id.tv_unread);
        }
    }
}
