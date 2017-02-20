package com.android.system.manager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.android.system.manager.server.MasterConstant;
import com.android.system.manager.server.MasterServer;
import com.android.system.manager.server.SettingHelper;

/**
 * Created by Administrator on 2017/1/3.
 */

public class MainApplication extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        MasterServer.getInstance().start(getApplicationContext());
        this.wakeClient();
    }

    public static Context getAppContext(){
        return appContext;
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
        MasterServer.getInstance().destroy();
        SettingHelper.onDestroy();
    }
}
