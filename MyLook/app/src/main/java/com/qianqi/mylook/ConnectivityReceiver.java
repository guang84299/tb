package com.qianqi.mylook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/12/26.
 */

public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        L.d("ConnectivityReceiver:onReceive");
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            EventBus.getDefault().post(new BusTag(BusTag.TAG_NETWORK_CHANGED));
        }
    }
}
