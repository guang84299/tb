package com.android.system.manager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.system.manager.fw.FWManager;
import com.android.system.manager.utils.L;

/**
 * Created by Administrator on 2017/1/19.
 */

public class SService extends Service{

    private FWManager fw;

    @Override
    public void onCreate() {
        super.onCreate();
//        L.d("SService onCreate:"+android.os.Process.myPid());
        fw = new FWManager(this.getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fw.initProcessFirewall();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fw.onDestroy();
    }
}
