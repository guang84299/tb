package com.android.system.manager.plugin.utils;

import android.util.Log;

/**
 * Created by Administrator on 2016/12/26.
 */

public class L {

    public static final String TAG = "MyLookLog";
    public static boolean DEBUG = false;

    public static void d(String s){
        if(DEBUG)
            Log.d(TAG,s);
    }

    public static void v(String s) {
        if(DEBUG)
            Log.v(TAG,s);
    }

    public static void d(String s, Throwable e) {
        if(DEBUG)
            Log.d(TAG,s,e);
    }

}
