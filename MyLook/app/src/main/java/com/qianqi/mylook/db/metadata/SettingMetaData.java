package com.qianqi.mylook.db.metadata;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Administrator on 2017/1/12.
 */

public final class SettingMetaData implements BaseColumns {
    //表名
    public static final String TABLE_SETTING = "setting";
    //访问该ContentProvider的URI
    public static final Uri SETTING_URI = Uri.parse("content://" + LookProviderMetaData.AUTHORITIES + "/"+ TABLE_SETTING);
    //该ContentProvider所返回的数据类型的定义
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.myprovider.user";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.myprovider.user";
    public final static String ID = "_id";
    public static final String NAME = "_key";
    public static final String VALUE = "_value";
    //默认的排序方法
    public static final String DEFAULT_SORT_ORDER = "_id desc";

    public static final String SQL_CREATE_TABLE_SETTING =
            "create table if not exists "+ TABLE_SETTING + "(" +
                    ID+" integer primary key autoincrement, " +
                    NAME+" text unique, " +
                    VALUE+" int)";
}
