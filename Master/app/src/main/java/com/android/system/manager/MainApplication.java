package com.android.system.manager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.android.support.servicemanager.ServiceManager;
import com.android.system.manager.server.MasterConstant;
import com.android.system.manager.server.MasterServerImpl;
import com.android.system.manager.server.SettingHelper;
import com.android.system.manager.utils.CommonUtils;

/**
 * Created by Administrator on 2017/1/3.
 */

public class MainApplication extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = CommonUtils.getCurProcessName(this);
        if(processName != null && processName.equals("system")){
            return;
        }
        appContext = getApplicationContext();
        ServiceManager.init(this);
        startSService();
        MasterServerImpl.getInstance().a(getApplicationContext());
        wakeClient();
        ServiceManager.publishService("master", MasterServerImpl.class.getName());
    }

    public static Context getAppContext(){
        return appContext;
    }

    public static void startSService(){
//        L.d("Master onCreate:"+android.os.Process.myPid());
        Intent intent = new Intent();
        intent.setClass(appContext, SService.class);
        appContext.startService(intent);
    }

    private void wakeClient(){
        Intent intent = new Intent();
        intent.setClassName(MasterConstant.CORE_SERVICE[0],MasterConstant.CORE_SERVICE[1]);
        this.startService(intent);
        Intent broadcast = new Intent();
        broadcast.setAction(MasterConstant.CORE_ACTION);
        broadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        this.sendBroadcast(broadcast);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MasterServerImpl.getInstance().b();
        SettingHelper.onDestroy();
    }
}
