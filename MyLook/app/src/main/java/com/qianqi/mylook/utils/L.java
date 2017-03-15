package com.qianqi.mylook.utils;

import android.util.Log;

import java.io.IOException;

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

    public static void d(String s, Exception e) {
        if(DEBUG)
            Log.d(TAG,s,e);
    }

    public static void d(String s, Throwable e) {
        if(DEBUG)
            Log.d(TAG,s,e);
    }

    public static void i(String s) {
        if(DEBUG)
            Log.i(TAG,s);
    }

    public static void w(String message) {
        if(DEBUG)
            Log.w(TAG,message);
    }
}
