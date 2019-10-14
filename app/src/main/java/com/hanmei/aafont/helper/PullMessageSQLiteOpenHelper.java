package com.hanmei.aafont.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PullMessageSQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "pushMessage.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_MESSAGE = "messages";
    public static final String TAG = "Database";

    private static final String MESSAGE_CREATE_SQL = "create table "+
            TABLE_MESSAGE
            +"("
            +"id integer primary key autoincrement,"
            +"msg text not null unique"
            +");";

    public PullMessageSQLiteOpenHelper(Context context) {
        super(context, DB_NAME,null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MESSAGE_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
