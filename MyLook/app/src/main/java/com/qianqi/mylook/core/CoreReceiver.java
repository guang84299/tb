package com.qianqi.mylook.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.provider.Settings;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/12/26.
 */

public class CoreReceiver extends BroadcastReceiver {
    public static int defMode = PackageModel.DEFAULT_POWER_MODE;
    public static boolean isCts = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        L.d("CoreReceiver:onReceive");
        if(intent.getAction().equals("android.hardware.usb.action.USB_STATE"))
        {
            boolean connected = intent.getExtras().getBoolean("connected");
            if(connected)
            {
                boolean enableAdb = (Settings.Secure.getInt(MainApplication.getInstance().getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
                if(enableAdb && !isCts)
                {
                    isCts = true;
                    defMode = PackageModel.getInstance(MainApplication.getInstance()).getPowerMode();
                    if(defMode != PackageModel.POWER_MODE_PERFORMANCE)
                        PackageModel.getInstance(MainApplication.getInstance()).setPowerMode(PackageModel.POWER_MODE_PERFORMANCE);
//                    Toast.makeText(MainApplication.getInstance(),"cts 模式打开 关闭功能",Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(new BusTag("com.cts.open"));
                }
            }
            else
            {
                isCts = false;
                if(PackageModel.getInstance(MainApplication.getInstance()).getPowerMode() != defMode)
                    PackageModel.getInstance(MainApplication.getInstance()).setPowerMode(defMode);
//                Toast.makeText(MainApplication.getInstance(),"cts 模式关闭 恢复功能",Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new BusTag("com.cts.close"));
            }
        }
        else
        {
            MainApplication.getInstance().startCoreService();
        }

        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            EventBus.getDefault().post(new BusTag(BusTag.TAG_NETWORK_CHANGED));
        }
    }
}
