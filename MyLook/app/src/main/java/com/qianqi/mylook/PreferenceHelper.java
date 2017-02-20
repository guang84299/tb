package com.qianqi.mylook;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2017/1/20.
 */

public class PreferenceHelper {
    public static final String PREFS_POWER = "power";
    public static final String PREFS_COMMON = "common";
    public static final String PREFS_START = "start";

    private static PreferenceHelper instance;
    private Context appContext;
    private SharedPreferences powerPrefs;
    private SharedPreferences commonPrefs;
    private SharedPreferences startPrefs;

    public static PreferenceHelper getInstance(){
        if(instance == null){
            synchronized (PreferenceHelper.class){
                if(instance == null){
                    instance = new PreferenceHelper();
                }
            }
        }
        return instance;
    }

    public PreferenceHelper(){

    }

    public void initContext(Context context){
        this.appContext = context;
    }

    public SharedPreferences power(){
        if(powerPrefs == null){
            powerPrefs = appContext.getSharedPreferences(PREFS_POWER,0);
        }
        return powerPrefs;
    }

    public SharedPreferences common(){
        if(commonPrefs == null){
            commonPrefs = appContext.getSharedPreferences(PREFS_COMMON,0);
        }
        return commonPrefs;
    }

    public SharedPreferences start(){
        if(startPrefs == null){
            startPrefs = appContext.getSharedPreferences(PREFS_START,0);
        }
        return startPrefs;
    }
}
