package com.hanmei.aafont.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hanmei.aafont.helper.DatabaseHelper;

import java.util.ArrayList;

public class DatabaseUtils {
    private static final String QUERY_ALL_MESSAGE = "select * from messages;";
    private static final String INSERT_MESSAGE = "insert or ignore into messages(msg) values(?);";

    public static ArrayList<String> queryAllMsg(Context context) {
        ArrayList<String> messages = new ArrayList<>();
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.rawQuery(QUERY_ALL_MESSAGE, null);
        while (cursor.moveToNext()) {
            String msg = cursor.getString(cursor.getColumnIndex("msg"));
            messages.add(msg);
        }
        return messages;
    }

    public static void deleteMessage(Context context, int id) {
        String mid = String.valueOf(id);
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        sqLiteDatabase.delete("messages", "id=?", new String[]{mid});
        sqLiteDatabase.close();
    }

    public static boolean isHasMsg(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(QUERY_ALL_MESSAGE, null);
        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public static void insertMsg(Context context, String msg) {
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL(INSERT_MESSAGE,new String[]{msg});
    }
}
