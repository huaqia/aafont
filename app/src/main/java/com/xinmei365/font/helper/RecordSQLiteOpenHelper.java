package com.xinmei365.font.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RecordSQLiteOpenHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "search.db";
    private final static int DB_VERDION = 1;
    private final static String CREATE_RECORDS_TABLE_SHOP  = "CREATE TABLE IF NOT EXISTS recordShops(_id INTEGER PRIMARY KEY AUTOINCREMENT ,username TEXT , keyword TEXT , time NOT NULL DEFAULT (datetime('now' , 'localtime')));";
    private final static String CREATE_RECORDS_TABLE_USER  = "CREATE TABLE IF NOT EXISTS recordUsers (_id INTEGER PRIMARY KEY AUTOINCREMENT ,username TEXT , keyword TEXT , time NOT NULL DEFAULT (datetime('now' , 'localtime')));";

    public RecordSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERDION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_RECORDS_TABLE_SHOP);
        db.execSQL(CREATE_RECORDS_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recordShops");
        db.execSQL("DROP TABLE IF EXISTS recordUsers");
        onCreate(db);
    }
}
