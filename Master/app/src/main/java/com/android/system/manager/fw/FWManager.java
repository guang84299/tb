package com.android.system.manager.fw;

import android.app.ActivityManager;
import android.app.IProcessObserver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.android.system.manager.server.MasterConstant;
import com.android.system.manager.utils.FileUtils;
import com.android.system.manager.utils.SystemServicesHelper;
import com.android.system.manager.utils.L;
import com.android.system.manager.utils.ReflectUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/19.
 */

public class FWManager {
    public static final int POWER_MODE_GAME = 0;
    public static final int POWER_MODE_SMART = 1;
    public static final int POWER_MODE_PERFORMANCE = 2;
    public static final int DEFAULT_POWER_MODE = POWER_MODE_SMART;
    private Context appContext;
    private Object fw;
    private List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = new ArrayList<>(0);
    private AutostartObserver autostartObserver;
    private SettingObserver settingObserver;
    private List<String> disallowAutoStartList = null;
    private List<String> blockList = new ArrayList<>();
    private IProcessObserver mProcessObserver;
    private int powerMode = DEFAULT_POWER_MODE;

    public FWManager(Context appContext){
        this.appContext = appContext;
        clearLog();
        initProviderObserver();
        loadSetting();
        loadAutoStartDB();
        initProcessObserver();
        refreshRunningAppProcesses();
        boolean success = initProcessFirewall();
        if(success){
            checkAutoStart();
        }
    }

    private void checkAutoStart(){
        if(runningAppProcessInfoList == null)
            return;
        if(disallowAutoStartList == null)
            return;
        for(ActivityManager.RunningAppProcessInfo processInfo:runningAppProcessInfoList){
            String[] packageList = processInfo.pkgList;
            boolean stop = true;
            for(String packageName:packageList){
                if(allowAutoStart(packageName)){
                    stop = false;
                    break;
                }
            }
            if(stop){
                for(String packageName:packageList){
                    forceStopPackage(packageName);
                    logBlock("[stop]"+packageName);
                }
            }
        }
    }

    private void clearLog(){
        File dir = appContext.getFilesDir();
        File logFile = new File(dir,"block");
        logFile.delete();
    }

    private void initProviderObserver(){
        ContentResolver resolver = appContext.getContentResolver();
        autostartObserver = new AutostartObserver(appContext, null);
        autostartObserver.setCallback(this);
        resolver.registerContentObserver(Uri.parse(AutostartObserver.URI), true, autostartObserver);
        settingObserver = new SettingObserver(appContext, null);
        settingObserver.setCallback(this);
        resolver.registerContentObserver(Uri.parse(SettingObserver.URI), true, settingObserver);
    }

    private void initProcessObserver() {
        if(mProcessObserver != null){
            return;
        }
        try {
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = activityManagerNative.getMethod("getDefault");
            Object iActivityManager = getDefaultMethod.invoke((Object[]) null, (Object[]) null);
            if (iActivityManager != null) {
                Method registerMethod = activityManagerNative.getMethod("registerProcessObserver", new Class[]{IProcessObserver.class});
                mProcessObserver = new IProcessObserver.Stub() {

                    @Override
                    public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
//                        L.d("onForegroundActivitiesChanged");
                        refreshRunningAppProcesses();
                    }

                    @Override
                    public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
//                        L.d("onProcessStateChanged");
                        refreshRunningAppProcesses();
                    }

                    @Override
                    public void onProcessDied(int pid, int uid) throws RemoteException {
//                        L.d("onProcessDied");
                        refreshRunningAppProcesses();
                    }
                };
                registerMethod.invoke(iActivityManager, mProcessObserver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized public void loadSetting(){
        ContentResolver resolver = appContext.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse(SettingObserver.URI), null, "_key=\"powerMode\"", null, null);
        if(cursor == null)
            return;
        if(cursor.moveToNext()){
            powerMode = cursor.getInt(cursor.getColumnIndex("_value"));
        }
        cursor.close();
    }

    synchronized public void loadAutoStartDB(){
        List<String> tmp = new ArrayList<>();
        ContentResolver resolver = appContext.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse(AutostartObserver.URI), null, null, null, null);
        if(cursor == null)
            return;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            String packageName = cursor.getString(cursor.getColumnIndex("packageName"));
            boolean allow = cursor.getInt(cursor.getColumnIndex("allow")) == 1;
            if(!allow){
                tmp.add(packageName);
            }
        }
        cursor.close();
        disallowAutoStartList = tmp;
    }

    public boolean initProcessFirewall(){
//        L.d("start init fw!!!!!!!!!!!!!!");
//        if(fw != null){
//            L.d("has init fw!!!!!!!!!!!!!!");
//            return;
//        }
        Object ams = SystemServicesHelper.getAMS(appContext);
        if(ams != null){
            try {
                Object oriFW = ReflectUtils.getValue(ams,"mIntentFirewall");
                if(oriFW.getClass().getName().contains("FWProxy")){
                    fw = oriFW;
                    L.d("has replace fw!!!!!!!!!!!!!!");
                    return false;
                }
                Object am = ReflectUtils.getValue(oriFW,"mAms");
                Object handler = ReflectUtils.getValue(oriFW,"mHandler");
//                Class amsInterface = SystemServicesHelper.getSystemClassLoader().loadClass("com.android.server.firewall.IntentFirewall.AMSInterface");
                Class processFirewall = SystemServicesHelper.getSystemClassLoader(appContext).loadClass("com.android.system.manager.fw.FWProxy");
                Constructor[] cons=processFirewall.getDeclaredConstructors();
                if(cons.length > 0){
                    Constructor constructor = cons[0];//ReflectUtils.getConstructor(processFirewall,true,true,amsInterface,Handler.class);
                    fw = constructor.newInstance(am,handler);
                    Method[] methods = fw.getClass().getDeclaredMethods();
                    for(Method m:methods){
//                        L.d("fw:"+m.getName());
                    }
                    ReflectUtils.callMethod(fw,"setOri",new Class[]{oriFW.getClass()},new Object[]{oriFW});
//                    Class master = SystemServicesHelper.getSystemClassLoader(appContext).loadClass("com.qianqi.master.server.MasterServerImpl");
                    ReflectUtils.callMethod(fw,"setManager",new Class[]{FWManager.class},new Object[]{this});
                    ReflectUtils.setValue(ams,"mIntentFirewall",fw);
                    L.d("replace fw success!!!!!!!!!!!!!!!!!");
                    return true;
                }
            } catch (Exception | Error e) {
                L.d("replace fw failed!!!!!!!!!!!!!!!!!",e);
            }
        }
        return false;
    }

    private void refreshRunningAppProcesses(){
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        runningAppProcessInfoList = am.getRunningAppProcesses();
    }

    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
//        L.d("Calling getRunningAppProcesses() directly");
        return runningAppProcessInfoList;
    }

    public boolean allowAutoStart(String packageName) {
        if(powerMode == POWER_MODE_PERFORMANCE) {
//            L.d("performance mode,allow");
            return true;
        }
        for(String s: MasterConstant.CORE_PKGS){
            if(s.equals(packageName)){
                return true;
            }
        }
        if(disallowAutoStartList == null)
            return true;
        return !disallowAutoStartList.contains(packageName);
    }

    public boolean allowAutoStart(int uid) {
        if(powerMode == POWER_MODE_PERFORMANCE) {
//            L.d("performance mode,allow");
            return true;
        }
        PackageManager pm = appContext.getPackageManager();
        String[] packages = pm.getPackagesForUid(uid);
        if(packages == null || packages.length < 1){
            return true;
        }
        for(String packageName:packages){
            if(allowAutoStart(packageName)){
                return true;
            }
        }
        return false;
    }

    public String getPackageForUid(int uid) {
        PackageManager pm = appContext.getPackageManager();
        String[] packages = pm.getPackagesForUid(uid);
        if(packages == null || packages.length < 1){
            return "null";
        }
        else return packages[0];
    }

    public void logBlock(String packageName){
        if(!this.blockList.contains(packageName)){
            this.blockList.add(packageName);
            File dir = appContext.getFilesDir();
            File logFile = new File(dir,"block");
            FileUtils.writeFile(logFile,packageName+"\n");
        }
    }

    public boolean forceStopPackage(String packageName){
//        L.d("forceStopPackage:"+packageName);
        if(appContext == null)
            return false;
//        L.d("Calling forceStopPackage() directly");
        ActivityManager mActivityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;
        try {
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(mActivityManager, packageName);  //packageName是需要强制停止的应用程序包名
            return true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void onDestroy() {
        appContext.getContentResolver().unregisterContentObserver(autostartObserver);
        appContext.getContentResolver().unregisterContentObserver(settingObserver);
    }
}
