package com.qianqi.mylook.client;

import android.app.ActivityManager;
import android.content.Context;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.bean.ComponentInfo;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import xiaofei.library.hermes.Hermes;
import xiaofei.library.hermes.HermesListener;
import xiaofei.library.hermes.HermesService;

/**
 * Created by Administrator on 2017/1/3.
 */

public class MasterClient extends HermesListener{

    public static final String MASTER_PACKAGE_NAME = "com.android.system.manager";
    private static MasterClient instance;
    private Context appContext;
    private IMasterServer masterServer;
    private ComponentHelper componentHelper;
    private ProcessHelper processHelper;
    private SettingHelper settingHelper;

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

    @Override
    public void onHermesConnected(Class<? extends HermesService> service) {
        L.d("onHermesConnected");
        masterServer = Hermes.getInstance(IMasterServer.class);
        componentHelper.setMasterServer(masterServer);
        processHelper.setMasterServer(masterServer);
        settingHelper.setMasterServer(masterServer);
        EventBus.getDefault().post(new BusTag(BusTag.TAG_MASTER_CONNECTED));
    }

    @Override
    public void onHermesDisconnected(Class<? extends HermesService> service) {
        L.d("onHermesDisconnected");
        super.onHermesDisconnected(service);
        this.masterServer = null;
        componentHelper.setMasterServer(null);
        processHelper.setMasterServer(null);
        settingHelper.setMasterServer(null);
        EventBus.getDefault().post(new BusTag(BusTag.TAG_MASTER_DISCONNECTED));
        Hermes.connectApp(appContext,MASTER_PACKAGE_NAME);
    }

    public void init(Context appContext){
        this.appContext = appContext;
        Hermes.setHermesListener(this);
        Hermes.connectApp(appContext,MASTER_PACKAGE_NAME);
        Hermes.register(ProcessHelper.class);
    }

    public void onDestroy(){
        Hermes.disconnect(appContext);
    }

    public void toggleComponent(ComponentInfo component, boolean newState){
        componentHelper.toggleComponent(component,newState);
    }

    public boolean getQueuedState(ComponentInfo componentInfo, boolean currentlyEnabled) {
        return componentHelper.getQueuedState(componentInfo,currentlyEnabled);
    }

    public boolean isInProcessing(ComponentInfo cmp) {
        return componentHelper.has(cmp);
    }

    public List<ActivityManager.RunningAppProcessInfo> getProcessList() {
        return processHelper.getProcessList();
    }

    public ActivityManager.RunningTaskInfo getTopTask() {
        return processHelper.getTopTask();
    }

    public void clearDir(String path, ArrayList<String> keyList){
        if(this.masterServer == null){
            return;
        }
        this.masterServer.clearDir(path,keyList);
    }

    public void deleteFile(String path){
        if(this.masterServer == null){
            return;
        }
        this.masterServer.deleteFile(path);
    }

    public void writeFile(String path,String content){
        if(this.masterServer == null){
//            L.d("writeFile:server == null,return");
            return;
        }
        this.masterServer.writeFile(path,content);
    }

    public boolean isFileExist(String path){
        if(this.masterServer == null){
            return false;
        }
        return this.masterServer.isFileExist(path);
    }

    public boolean forceStop(String packageName) {
        if(this.masterServer == null){
            L.d("master == null");
            return false;
        }
        return this.masterServer.forceStopPackage(packageName);
    }
}
