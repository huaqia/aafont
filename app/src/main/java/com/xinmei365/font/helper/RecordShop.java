package com.xinmei365.font.helper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordShop {
    private final String TABLE_NAME = "recordShops";
    private SQLiteDatabase mRecordsDb;
    private RecordSQLiteOpenHelper mRecordHelper;
    private String mUsername;
    private NotifyDataChanged mNotifyDataChanged;

    public RecordShop(Context context , String mUsername){
        this.mRecordHelper = new RecordSQLiteOpenHelper(context);
        this.mUsername = mUsername;
    }

    public interface NotifyDataChanged{
        void notifyDataChanged();
    }

    public void setNotifyDataChanged(NotifyDataChanged notifyDataChanged){
        this.mNotifyDataChanged = notifyDataChanged;
    }

    public void removeNotifyDataChanged(){
        if (mNotifyDataChanged != null){
            mNotifyDataChanged = null;
        }
    }

    private synchronized SQLiteDatabase getWritableDatabase(){
        return mRecordHelper.getWritableDatabase();
    }

    private synchronized SQLiteDatabase getReadableDatabase(){
        return mRecordHelper.getReadableDatabase();
    }

    public void closeDatabase(){
        if (mRecordsDb != null){
            mRecordsDb.close();
        }
    }

    /**
     * 添加搜索记录
     * @param record
     */
    public void addRecords(String record){
        int recordId = getRecordId(record);
        try {
            mRecordsDb = getReadableDatabase();
            if (-1 == recordId){
                ContentValues values = new ContentValues();
                values.put("username" , mUsername);
                values.put("keyword" , record);
                mRecordsDb.insert(TABLE_NAME , null , values);
            }else {
                Date date =  new Date();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                ContentValues contentValues = new ContentValues();
                contentValues.put("time" , sdf.format(date));
                mRecordsDb.update(TABLE_NAME , contentValues , "_id = ?" , new String[]{Integer.toString(recordId)});
            }
            if (mNotifyDataChanged != null){
                mNotifyDataChanged.notifyDataChanged();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isHasRecord(String record) {
        boolean isHasRecord = false;
        Cursor cursor = null;
        try {
            mRecordsDb = getReadableDatabase();
            cursor = mRecordsDb.query(TABLE_NAME, null, "username = ?", new String[]{mUsername}, null, null, null);
            while (cursor.moveToNext()) {
                if (record.equals(cursor.getString(cursor.getColumnIndexOrThrow("keyword")))) {
                    isHasRecord = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                //关闭游标
                cursor.close();
            }
        }
        return isHasRecord;
    }

    private int getRecordId(String record) {
        int recordId = -1;
        Cursor cursor =null;
        try {
            mRecordsDb = getReadableDatabase();
            cursor = mRecordsDb.query(TABLE_NAME , null , "username = ?" , new String[]{mUsername} , null , null ,null);
            while (cursor.moveToNext()){
                if (record.equals(cursor.getString(cursor.getColumnIndexOrThrow("keyword")))){
                    recordId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return recordId;
    }

    public List<String> getRecordsByNumber(int recordNumber) {
        List<String> recordsList = new ArrayList<>();
        if (recordNumber < 0) {
            throw new IllegalArgumentException();
        } else if (0 == recordNumber) {
            return recordsList;
        } else {
            Cursor cursor = null;
            try {
                mRecordsDb = getReadableDatabase();
                cursor = mRecordsDb.query(TABLE_NAME, null, "username = ?", new String[]{mUsername}, null, null, "time desc limit " + recordNumber);
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("keyword"));
                    recordsList.add(name);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    //关闭游标
                    cursor.close();
                }
            }
        }
        return recordsList;
    }


    /**
     * 模糊查询
     */
    public List<String> querySimlarRecord(String record){
        List<String> simlarRecords = new ArrayList<>();
        Cursor cursor = null;
        try {
            mRecordsDb = getReadableDatabase();
            cursor = mRecordsDb.query(TABLE_NAME , null ,"username = ? and keyword like '%?%'" , new String[]{mUsername , record}, null , null , "order by time desc");
            while (cursor.moveToNext()){
                String name = cursor.getString(cursor.getColumnIndexOrThrow("keyword"));
                simlarRecords.add(name);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return simlarRecords;
    }

    /**
     * 清除指定用户的搜索记录
     */
    public void deleteUsernameAllRecords() {
        try {
            mRecordsDb = getWritableDatabase();
            mRecordsDb.delete(TABLE_NAME, "username = ?", new String[]{mUsername});
            if (mNotifyDataChanged != null) {
                mNotifyDataChanged.notifyDataChanged();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TABLE_NAME, "清除所有历史记录失败");
        } finally {
        }
    }

    /**
     * 清空数据库所有的历史记录
     */
    public void deleteAllRecords() {
        try {
            mRecordsDb = getWritableDatabase();
            mRecordsDb.execSQL("delete from " + TABLE_NAME);
            if (mNotifyDataChanged != null) {
                mNotifyDataChanged.notifyDataChanged();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TABLE_NAME, "清除所有历史记录失败");
        } finally {
        }
    }

    /**
     * 通过id删除记录
     *
     * @param id 记录id
     * @return 返回删除id
     */
    public int deleteRecord(int id) {
        int d = -1;
        try {
            mRecordsDb = getWritableDatabase();
            d = mRecordsDb.delete(TABLE_NAME, "_id = ?", new String[]{Integer.toString(id)});
            if (mNotifyDataChanged != null) {
                mNotifyDataChanged.notifyDataChanged();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TABLE_NAME, "删除_id：" + id + "历史记录失败");
        }
        return d;
    }

    /**
     * 通过记录删除记录
     *
     * @param record 记录
     */
    public int deleteRecord(String record) {
        int recordId = -1;
        try {
            mRecordsDb = getWritableDatabase();
            recordId = mRecordsDb.delete(TABLE_NAME, "username = ? and keyword = ?", new String[]{mUsername, record});
            if (mNotifyDataChanged != null) {
                mNotifyDataChanged.notifyDataChanged();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TABLE_NAME, "清除所有历史记录失败");
        }
        return recordId;
    }
}
