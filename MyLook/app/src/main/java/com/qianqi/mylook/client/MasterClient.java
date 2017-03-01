package com.qianqi.mylook.client;

import android.content.Intent;
import android.text.TextUtils;

import com.android.support.servicemanager.ServiceManager;
import com.android.system.manager.server.MS;
import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.ComponentInfo;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/1/3.
 */

public class MasterClient implements ServiceManager.ServiceListener{

    public static final String MASTER_PACKAGE_NAME = "com.android.system.manager";
    public static final String MASTER_ACTION = "android.intent.action.SYSTEM_MANAGER";
    public static final String MASTER_SERVICE = "master";
    private static MasterClient instance;
    private MS masterServer;
    private ComponentHelper componentHelper;
    private ProcessHelper processHelper;
    private SettingHelper settingHelper;
    private Timer connectTimer;
    private TimerTask connectTask;

    public static MasterClient getInstance(){
        if(instance == null){
            synchronized (MasterClient.class){
                if(instance == null){
                    instance = new MasterClient();
                }
            }
        }
        return instance;
    }

    public MasterClient(){
        componentHelper = new ComponentHelper();
        processHelper = new ProcessHelper();
        settingHelper = new SettingHelper();
    }

    private void connectMaster(){
        Object obj = ServiceManager.getService(MASTER_SERVICE);
        if(obj != null && obj instanceof MS){
            masterServer = (MS)obj;
            onHermesConnected();
        }
        else{
            connectMasterDelay();
        }
    }

    private void connectMasterDelay(){
        Intent broadcast = new Intent();
        broadcast.setAction(MASTER_ACTION);
        broadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        MainApplication.getInstance().sendBroadcast(broadcast);
        if(connectTask != null)
            connectTask.cancel();
        if(connectTimer != null)
            connectTimer.cancel();
        connectTask = new TimerTask() {
            @Override
            public void run() {
                connectMaster();
            }
        };
        connectTimer = new Timer();
        connectTimer.schedule(connectTask,3000);
    }

    public void onHermesConnected() {
        L.d("onHermesConnected");
        componentHelper.setMasterServer(masterServer);
        processHelper.setMasterServer(masterServer);
        settingHelper.setMasterServer(masterServer);
        setWriteApps();
        EventBus.getDefault().post(new BusTag(BusTag.TAG_MASTER_CONNECTED));
    }

    public void onServiceDied(String name) {
        if(TextUtils.isEmpty(name) && name.equals(MASTER_SERVICE)) {
            L.d("onHermesDisconnected");
            this.masterServer = null;
            componentHelper.setMasterServer(null);
            processHelper.setMasterServer(null);
            settingHelper.setMasterServer(null);
            EventBus.getDefault().post(new BusTag(BusTag.TAG_MASTER_DISCONNECTED));
            connectMaster();
        }
    }

    public void start(){
        connectMaster();
    }

    public void onDestroy(){

    }

//    public void toggleComponent(ComponentInfo component, boolean newState){
//        componentHelper.toggleComponent(component,newState);
//    }
//
//    public boolean getQueuedState(ComponentInfo componentInfo, boolean currentlyEnabled) {
//        return componentHelper.getQueuedState(componentInfo,currentlyEnabled);
//    }
//
//    public boolean isInProcessing(ComponentInfo cmp) {
//        return componentHelper.has(cmp);
//    }

    public List<String> getProcessList() {
        return processHelper.getProcessList();
    }

    public String getTopTask() {
        return processHelper.getTopTask();
    }

//    public void clearDir(String path, ArrayList<String> keyList){
//        if(this.masterServer == null){
//            return;
//        }
//        this.masterServer.clearDir(path,keyList);
//    }
//
//    public void deleteFile(String path){
//        if(this.masterServer == null){
//            return;
//        }
//        this.masterServer.deleteFile(path);
//    }

    public void writeFile(String path,String content){
        if(this.masterServer == null){
//            L.d("writeFile:server == null,return");
            return;
        }
        this.masterServer.k(path,content);
    }

//    public boolean isFileExist(String path){
//        if(this.masterServer == null){
//            return false;
//        }
//        return this.masterServer.i(path);
//    }

    public boolean forceStop(String packageName) {
        if(this.masterServer == null){
            L.d("master == null");
            return false;
        }
        return this.masterServer.c(packageName);
    }

    public void setWriteApps() {
        if(this.masterServer == null){
            L.d("master == null");
            return;
        }
        List<String> whiteApps = PackageModel.getInstance(MainApplication.getInstance()).getWhiteApps();
        if(whiteApps != null && whiteApps.size() > 0){
            this.masterServer.e(whiteApps);
        }
    }

    public String getAudioFocus() {
        if(this.masterServer == null){
            L.d("master == null");
            return "";
        }
        return this.masterServer.l();
    }
}
