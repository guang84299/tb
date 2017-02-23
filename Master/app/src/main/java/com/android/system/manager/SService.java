package com.android.system.manager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.android.system.manager.f.M;

/**
 * Created by Administrator on 2017/1/19.
 */

public class SService extends Service{

    private M m;

    @Override
    public void onCreate() {
        super.onCreate();
//        L.d("SService onCreate:"+android.os.Process.myPid());
        m = new M(this.getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        m.i();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        m.d();
    }
}
