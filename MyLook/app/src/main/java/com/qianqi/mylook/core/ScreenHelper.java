package com.qianqi.mylook.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.qianqi.mylook.BusTag;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/2/10.
 */

public class ScreenHelper extends BroadcastReceiver {

    public ScreenHelper(){
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
            EventBus.getDefault().post(new BusTag(BusTag.TAG_SCREEN_ON));
        }
        else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            EventBus.getDefault().post(new BusTag(BusTag.TAG_SCREEN_OFF));
        }
        else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            EventBus.getDefault().post(new BusTag(BusTag.TAG_USER_PRESENT));
        }
    }
}
