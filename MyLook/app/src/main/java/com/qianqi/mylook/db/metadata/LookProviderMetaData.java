package com.qianqi.mylook.db.metadata;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Administrator on 2017/1/12.
 */

public class LookProviderMetaData {
    //URI的指定，此处的字符串必须和声明的authorities一致
    public static final String AUTHORITIES = "com.qianqi.mylook.provider";
    //数据库名称
    public static final String DATABASE_NAME = "mylook.db";
    //数据库的版本
    public static final int DATABASE_VERSION = 1;


}
