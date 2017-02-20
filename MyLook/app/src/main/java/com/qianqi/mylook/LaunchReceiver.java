package com.qianqi.mylook;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.qianqi.mylook.utils.L;

/**
 * Created by Administrator on 2016/12/26.
 */

public class LaunchReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        L.d("LaunchReceiver:onReceive");
    }


}
