package com.xinmei365.font;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xinmei365.font.model.User;
import com.xinmei365.font.ui.activity.MainActivity;
import com.xinmei365.font.utils.DatabaseUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMMessageType;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.BmobIMMessageHandler;
import cn.bmob.newim.notification.BmobNotificationManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class DemoMessageHandler extends BmobIMMessageHandler {
    private Context mContext;

    public DemoMessageHandler(Context context) {
        mContext = context;
    }

    @Override
    public void onMessageReceive(final MessageEvent event) {
        updateUserInfo(event);
    }

    @Override
    public void onOfflineReceive(final OfflineMessageEvent event) {
        Map<String, List<MessageEvent>> map = event.getEventMap();
        for (Map.Entry<String, List<MessageEvent>> entry : map.entrySet()) {
            List<MessageEvent> list = entry.getValue();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                //处理每条消息
                updateUserInfo(list.get(i));
            }
        }
    }

    private void updateUserInfo(final MessageEvent event) {
        final BmobIMConversation conversation = event.getConversation();
        final BmobIMUserInfo info = event.getFromUserInfo();
        final BmobIMMessage msg = event.getMessage();
        String username = info.getName();
        String avatar = info.getAvatar();
        String title = conversation.getConversationTitle();
        String icon = conversation.getConversationIcon();
        if (!username.equals(title) || (avatar != null && !avatar.equals(icon))) {
            BmobQuery<User> query = new BmobQuery<>();
            query.addWhereEqualTo("objectId", info.getUserId());
            query.findObjects(new FindListener<User>() {
                @Override
                public void done(List<User> list, BmobException e) {
                    if (e == null) {
                        if (list.size() == 1) {
                            User user = list.get(0);
                            String name = user.getNickName();
                            String avatar = user.getAvatar();
                            conversation.setConversationIcon(avatar);
                            conversation.setConversationTitle(name);
                            info.setName(name);
                            info.setAvatar(avatar);
                            BmobIM.getInstance().updateUserInfo(info);
                            if (!msg.isTransient()) {
                                BmobIM.getInstance().updateConversation(conversation);
                            }
                            if (BmobIMMessageType.getMessageTypeValue(msg.getMsgType()) == 0) {
                                ArrayList<String> channels = user.getChannels();
                                if (channels == null || !channels.contains(msg.getContent())) {
                                    processCustomMessage(msg, event.getFromUserInfo());
                                }
                            } else {
                                processSDKMessage(msg, event);
                            }
                        }
                    }
                }
            });
        }
    }

    private void processCustomMessage(BmobIMMessage msg, BmobIMUserInfo info) {
        String extra = msg.getExtra();
        if (extra != null) {
            String type = msg.getContent();
            DatabaseUtils.insertMsg(mContext, type, String.valueOf(msg.getCreateTime()), info.getUserId(), extra);
            if (type != null) {
                Intent pendingIntent = new Intent(mContext, MainActivity.class);
                pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
                if (type.equals("COMMENT")) {
                    HashMap<String, String> map = new Gson().fromJson(extra, new TypeToken<HashMap<String, String>>() {}.getType());
                    if (map != null) {
                        String msgContent = map.get("content");
                        cn.bmob.v3.util.BmobNotificationManager.getInstance(mContext).showNotification(largeIcon, "收到一条评论", msgContent, msgContent, pendingIntent, NotificationManager.IMPORTANCE_MIN, NotificationCompat.FLAG_ONLY_ALERT_ONCE);
                    }
                } else if (type.equals("LIKE")) {
                    cn.bmob.v3.util.BmobNotificationManager.getInstance(mContext).showNotification(largeIcon, "收到一条点赞", "", "", pendingIntent, NotificationManager.IMPORTANCE_MIN, NotificationCompat.FLAG_ONLY_ALERT_ONCE);
                } else if (type.equals("FAVORITE")) {
                    cn.bmob.v3.util.BmobNotificationManager.getInstance(mContext).showNotification(largeIcon, "收到一条收藏", "", "", pendingIntent, NotificationManager.IMPORTANCE_MIN, NotificationCompat.FLAG_ONLY_ALERT_ONCE);
                } else if (type.equals("FOLLOW")) {
                    cn.bmob.v3.util.BmobNotificationManager.getInstance(mContext).showNotification(largeIcon, "收到一条关注", "", "", pendingIntent, NotificationManager.IMPORTANCE_MIN, NotificationCompat.FLAG_ONLY_ALERT_ONCE);
                }
            }
        }
    }

    private void processSDKMessage(BmobIMMessage msg, MessageEvent event) {
        if (BmobNotificationManager.getInstance(mContext).isShowNotification()) {
            //如果需要显示通知栏，SDK提供以下两种显示方式：
            Intent pendingIntent = new Intent(mContext, MainActivity.class);
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            //BmobNotificationManager.getInstance(context).showNotification(event, pendingIntent);

            BmobIMUserInfo info = event.getFromUserInfo();
            //这里可以是应用图标，也可以将聊天头像转成bitmap
            Bitmap largeIcon = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
            BmobNotificationManager.getInstance(mContext).showNotification(largeIcon,
                    info.getName(), msg.getContent(), "您有一条新消息", pendingIntent);
        } else {
            EventBus.getDefault().post(event);
        }
    }
}
