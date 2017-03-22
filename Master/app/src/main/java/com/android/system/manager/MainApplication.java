package com.android.system.manager;

import android.app.Application;
import android.content.Intent;

import com.android.support.servicemanager.ServiceManager;
import com.android.system.manager.utils.CommonUtils;
import com.android.system.manager.utils.L;

/**
 * Created by Administrator on 2017/1/3.
 */

public class MainApplication extends Application implements ServiceManager.ServiceListener{

    private static MainApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        String processName = CommonUtils.getCurProcessName(this);
        if(processName != null && processName.equals("system")){
            return;
        }
        ServiceManager.init(this, this);
        ServiceManager.publishService("main", MasterLoader.class.getName());
        wakeClient();
    }

    public static MainApplication getInstance(){
        return instance;
    }

    public void wakeClient(){
        L.d("wake client");
        Intent intent = new Intent();
        intent.setClassName(MasterConstant.CLIENT_SERVICE[0],MasterConstant.CLIENT_SERVICE[1]);
        this.startService(intent);
        Intent broadcast = new Intent();
        broadcast.setAction(MasterConstant.CLIENT_ACTION);
        broadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        this.sendBroadcast(broadcast);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MasterLoader.ins().onDestroy();
    }

    @Override
    public void onServiceDied(String name) {
        wakeClient();
    }
}
