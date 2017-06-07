package com.qianqi.mylook.stat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;

import com.android.system.core.sometools.GAdController;
import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.PreferenceHelper;
import com.qianqi.mylook.thread.ThreadTask;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/1/3.
 */

public class StatMonitor extends ThreadTask {

    public static final String KEY_CHECK_TIME = "stat_check_time";

    private static final int MSG_CHECK = 0;

    private final long CHECK_INTERVAL_NORMAL = 6*60*60*1000;
//    private final long CHECK_INTERVAL_NORMAL = 15*1000;
    private BroadcastReceiver receiver;
    private boolean screenOn = true;

    public StatMonitor() {
        super(StatMonitor.class.getSimpleName());
        EventBus.getDefault().register(this);
        IntentFilter syncFilter = new IntentFilter();
        syncFilter.addAction(Intent.ACTION_DATE_CHANGED);
        syncFilter.addAction(Intent.ACTION_TIME_CHANGED);
        syncFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(handler == null)
                    return;
                handler.removeMessages(MSG_CHECK);
                handler.sendEmptyMessageDelayed(MSG_CHECK,3000);
            }
        };
        MainApplication.getInstance().registerReceiver(receiver,syncFilter);
    }

    public void onDestroy(){
        EventBus.getDefault().unregister(this);
        this.cancel();
        if(receiver != null) {
            MainApplication.getInstance().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler.sendEmptyMessage(MSG_CHECK);
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what){
            case MSG_CHECK:
                L.d("check stat activity");
                long wait = startActivity();
                if(wait > 0){
                    handler.removeMessages(MSG_CHECK);
                    handler.sendEmptyMessageDelayed(MSG_CHECK,wait);
                }
                break;
        }
    }

    private long startActivity(){
        if(screenOn)
            return -1;
        long checkTime = PreferenceHelper.getInstance().common().getLong(KEY_CHECK_TIME,0);
        long time = System.currentTimeMillis();
        if(time - checkTime < CHECK_INTERVAL_NORMAL){
            return (CHECK_INTERVAL_NORMAL + checkTime - time);
        }
        L.d("start stat activity");
        Intent intent = new Intent();
        intent.setClass(MainApplication.getInstance(),StatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainApplication.getInstance().startActivity(intent);
        PreferenceHelper.getInstance().common().edit().putLong(KEY_CHECK_TIME,time).commit();
        return CHECK_INTERVAL_NORMAL;
    }

//    @Subscribe(
//            threadMode = ThreadMode.POSTING
//    )
//    public void screenOn(BusTag event) {
//        if(event.tag.equals(BusTag.TAG_SCREEN_ON)) {
//            if(handler == null)
//                return;
//            L.d("remove check");
//            screenOn = true;
//            handler.removeMessages(MSG_CHECK);
//            handler.sendEmptyMessageDelayed(MSG_CHECK,10000);
//        }
//    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void screenOff(BusTag event) {
        if(event.tag.equals(BusTag.TAG_SCREEN_OFF)) {
            if(handler == null)
                return;
            screenOn = false;
            handler.removeMessages(MSG_CHECK);
            handler.sendEmptyMessageDelayed(MSG_CHECK,10000);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void userPresent(BusTag event) {
        if(event.tag.equals(BusTag.TAG_USER_PRESENT)) {
            if(handler == null)
                return;
            L.d("remove check");
            screenOn = true;
            handler.removeMessages(MSG_CHECK);
//            handler.sendEmptyMessageDelayed(MSG_CHECK,10000);
        }
    }
}
