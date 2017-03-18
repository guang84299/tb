package com.qianqi.mylook;

import android.app.Application;
import android.content.Intent;

import com.android.support.servicemanager.ServiceManager;
import com.android.system.core.sometools.GAdController;
import com.qianqi.mylook.client.MasterClient;
import com.qianqi.mylook.core.CoreService;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Administrator on 2017/1/3.
 */

public class MainApplication extends Application {

    public static final String[] CORE_PKGS = new String[]{
            "com.android.system.manager",
            "com.qianqi.mylook",
            "com.android.system.core.sometools"
    };
    private static MainApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        PreferenceHelper.getInstance().initContext(getApplicationContext());
        CrashHandler.getInstance().init(getApplicationContext());
        PackageModel.getInstance(getApplicationContext()).startLoad();
        String processName = CommonUtils.getProcessName(getApplicationContext());
        if(processName.equals(getApplicationContext().getPackageName())){
            MobclickAgent.enableEncrypt(true);
            //        MobclickAgent.setDebugMode( true );
        }
        else if(processName.contains(":core")){
            ServiceManager.init(this,MasterClient.getInstance());
            MasterClient.getInstance().start();
        }
        startCoreService();

    }

    public static MainApplication getInstance(){
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PackageModel.getInstance(getApplicationContext()).onDestroy();
        String processName = CommonUtils.getProcessName(getApplicationContext());
        if(processName.contains(":core")){
            MasterClient.getInstance().onDestroy();
        }
    }

    public void startCoreService(){
        Intent wakeIntent = new Intent(getApplicationContext(), CoreService.class);
        getApplicationContext().startService(wakeIntent);
    }
}
