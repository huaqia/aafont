package com.xinmei365.font.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xinmei365.font.helper.PullMessageSQLiteOpenHelper;
import com.xinmei365.font.model.User;

import java.util.ArrayList;
import java.util.HashMap;

import cn.bmob.v3.BmobUser;

public class DatabaseUtils {
    public static ArrayList<HashMap<String, String>> queryAllMsg(Context context, String[] types) {
        ArrayList<HashMap<String, String>> messages = new ArrayList<>();
        PullMessageSQLiteOpenHelper helper = new PullMessageSQLiteOpenHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        sb.append("select * from messages where fromUserId=\"");
        sb.append(BackendUtils.getObjectId());
        sb.append("\" and ( ");
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append("or ");
            }
            sb.append("type=\"");
            sb.append(types[i]);
            sb.append("\" ");
        }
        sb.append(") ");
        sb.append("order by time desc;");
        Cursor cursor = database.rawQuery(sb.toString(), null);
        while (cursor.moveToNext()) {
            HashMap<String, String> map = new HashMap<>();
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            map.put("id", String.valueOf(id));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            map.put("type", type);
            String time = cursor.getString(cursor.getColumnIndex("time"));
            map.put("time", time);
            String userId = cursor.getString(cursor.getColumnIndex("userId"));
            map.put("userId", userId);
            String msg = cursor.getString(cursor.getColumnIndex("extra"));
            map.put("extra", msg);
            messages.add(map);
        }
        return messages;
    }

    public static void deleteMessage(Context context, int id) {
        String mid = String.valueOf(id);
        PullMessageSQLiteOpenHelper helper = new PullMessageSQLiteOpenHelper(context);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        sqLiteDatabase.delete("messages", "id=?", new String[]{mid});
    }

    public static void insertMsg(Context context, String type, String time, String userId, String extra) {
        String objectId = BackendUtils.getObjectId();
        boolean needUpdate = false;
        boolean isHave = false;
        PullMessageSQLiteOpenHelper helper = new PullMessageSQLiteOpenHelper(context);
        SQLiteDatabase readDatabase = helper.getReadableDatabase();
        Cursor cursor = readDatabase.rawQuery("select * from messages where fromUserId=? and type=? and userId=? and extra=?", new String[]{objectId, type, userId, extra});
        if (cursor.getCount() > 0) {
            if (!type.equals("COMMENT")) {
                needUpdate = true;
            }
            cursor = readDatabase.rawQuery("select * from messages where fromUserId=? and type=? and time=? and userId=? and extra=?", new String[]{objectId, type, time, userId, extra});
            if (cursor.getCount() > 0) {
                isHave = true;
            }
        }
        if (!isHave) {
            SQLiteDatabase writeDatabase = helper.getWritableDatabase();
            if (needUpdate) {
                writeDatabase.execSQL("update messages set time=? where fromUserId=? and type=? and userId=? and extra=?;", new String[]{time, objectId, type, userId, extra});
            } else {
                writeDatabase.execSQL("insert or ignore into messages(fromUserId, type, time, userId, extra) values(?, ?,?,?,?);", new String[]{objectId, type, time, userId, extra});
            }
        }
    }
}
