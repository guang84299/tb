package com.qianqi.mylook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.qianqi.mylook.db.metadata.AutoStartMetaData;
import com.qianqi.mylook.db.metadata.SettingMetaData;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AutoStartMetaData.SQL_CREATE_TABLE_AUTOSTART);
        db.execSQL(SettingMetaData.SQL_CREATE_TABLE_SETTING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
