package com.qianqi.mylook.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.qianqi.mylook.db.metadata.AutoStartMetaData;
import com.qianqi.mylook.db.metadata.LookProviderMetaData;
import com.qianqi.mylook.db.metadata.SettingMetaData;

import java.util.HashMap;

import static com.qianqi.mylook.db.metadata.LookProviderMetaData.DATABASE_VERSION;

/**
 * Created by Administrator on 2017/1/12.
 */

public class LookProvider extends ContentProvider {
    private final static int AUTOSTART = 1;
    private final static int AUTOSTARTS = 2;
    private final static int SETTINGS = 3;
    //操作URI的类
    public static final UriMatcher uriMatcher;
    //为UriMatcher添加自定义的URI
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(LookProviderMetaData.AUTHORITIES,"/autostart", AUTOSTARTS);
        uriMatcher.addURI(LookProviderMetaData.AUTHORITIES,"/autostart/#", AUTOSTART);
        uriMatcher.addURI(LookProviderMetaData.AUTHORITIES,"/setting", SETTINGS);
    }
    private DatabaseHelper dh;
    //为数据库表字段起别名
    public static HashMap userProjectionMap;
    static {
        userProjectionMap = new HashMap();
        userProjectionMap.put(AutoStartMetaData._ID,AutoStartMetaData._ID);
        userProjectionMap.put(AutoStartMetaData.PACKAGE_NAME, AutoStartMetaData.PACKAGE_NAME);
        userProjectionMap.put(AutoStartMetaData.ALLOW, AutoStartMetaData.ALLOW);
    }

    /**
     * 创建ContentProvider时调用的回调函数
     * */
    @Override
    public boolean onCreate() {
        dh  = new DatabaseHelper(getContext(), LookProviderMetaData.DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }

    /**
     * 删除表数据
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dh.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case AUTOSTART:
                long id = ContentUris.parseId(uri);
                String where = AutoStartMetaData.ID + "=" + id;
                if (selection != null && !"".equals(selection)) {
                    selection = where + "and" + selection;
                }
                count = database.delete(AutoStartMetaData.TABLE_AUTOSTART, selection, selectionArgs);
                break;
            case AUTOSTARTS:
                count = database.delete(AutoStartMetaData.TABLE_AUTOSTART, selection, selectionArgs);
                break;
            case SETTINGS:
                count = database.delete(SettingMetaData.TABLE_SETTING, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("UnKnown uri: " + uri);
        }
        if(count > 0){
            //通知监听器，数据已经改变
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    /**
    * 插入数据
    */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //得到一个可写的数据库
        SQLiteDatabase db = dh.getWritableDatabase();
        long rowId = -1;
        switch (uriMatcher.match(uri)) {
            case AUTOSTARTS:
                //向指定的表插入数据，得到返回的Id
                rowId = db.replace(AutoStartMetaData.TABLE_AUTOSTART, null, values);
                if(rowId > -1){ // 判断插入是否执行成功
                    //如果添加成功，利用新添加的Id和
                    Uri insertedUserUri = ContentUris.withAppendedId(AutoStartMetaData.AUTOSTART_URI, rowId);
                    //通知监听器，数据已经改变
                    getContext().getContentResolver().notifyChange(insertedUserUri, null);
                    return insertedUserUri;
                }
                break;
            case SETTINGS:
                //向指定的表插入数据，得到返回的Id
                rowId = db.replace(SettingMetaData.TABLE_SETTING, null, values);
                if(rowId > -1){ // 判断插入是否执行成功
                    //如果添加成功，利用新添加的Id和
                    Uri insertedUserUri = ContentUris.withAppendedId(SettingMetaData.SETTING_URI, rowId);
                    //通知监听器，数据已经改变
                    getContext().getContentResolver().notifyChange(insertedUserUri, null);
                    return insertedUserUri;
                }
                break;
        }
        return uri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = dh.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case AUTOSTART:
                long id = ContentUris.parseId(uri);
                String where = AutoStartMetaData.ID + "=" + id;
                if (selection != null && !"".equals(selection)) {
                    selection = where + "and" + selection;
                }
                return database.query(AutoStartMetaData.TABLE_AUTOSTART, projection, selection, selectionArgs, null, null, sortOrder);
            case AUTOSTARTS:
                return database.query(AutoStartMetaData.TABLE_AUTOSTART, projection, selection, selectionArgs, null, null, sortOrder);
            case SETTINGS:
                return database.query(SettingMetaData.TABLE_SETTING, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException("UnKnown uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dh.getWritableDatabase();
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case AUTOSTARTS:
                //执行更新语句，得到更新的条数
                count = db.update(AutoStartMetaData.TABLE_AUTOSTART, values, selection, selectionArgs);
                if(count > 0){
                    //通知监听器，数据已经改变
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            case SETTINGS:
                //执行更新语句，得到更新的条数
                count = db.update(SettingMetaData.TABLE_SETTING, values, selection, selectionArgs);
                if(count > 0){
                    //通知监听器，数据已经改变
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case AUTOSTART:
                return "vnd.android.cursor.item";
            case AUTOSTARTS:
                return "vnd.android.cursor.dir";
            case SETTINGS:
                return "vnd.android.cursor.dir";
            default:
                throw new IllegalArgumentException("UnKnown uri: " + uri);
        }
    }
}
