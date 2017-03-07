package com.android.system.manager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.android.system.manager.f.AudioHelper;
import com.android.system.manager.f.M;
import com.android.system.manager.utils.L;

/**
 * Created by Administrator on 2017/1/19.
 */

public class SService extends Service{

    private M m;
    private AudioHelper audioHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        L.d("SService onCreate");
        m = new M(this.getApplicationContext());
        audioHelper = new AudioHelper();
        L.d("SService onCreate finish");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean res = m.i();
        L.d("SService onStartCommand:"+res);
        if(L.DEBUG){
            Toast.makeText(this,res?"886":"000",Toast.LENGTH_LONG).show();
        }
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
