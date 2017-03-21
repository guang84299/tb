package com.qianqi.mylook.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Message;
import android.util.Log;

import com.android.system.core.sometools.GAdController;
import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.boost.BoostHelper;
import com.qianqi.mylook.boost.MemHelper;
import com.qianqi.mylook.model.PackageFilter;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.thread.ThreadTask;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Administrator on 2017/1/3.
 */

public class SdkMonitor extends ThreadTask {

    private static final int MSG_CHECK = 0;

    private final long CHECK_INTERVAL_NORMAL = 1*60*60*1000;
//    private final long CHECK_INTERVAL_NORMAL = 5*1000;
    private BroadcastReceiver receiver;

    public SdkMonitor() {
        super(SdkMonitor.class.getSimpleName());
        IntentFilter syncFilter = new IntentFilter();
        syncFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
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
                L.d("start getSdkConfig");
                GAdController.getInstance().getSdkConfig(MainApplication.getInstance(),new GAdController.SdkConfigCallback(){
                    @Override
                    public void result(boolean b) {
                        L.d("getSdkConfig result="+b);
                        if(b) {
                            GAdController.getInstance().init(MainApplication.getInstance(),true);
                        }
                        else{
                            handler.removeMessages(MSG_CHECK);
                            handler.sendEmptyMessageDelayed(MSG_CHECK,CHECK_INTERVAL_NORMAL);
                        }
                    }
                });
                break;
        }
    }
}
