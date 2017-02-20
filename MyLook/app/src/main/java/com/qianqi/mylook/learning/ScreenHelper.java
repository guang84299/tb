package com.qianqi.mylook.learning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.qianqi.mylook.BusTag;

/**
 * Created by Administrator on 2017/2/10.
 */

public class ScreenHelper extends BroadcastReceiver {

    private LearningMonitor monitor;

    public ScreenHelper(LearningMonitor monitor){
        this.monitor = monitor;
    }

    public void registerReceiver(Context context){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(this,filter);
    }

    public void unregisterReceiver(Context context){
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            monitor.screenOn();
        }
        else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            monitor.screenOff();
        }
        else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            monitor.userPresent();
        }
    }
}
