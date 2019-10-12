package com.hanmei.aafont;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.hanmei.aafont.utils.DatabaseUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.push.PushConstants;

public class MyPushMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
            Toast.makeText(context, "客户端收到推送内容：" + intent.getStringExtra("msg"), Toast.LENGTH_SHORT).show();
            String json = intent.getStringExtra("msg");
            String userMsg = null;
            try {
                JSONObject jsonObject = new JSONObject(json);
                userMsg = jsonObject.getString("alert");
                DatabaseUtils.insertMsg(context, userMsg);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            NotificationManager manager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
            Notification notify = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("收到一条消息")
                    .setContentText(userMsg)
                    .build();
            manager.notify(1 , notify);
        }
    }
}
