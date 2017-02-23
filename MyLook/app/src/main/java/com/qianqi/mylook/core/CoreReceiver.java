package com.qianqi.mylook.core;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/12/26.
 */

public class CoreReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        L.d("CoreReceiver:onReceive");
        MainApplication.getInstance().startCoreService();
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            EventBus.getDefault().post(new BusTag(BusTag.TAG_NETWORK_CHANGED));
        }
    }


}
