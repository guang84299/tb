package com.qianqi.mylook.client;

import android.content.ComponentName;

import com.android.system.manager.server.MS;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.core.WindowService;

/**
 * Created by Administrator on 2017/1/17.
 */

public class SettingHelper {

    private MS masterServer;

    public void setMasterServer(MS server){
        this.masterServer = server;
        if(this.masterServer != null){
            checkAccessibility();
        }
    }

    private void checkAccessibility() {
        ComponentName com = new ComponentName(MainApplication.getInstance(), WindowService.class.getName());
        masterServer.g(com.flattenToString());
    }

}
