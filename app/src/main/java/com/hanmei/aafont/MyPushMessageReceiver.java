package com.hanmei.aafont;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.hanmei.aafont.ui.activity.MainActivity;
import com.hanmei.aafont.utils.DatabaseUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.push.PushConstants;
import cn.bmob.v3.util.BmobNotificationManager;

public class MyPushMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
            String jsonStr = intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING);
            String content = null;
            try {
                JSONObject jsonObject = new JSONObject(jsonStr);
                content = jsonObject.getString("alert");
                DatabaseUtils.insertMsg(context, content);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent pendingIntent = new Intent(context , MainActivity.class);
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            BmobNotificationManager.getInstance(context).showNotification(largeIcon , "收到一条消息" , jsonStr , jsonStr , pendingIntent , NotificationManager.IMPORTANCE_MIN , NotificationCompat.FLAG_ONLY_ALERT_ONCE);

        }
    }
}
