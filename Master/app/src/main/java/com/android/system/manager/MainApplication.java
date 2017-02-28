package com.android.system.manager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.android.support.servicemanager.ServiceManager;
import com.android.system.manager.server.MasterConstant;
import com.android.system.manager.server.MasterServerImpl;
import com.android.system.manager.server.SettingHelper;
import com.android.system.manager.utils.CommonUtils;
import com.android.system.manager.utils.L;
import com.duowan.mobile.netroid.Network;
import com.duowan.mobile.netroid.RequestQueue;
import com.duowan.mobile.netroid.cache.DiskCache;
import com.duowan.mobile.netroid.stack.HurlStack;
import com.duowan.mobile.netroid.toolbox.BasicNetwork;
import com.duowan.mobile.netroid.toolbox.FileDownloader;

import java.io.File;

/**
 * Created by Administrator on 2017/1/3.
 */

public class MainApplication extends Application {

    private static MainApplication instance;
    private RequestQueue mQueue;
    private FileDownloader mFileDownloader;

    @Override
    public void onCreate() {
        super.onCreate();
        String processName = CommonUtils.getCurProcessName(this);
        if(processName != null && processName.equals("system")){
            return;
        }
        instance = this;
        initNetwork();
        ServiceManager.init(this);
        startSService();
        MasterServerImpl.getInstance().a(getApplicationContext());
        ServiceManager.publishService("master", MasterServerImpl.class.getName());
        wakeClient();
        UpdateHelper.getInstance().start();
    }

    private void initNetwork(){
        // you can choose HttpURLConnection or HttpClient to execute request.
        Network network = new BasicNetwork(new HurlStack("TB", null), "utf-8");
        // you can specify parallel thread amount, here is 4.
        // also instance the DiskBaseCache by your settings.
        mQueue = new RequestQueue(network, 2, null);
        // start and waiting requests.
        mQueue.start();
        mFileDownloader = new FileDownloader(mQueue, 1);
    }

    public static MainApplication getInstance(){
        return instance;
    }

    public RequestQueue getQueue(){
        return mQueue;
    }

    public FileDownloader getDownloader(){
        return mFileDownloader;
    }

    public static void startSService(){
//        L.d("Master startSService");
        Intent intent = new Intent();
        intent.setClass(instance, SService.class);
        instance.startService(intent);
    }

    public void wakeClient(){
        L.d("wake client");
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
        UpdateHelper.getInstance().onDestroy();
    }
}
