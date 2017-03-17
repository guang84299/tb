package com.qianqi.mylook.model;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.PreferenceHelper;
import com.qianqi.mylook.R;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.client.MasterClient;
import com.qianqi.mylook.learning.UsageCache;
import com.qianqi.mylook.utils.CommonUtils;
import com.qianqi.mylook.utils.FileUtils;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by Administrator on 2017/1/4.
 */

public class PackageModel extends BroadcastReceiver{
    public static final Uri URI_AUTO_START = Uri.parse("content://com.qianqi.mylook.provider/autostart");
    public static final Uri URI_SETTING = Uri.parse("content://com.qianqi.mylook.provider/setting");
    public static final String KEY_FIRST_RUN = "first_run";
    public static final String KEY_SMART_MODE_LIST = "smart_list";
    public static final String KEY_GAME_MODE_LIST = "game_list";
    public static final int POWER_MODE_GAME = 0;
    public static final int POWER_MODE_SMART = 1;
    public static final int POWER_MODE_PERFORMANCE = 2;
    public static final int DEFAULT_POWER_MODE = POWER_MODE_SMART;
    public static final int MSG_LOAD = 1;
    public static final int MSG_PACKAGE_ADD = 2;
    public static final int MSG_PACKAGE_REMOVE = 3;
    public static final String ACTION_MODE_UPDATE = "mylook.action.mode_update";
    public static final String ACTION_SMART_UPDATE = "mylook.action.smart_update";
    public static final String ACTION_DEBUG = "mylook.action.dbm";

    public static List<String> qianqiApps = null;
    public static List<String> smartSystemApps = null;
    public static List<String> imApps = null;

    private static PackageModel instance;
    private Context appContext;
    private ArrayList<String> gameModeList = null;
    private ArrayList<String> smartModeList = null;
    private ArrayList<EnhancePackageInfo> packageList;
    private List<String> whiteApps;
    private String topPackageName = "";
    private PackageReader reader;
    private HandlerThread thread;
    private Handler handler;
    private boolean firstRun = true;

    public static PackageModel getInstance(Context appContext){
        if(instance == null){
            synchronized (PackageModel.class){
                if(instance == null){
                    instance = new PackageModel(appContext);
                }
            }
        }
        return instance;
    }

    public PackageModel(Context appContext){
        qianqiApps = new ArrayList<>(MainApplication.CORE_PKGS.length);
        Collections.addAll(qianqiApps, MainApplication.CORE_PKGS);
        String[] smartApps = appContext.getResources().getStringArray(R.array.smart_system_apps);
        smartSystemApps = new ArrayList<>(smartApps.length);
        Collections.addAll(smartSystemApps, smartApps);
        String[] ims = appContext.getResources().getStringArray(R.array.im_apps);
        imApps = new ArrayList<>(ims.length);
        Collections.addAll(imApps, ims);

        EventBus.getDefault().register(this);
        this.appContext = appContext;
        firstRun = PreferenceHelper.getInstance().common().getBoolean(KEY_FIRST_RUN,true);
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        this.appContext.registerReceiver(this,filter);
        IntentFilter syncFilter = new IntentFilter();
        syncFilter.addAction(ACTION_MODE_UPDATE);
        syncFilter.addAction(ACTION_SMART_UPDATE);
        syncFilter.addAction(ACTION_DEBUG);
        this.appContext.registerReceiver(this,syncFilter);
        reader = new PackageReader(appContext);
        thread = new HandlerThread(PackageModel.class.getSimpleName());
        thread.start();
        handler = new Handler(thread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                PackageModel.this.handleMessage(msg);
            }
        };
    }

    public void onDestroy(){
        this.appContext.unregisterReceiver(this);
        EventBus.getDefault().unregister(this);
    }

    public boolean firstRun(){
        return firstRun;
    }

    public void startLoad(){
        handler.sendEmptyMessage(MSG_LOAD);
    }

    public int getPowerMode(){
        int mode = DEFAULT_POWER_MODE;
        ContentResolver resolver = appContext.getContentResolver();
        Cursor cursor = resolver.query(URI_SETTING,null,"_key=\"powerMode\"",null,null);
        if(cursor != null){
            if(cursor.moveToNext()){
                mode = cursor.getInt(cursor.getColumnIndex("_value"));
            }
            cursor.close();
        }
        return mode;
    }

    public void setPowerMode(int mode){
        ContentResolver resolver = appContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("_key", "powerMode");
        values.put("_value",mode);
        resolver.insert(URI_SETTING,values);
        appContext.sendBroadcast(new Intent(ACTION_MODE_UPDATE));
    }

    public boolean getAutoStart(String packageName){
        boolean res = true;
        ContentResolver resolver = appContext.getContentResolver();
        Cursor cursor = resolver.query(URI_AUTO_START,null,"packageName=?",new String[]{packageName},null);
        if(cursor != null){
            if(cursor.moveToNext()){
                res = cursor.getInt(cursor.getColumnIndex("allow")) == 1;
            }
            cursor.close();
        }
        return res;
    }

    public void setAutoStart(String packageName,boolean bool){
        ContentResolver resolver = appContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("packageName", packageName);
        values.put("allow",bool?1:0);
        resolver.insert(URI_AUTO_START,values);
        updateState();
        postPackageList(BusTag.TAG_PACKAGE_AUTOSTART_UPDATE);
    }

    public void setSmartState(String packageName,boolean bool){
        Intent intent = new Intent(ACTION_SMART_UPDATE);
        intent.putExtra("packageName",packageName);
        intent.putExtra("bool",bool);
        appContext.sendBroadcast(intent);
    }

    private void deleteAutoStart(String packageName){
        ContentResolver resolver = appContext.getContentResolver();
        int row = resolver.delete(URI_AUTO_START,"packageName=?",new String[]{packageName});
    }

    public void initState(){
        smartModeList = new ArrayList<>();
        gameModeList = new ArrayList<>();
        if(firstRun){
            if(packageList != null){
                Set<String> smartSet = new HashSet<>();
                Set<String> gameSet = new HashSet<>();
                ContentResolver resolver = appContext.getContentResolver();
                for (int i = 0;i < packageList.size();i++){
                    EnhancePackageInfo p = packageList.get(i);
                    ContentValues values = new ContentValues();
                    values.put("packageName", p.packageName);
                    values.put("allow", 0);
                    resolver.insert(URI_AUTO_START, values);
                }
                gameSet.addAll(imApps);
                PreferenceHelper.getInstance().power().edit().putStringSet(KEY_SMART_MODE_LIST,smartSet).commit();
                PreferenceHelper.getInstance().power().edit().putStringSet(KEY_GAME_MODE_LIST,gameSet).commit();
                smartModeList.addAll(smartSet);
                gameModeList.addAll(gameSet);
            }
            PreferenceHelper.getInstance().common().edit().putBoolean(KEY_FIRST_RUN,false).commit();
        }
        else{
            Set<String> smartSet = PreferenceHelper.getInstance().power().getStringSet(KEY_SMART_MODE_LIST,null);
            if(smartSet != null){
                smartModeList.addAll(smartSet);
            }
            Set<String> gameSet = PreferenceHelper.getInstance().power().getStringSet(KEY_GAME_MODE_LIST,null);
            if(gameSet != null){
                gameModeList.addAll(gameSet);
            }
        }
    }

    public boolean inSmartMode(String packageName){
        if(smartModeList == null)
            return false;
        return smartModeList.contains(packageName);
    }

    public boolean inGameMode(String packageName){
        if(gameModeList == null)
            return false;
        return gameModeList.contains(packageName);
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void onProcessUpdate(BusTag event){
//        L.d("on process update");
        if(event.tag.equals(BusTag.TAG_REQUEST_PROCESS_UPDATE)){
            List<String> runningPackages = MasterClient.getInstance().getProcessList();
            updateProcessInfo(runningPackages);
            postPackageList(BusTag.TAG_PACKAGE_PROCESS_UPDATE);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void onWindowChanged(BusTag event){
        if(event.tag.equals(BusTag.TAG_WINDOW_CHANGED)){
            if(event.data != null && event.data instanceof String){
                String top = (String) event.data;
                if(!top.equals(topPackageName)) {
                    setTopPackageName(top);
                    EventBus.getDefault().post(new BusTag(BusTag.TAG_TOP_TASK_CHANGED));
                }
            }
        }
    }

    private void updateProcessInfo(List<String> runningPackages){
        if(packageList != null && runningPackages != null){
            for(EnhancePackageInfo p:packageList){
                String packageName = p.packageName;
                if(!runningPackages.contains(packageName)){
                    p.isRunning = false;
                }
                else{
                    p.isRunning = true;
                }
            }
        }
    }

    private void updateState(){
        if(packageList != null){
            for (int i = 0;i < packageList.size();i++){
                EnhancePackageInfo p = packageList.get(i);
                p.allowAutoStart = getAutoStart(p.packageName);
                p.setInSmartList(inSmartMode(p.packageName));
            }
        }
    }

    private void postPackageList(String tag){
        if(packageList == null){
            EventBus.getDefault().post(new BusTag(tag));
            return;
        }
        EventBus.getDefault().post(new BusTag(tag));
    }

    public boolean hasInit(){
        return packageList != null;
    }

    public List<EnhancePackageInfo> getPackageList(PackageFilter filter){
        if(packageList == null){
            return null;
        }
        if(filter == null){
            return (List<EnhancePackageInfo>) packageList.clone();
        }
        return filter.filterPackageList(packageList);
    }

    public boolean isPackageExist(String packageName){
        if(packageList == null)
            return false;
        boolean exist = false;
        for(EnhancePackageInfo p:packageList){
            if(p.packageName.equals(packageName)) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    public EnhancePackageInfo getPackageInfo(String packageName){
        for(EnhancePackageInfo p:packageList){
            if(p.packageName.equals(packageName)) {
                return p;
            }
        }
        return null;
    }

    public List<String> getWhiteApps() {
        return whiteApps;
    }

    public String getTopPackageName() {
        return topPackageName;
    }

    private void setTopPackageName(String packageName){
        this.topPackageName = packageName;
        File dir = MainApplication.getInstance().getFilesDir();
        File logFile = new File(dir,"shared_tools");
        FileUtils.writeFile(logFile,this.topPackageName,false);
    }

    private void handleMessage(Message msg){
        String packageName;
        switch (msg.what){
            case MSG_LOAD:
                if(TextUtils.isEmpty(topPackageName)) {
                    setTopPackageName(MasterClient.getInstance().getTopTask());
                }
                if(packageList != null) {
                    packageList.clear();
                    packageList = null;
                }
                boolean ignoreIcon = false;
                String processName = CommonUtils.getProcessName(MainApplication.getInstance());
                if(processName.contains(":core"))
                    ignoreIcon = true;
                reader.loadAllPackages(false, ignoreIcon, new PackageReader.OnLoadListener() {
                    @Override
                    public void onLoadPackageInfo(ArrayList<EnhancePackageInfo> list,List<String> whiteList) {
                        packageList = list;
                        whiteApps = whiteList;
                        initState();
                        updateState();
                        postPackageList(BusTag.TAG_PACKAGE_UPDATE);
                        MasterClient.getInstance().setWriteApps();
                    }

                    @Override
                    public void onLoadPackageIcon(ArrayList<EnhancePackageInfo> list) {
                        packageList = list;
                        postPackageList(BusTag.TAG_PACKAGE_UPDATE);
                    }
                });
                break;
            case MSG_PACKAGE_ADD:
                if(packageList == null){
                    return;
                }
                packageName = (String) msg.obj;
                if(!isPackageExist(packageName)){
                    EnhancePackageInfo p = reader.loadPackage(true,packageName);
                    if(p != null){
                        p.allowAutoStart = false;
                        p.setInSmartList(inSmartMode(p.packageName));
                        packageList.add(p);
                        setAutoStart(p.packageName,p.allowAutoStart);
                        postPackageList(BusTag.TAG_PACKAGE_UPDATE);
                    }
                }
                break;
            case MSG_PACKAGE_REMOVE:
                if(packageList == null){
                    return;
                }
                packageName = (String) msg.obj;
                for(EnhancePackageInfo p:packageList){
                    if(p.packageName.equals(packageName)) {
                        packageList.remove(p);
                        deleteAutoStart(packageName);
                        postPackageList(BusTag.TAG_PACKAGE_UPDATE);
                        break;
                    }
                }
                UsageCache.deleteDir(packageName);
                break;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
            if(handler == null){
                return;
            }
            String data = intent.getDataString();
            if(data == null || data.length() < 9)
                return;
            Message msg = new Message();
            msg.what = MSG_PACKAGE_ADD;
            String packageName = data.substring(8);
            msg.obj = packageName;
            handler.sendMessage(msg);
        }
        else if(action.equals(Intent.ACTION_PACKAGE_CHANGED)){

        }
        else if(action.equals(Intent.ACTION_PACKAGE_REMOVED)){
            if(handler == null){
                return;
            }
            String data = intent.getDataString();
            if(data == null || data.length() < 9)
                return;
            Message msg = new Message();
            msg.what = MSG_PACKAGE_REMOVE;
            String packageName = data.substring(8);
            msg.obj = packageName;
            handler.sendMessage(msg);
        }
        else if(action.equals(ACTION_MODE_UPDATE)){
            EventBus.getDefault().post(new BusTag(BusTag.TAG_POWER_MODE_UPDATE));
        }
        else if(action.equals(ACTION_SMART_UPDATE)){
            String packageName = intent.getStringExtra("packageName");
            boolean bool = intent.getBooleanExtra("bool",false);
            if(smartModeList == null)
                return;
            if(packageList != null){
                for (int i = 0;i < packageList.size();i++){
                    EnhancePackageInfo p = packageList.get(i);
                    if(p.packageName.equals(packageName))
                        p.setInSmartList(bool);
                }
            }
            if(bool && !smartModeList.contains(packageName)){
                smartModeList.add(packageName);
                Set<String> set = new HashSet<>(smartModeList.size());
                set.addAll(smartModeList);
                PreferenceHelper.getInstance().power().edit().putStringSet(KEY_SMART_MODE_LIST, set).commit();
            }
            if(!bool && smartModeList.contains(packageName)){
                smartModeList.remove(packageName);
                Set<String> set = new HashSet<>(smartModeList.size());
                set.addAll(smartModeList);
                PreferenceHelper.getInstance().power().edit().putStringSet(KEY_SMART_MODE_LIST, set).commit();
            }
            EventBus.getDefault().post(new BusTag(BusTag.TAG_PACKAGE_SMART_UPDATE));
        }
        else if(action.equals(ACTION_DEBUG)){
            L.DEBUG = !L.DEBUG;
            MasterClient.getInstance().updateDebug(L.DEBUG);
        }
    }
}
