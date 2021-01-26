package com.xinmei365.font.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.xinmei365.font.MyApplication;
import com.xinmei365.font.R;
import com.xinmei365.font.model.User;
import com.xinmei365.font.utils.MiscUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMImageMessage;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;
import cn.bmob.newim.bean.BmobIMSendStatus;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "ChatAdapter";

    private final int TYPE_RECEIVER_TXT = 0;
    private final int TYPE_SEND_TXT = 1;
    private final int TYPE_SEND_IMAGE = 2;
    private final int TYPE_RECEIVER_IMAGE = 3;
    private final long TIME_INTERVAL = 10 * 60 * 1000;

    private Context mContext;
    private BmobIMConversation mConversation;
    private List<BmobIMMessage> mMessages = new ArrayList<>();
    private String mCurrentId="";

    public ChatAdapter(Context context, BmobIMConversation conversation) {
        mContext = context;
        try {
            mCurrentId = BmobUser.getCurrentUser(User.class).getObjectId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mConversation = conversation;
    }

    public int findPosition(BmobIMMessage message) {
        int position = -1;
        for (int i = 0; i < mMessages.size(); i++) {
            if (message.equals(mMessages.get(i))) {
                position = i;
                break;
            }
        }
        return position;
    }

    public void addMessages(List<BmobIMMessage> messages) {
        mMessages.addAll(0, messages);
        notifyDataSetChanged();
    }

    public void addMessage(BmobIMMessage message) {
        mMessages.addAll(Arrays.asList(message));
        notifyDataSetChanged();
    }

    public BmobIMMessage getFirstMessage() {
        if (null != mMessages && mMessages.size() > 0) {
            return mMessages.get(0);
        } else {
            return null;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SEND_TXT) {
            return new SendTextHolder(inflater.inflate(R.layout.item_chat_sent_message, parent, false));
        } else if (viewType == TYPE_RECEIVER_TXT) {
            return new ReceiveTextHolder(inflater.inflate(R.layout.item_chat_received_message, parent, false));
        } else if (viewType == TYPE_SEND_IMAGE) {
            return new SendImageHolder(inflater.inflate(R.layout.item_chat_sent_image, parent, false));
        } else if (viewType == TYPE_RECEIVER_IMAGE) {
            return new ReceiveImageHolder(inflater.inflate(R.layout.item_chat_received_image, parent, false));
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final BmobIMMessage message = mMessages.get(position);
        View.OnLongClickListener listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                MiscUtils.showAskDialog(mContext, "确定要删除这条聊天记录？" , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mConversation.deleteMessage(message);
                        mMessages.remove(position);
                        notifyDataSetChanged();
                    }
                });
                return false;
            }
        };
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = dateFormat.format(message.getCreateTime());
        final BmobIMUserInfo info = message.getBmobIMUserInfo();
        if (holder instanceof SendTextHolder) {
            final SendTextHolder viewHolder = (SendTextHolder)holder;
            fillAvatar(info, message.getFromId(), viewHolder.iv_avatar);
            viewHolder.tv_message.setText(message.getContent());
            viewHolder.tv_time.setText(time);
            if (shouldShowTime(position)) {
                viewHolder.tv_time.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv_time.setVisibility(View.GONE);
            }
            int status = message.getSendStatus();
            if (status == BmobIMSendStatus.SEND_FAILED.getStatus()) {
                viewHolder.iv_fail_resend.setVisibility(View.VISIBLE);
                viewHolder.progress_load.setVisibility(View.GONE);
            } else if (status== BmobIMSendStatus.SENDING.getStatus()) {
                viewHolder.iv_fail_resend.setVisibility(View.GONE);
                viewHolder.progress_load.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_fail_resend.setVisibility(View.GONE);
                viewHolder.progress_load.setVisibility(View.GONE);
            }
            viewHolder.iv_fail_resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mConversation.resendMessage(message, new MessageSendListener() {
                        @Override
                        public void onStart(BmobIMMessage msg) {
                            viewHolder.progress_load.setVisibility(View.VISIBLE);
                            viewHolder.iv_fail_resend.setVisibility(View.GONE);
                            viewHolder.tv_send_status.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                            if (e == null) {
                                viewHolder.tv_send_status.setVisibility(View.VISIBLE);
                                viewHolder.tv_send_status.setText("已发送");
                                viewHolder.iv_fail_resend.setVisibility(View.GONE);
                                viewHolder.progress_load.setVisibility(View.GONE);
                            } else {
                                viewHolder.iv_fail_resend.setVisibility(View.VISIBLE);
                                viewHolder.progress_load.setVisibility(View.GONE);
                                viewHolder.tv_send_status.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            });
            viewHolder.tv_message.setOnLongClickListener(listener);
        } else if (holder instanceof ReceiveTextHolder) {
            final ReceiveTextHolder viewHolder = (ReceiveTextHolder)holder;
            fillAvatar(info, message.getFromId(), viewHolder.iv_avatar);
            viewHolder.tv_message.setText(message.getContent());
            viewHolder.tv_time.setText(time);
            if (shouldShowTime(position)) {
                viewHolder.tv_time.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv_time.setVisibility(View.GONE);
            }
            viewHolder.tv_message.setOnLongClickListener(listener);
        } else if (holder instanceof SendImageHolder) {
            final SendImageHolder viewHolder = (SendImageHolder)holder;
            fillAvatar(info, message.getFromId(), viewHolder.iv_avatar);
            viewHolder.tv_time.setText(time);
            if (shouldShowTime(position)) {
                viewHolder.tv_time.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv_time.setVisibility(View.GONE);
            }
            final BmobIMImageMessage msg = BmobIMImageMessage.buildFromDB(true, message);
            int status = msg.getSendStatus();
            if (status == BmobIMSendStatus.SEND_FAILED.getStatus()) {
                viewHolder.iv_fail_resend.setVisibility(View.VISIBLE);
                viewHolder.progress_load.setVisibility(View.GONE);
            } else if (status== BmobIMSendStatus.SENDING.getStatus()) {
                viewHolder.iv_fail_resend.setVisibility(View.GONE);
                viewHolder.progress_load.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_fail_resend.setVisibility(View.GONE);
                viewHolder.progress_load.setVisibility(View.GONE);
            }
            String picUrl;
            if (TextUtils.isEmpty(msg.getRemoteUrl())) {
                picUrl = msg.getLocalPath();
            } else {
                picUrl = msg.getRemoteUrl();
            }
            Glide.with(MyApplication.getInstance())
                    .load(picUrl)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(viewHolder.iv_picture);
            viewHolder.iv_fail_resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mConversation.resendMessage(message, new MessageSendListener() {
                        @Override
                        public void onStart(BmobIMMessage msg) {
                            viewHolder.progress_load.setVisibility(View.VISIBLE);
                            viewHolder.iv_fail_resend.setVisibility(View.GONE);
                            viewHolder.tv_send_status.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void done(BmobIMMessage bmobIMMessage, BmobException e) {
                            if (e == null) {
                                viewHolder.tv_send_status.setVisibility(View.VISIBLE);
                                viewHolder.tv_send_status.setText("已发送");
                                viewHolder.iv_fail_resend.setVisibility(View.GONE);
                                viewHolder.progress_load.setVisibility(View.GONE);
                            } else {
                                viewHolder.iv_fail_resend.setVisibility(View.VISIBLE);
                                viewHolder.progress_load.setVisibility(View.GONE);
                                viewHolder.tv_send_status.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            });
            viewHolder.iv_picture.setOnLongClickListener(listener);
        } else if (holder instanceof ReceiveImageHolder) {
            final ReceiveImageHolder viewHolder = (ReceiveImageHolder)holder;
            fillAvatar(info, message.getFromId(), viewHolder.iv_avatar);
            viewHolder.tv_time.setText(time);
            if (shouldShowTime(position)) {
                viewHolder.tv_time.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv_time.setVisibility(View.GONE);
            }
            final BmobIMImageMessage msg = BmobIMImageMessage.buildFromDB(false, message);
            Glide.with(MyApplication.getInstance())
                    .load(msg.getRemoteUrl())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(viewHolder.iv_picture);
            viewHolder.iv_picture.setOnLongClickListener(listener);
        }
    }

    private void fillAvatar(BmobIMUserInfo info, String fromId, final ImageView view) {
        if (info != null && !TextUtils.isEmpty(info.getAvatar())) {
            Glide.with(MyApplication.getInstance())
                    .load(info.getAvatar())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(view);
        } else {
            BmobQuery<User> userQuery = new BmobQuery<>();
            userQuery.addWhereEqualTo("objectId", fromId);
            userQuery.findObjects(new FindListener<User>() {
                @Override
                public void done(List<User> list, BmobException e) {
                    if (e == null) {
                        if (list.size() == 1) {
                            User user = list.get(0);
                            if (!TextUtils.isEmpty(user.getAvatar())) {
                                Glide.with(MyApplication.getInstance())
                                        .load(user.getAvatar())
                                        .fitCenter()
                                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                        .into(view);
                            }
                        }
                    } else {
                        if (e.getErrorCode() == 9016) {
                            Toast toast = Toast.makeText(mContext, "网络被外星人劫持了，请稍后再试…",Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        } else {
                            Log.e(TAG, e.toString());
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        BmobIMMessage message = mMessages.get(position);
        if(message.getMsgType().equals(BmobIMMessageType.IMAGE.getType())){
            return message.getFromId().equals(mCurrentId) ? TYPE_SEND_IMAGE: TYPE_RECEIVER_IMAGE;
        } else if(message.getMsgType().equals(BmobIMMessageType.TEXT.getType())){
            return message.getFromId().equals(mCurrentId) ? TYPE_SEND_TXT: TYPE_RECEIVER_TXT;
        }else{
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    private boolean shouldShowTime(int position) {
        if (position == 0) {
            return true;
        }
        long lastTime = mMessages.get(position - 1).getCreateTime();
        long curTime = mMessages.get(position).getCreateTime();
        return curTime - lastTime > TIME_INTERVAL;
    }

    static class SendTextHolder extends RecyclerView.ViewHolder {
        public CircleImageView iv_avatar;
        public AppCompatTextView tv_message;
        public AppCompatTextView tv_time;
        public AppCompatImageView iv_fail_resend;
        public ProgressBar progress_load;
        public AppCompatTextView tv_send_status;

        public SendTextHolder(View itemView) {
            super(itemView);
            iv_avatar = (CircleImageView)itemView.findViewById(R.id.iv_avatar);
            tv_message = (AppCompatTextView)itemView.findViewById(R.id.tv_message);
            tv_time = (AppCompatTextView)itemView.findViewById(R.id.tv_time);
            iv_fail_resend = (AppCompatImageView)itemView.findViewById(R.id.iv_fail_resend);
            progress_load = (ProgressBar)itemView.findViewById(R.id.progress_load);
            tv_send_status = (AppCompatTextView)itemView.findViewById(R.id.tv_send_status);
        }
    }

    static class ReceiveTextHolder extends RecyclerView.ViewHolder {
        public CircleImageView iv_avatar;
        public AppCompatTextView tv_message;
        public AppCompatTextView tv_time;

        public ReceiveTextHolder(View itemView) {
            super(itemView);
            iv_avatar = (CircleImageView)itemView.findViewById(R.id.iv_avatar);
            tv_message = (AppCompatTextView)itemView.findViewById(R.id.tv_message);
            tv_time = (AppCompatTextView)itemView.findViewById(R.id.tv_time);
        }
    }

    static class SendImageHolder extends RecyclerView.ViewHolder {
        public CircleImageView iv_avatar;
        public AppCompatTextView tv_time;
        public AppCompatImageView iv_picture;
        public AppCompatImageView iv_fail_resend;
        public ProgressBar progress_load;
        public AppCompatTextView tv_send_status;

        public SendImageHolder(View itemView) {
            super(itemView);
            iv_avatar = (CircleImageView)itemView.findViewById(R.id.iv_avatar);
            tv_time = (AppCompatTextView)itemView.findViewById(R.id.tv_time);
            iv_picture = (AppCompatImageView)itemView.findViewById(R.id.iv_picture);
            iv_fail_resend = (AppCompatImageView)itemView.findViewById(R.id.iv_fail_resend);
            progress_load = (ProgressBar)itemView.findViewById(R.id.progress_load);
            tv_send_status = (AppCompatTextView)itemView.findViewById(R.id.tv_send_status);
        }
    }

    static class ReceiveImageHolder extends RecyclerView.ViewHolder {
        public CircleImageView iv_avatar;
        public AppCompatTextView tv_time;
        public AppCompatImageView iv_picture;

        public ReceiveImageHolder(View itemView) {
            super(itemView);
            iv_avatar = (CircleImageView)itemView.findViewById(R.id.iv_avatar);
            tv_time = (AppCompatTextView)itemView.findViewById(R.id.tv_time);
            iv_picture = (AppCompatImageView)itemView.findViewById(R.id.iv_picture);
        }
    }
}
