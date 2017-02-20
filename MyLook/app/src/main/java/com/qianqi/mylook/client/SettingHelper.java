package com.qianqi.mylook.client;

import android.content.ComponentName;
import android.os.Binder;
import android.provider.Settings;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.accessibility.WindowService;
import com.qianqi.mylook.utils.L;

/**
 * Created by Administrator on 2017/1/17.
 */

public class SettingHelper {

    private IMasterServer masterServer;

    public void setMasterServer(IMasterServer server){
        this.masterServer = server;
        if(this.masterServer != null){
            checkAccessibility();
        }
    }

    private void checkAccessibility() {
        ComponentName com = new ComponentName(MainApplication.getInstance(), WindowService.class.getName());
        masterServer.enableAccessibilityService(com.flattenToString());
    }

}
