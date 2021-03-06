package com.qianqi.mylook.client;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.android.support.servicemanager.ServiceManager;
import com.android.system.manager.ILoader;
import com.android.system.manager.plugin.master.MS;
import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.PreferenceHelper;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.utils.FileUtils;
import com.qianqi.mylook.utils.L;
import com.qianqi.mylook.utils.NetworkUtils;
import com.qianqi.mylook.utils.StringUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/1/3.
 */

public class MasterClient implements ServiceManager.ServiceListener{

    public static final String MASTER_PLUGIN_DIR = "dat";
    public static final String MASTER_ACTION = "android.intent.action.SYSTEM_MANAGER";
    public static final String LOADER_SERVICE = "main";
    public static final String MASTER_SERVICE = "master";
    public static final String KEY_SYSTEM_INIT = "system_init";
    private static MasterClient instance;
    private MS masterServer;
    private ComponentHelper componentHelper;
    private ProcessHelper processHelper;
    private SettingHelper settingHelper;
    private Timer connectTimer;
    private TimerTask connectTask;
    private boolean systemHasInit = false;

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
        if(NetworkUtils.getConnectedType(MainApplication.getInstance()) != NetworkUtils.NETWORK_OFFLINE) {
            systemHasInit = true;
        }
        else{
            systemHasInit = PreferenceHelper.getInstance().common().getBoolean(KEY_SYSTEM_INIT,false);
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void onNetworkChanged(BusTag event){
        if(event.tag.equals(BusTag.TAG_NETWORK_CHANGED)) {
            if(NetworkUtils.getConnectedType(MainApplication.getInstance()) != NetworkUtils.NETWORK_OFFLINE) {
                PreferenceHelper.getInstance().common().edit().putBoolean(KEY_SYSTEM_INIT,true).commit();
                systemHasInit = true;
            }
        }
    }

    private void connectMaster(){
        L.d("connect master");
        if(!systemHasInit){
            L.d("system not init");
            connectMasterDelay(20000);
            return;
        }
        Object loaderObj = ServiceManager.getService(LOADER_SERVICE);
        if(loaderObj != null && loaderObj instanceof ILoader){
            L.d("find loader");
            ILoader loader = (ILoader)loaderObj;
            String pluginPath = copyPlugin();
            if(!TextUtils.isEmpty(pluginPath)){
                loader.o(pluginPath,"com.android.system.manager.plugin.e","");
            }
        }
        Object obj = ServiceManager.getService(MASTER_SERVICE);
        if(obj != null && obj instanceof MS){
            L.d("find master");
            masterServer = (MS)obj;
            onHermesConnected();
        }
        else{
            connectMasterDelay(5000);
        }
    }

    private String copyPlugin(){
        String assetsPath = "data/cfg.bin";
//        String dstDir = new File(MainApplication.getInstance().getFilesDir(),MASTER_PLUGIN_DIR).getAbsolutePath();
        File sd = FileUtils.getStorageFile(MainApplication.getInstance(),MASTER_PLUGIN_DIR);
        if(sd == null)
            return null;
        String dstDir = sd.getAbsolutePath();
        String dstFileName = StringUtils.stringToMD5(System.currentTimeMillis()+"");
        FileUtils.deleteFile(new File(dstDir));
        if(FileUtils.copyAssetsFile(MainApplication.getInstance(),assetsPath,dstDir,dstFileName)){
            return new File(dstDir,dstFileName).getAbsolutePath();
        }
        return null;
    }

    private void connectMasterDelay(long delay){
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
        connectTimer.schedule(connectTask,delay);
    }

    public void onHermesConnected() {
        L.d("onHermesConnected");
        PackageModel.getInstance(MainApplication.getInstance()).checkDisable();
        PackageModel.getInstance(MainApplication.getInstance()).updateList();
        componentHelper.setMasterServer(masterServer);
        processHelper.setMasterServer(masterServer);
        settingHelper.setMasterServer(masterServer);
        setWriteApps();
        EventBus.getDefault().post(new BusTag(BusTag.TAG_MASTER_CONNECTED));
    }

    public void onServiceDied(String name) {
        if(!TextUtils.isEmpty(name) && name.equals(MASTER_SERVICE)) {
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
        EventBus.getDefault().unregister(this);
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

    public void updateDebug(boolean bool){
        if(this.masterServer != null){
            this.masterServer.m(bool);
        }
    }

    public long getMinFree() {
        if(this.masterServer == null){
            return -1;
        }
        long m = this.masterServer.n();
        if(m > 0){
            m = (long) (Math.sqrt(m)*1024*1024);
            L.d("master min free:"+m);
        }
        return m;
    }

    public boolean disabled() {
        if(this.masterServer == null){
            return false;
        }
        return this.masterServer.p();
    }

    public String getWhiteList(){
        if(this.masterServer == null){
            return "";
        }
        return this.masterServer.r();
    }

    public String getBlackList(){
        if(this.masterServer == null){
            return "";
        }
        return this.masterServer.s();
    }

    public String getGrayList(){
        if(this.masterServer == null){
            return "";
        }
        return this.masterServer.t();
    }
}
