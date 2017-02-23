package com.android.system.manager.server;

import android.app.ActivityManager;
import android.content.Context;

import com.android.system.manager.utils.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/1/12.
 */

public class MasterServerImpl implements MS {
    private static MS instance = new MasterServerImpl();
    private Context appContext;
    private List<String> whiteApps = new ArrayList<>(0);
//    private IProcessObserver mProcessObserver;
//    private List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = new ArrayList<>(0);

    public static MS getInstance(){
        return instance;
    }

    public void a(Context context){
        this.appContext = context;
//        refreshRunningAppProcesses();
//        initProcessObserver();
    }

    public void b() {
//        mProcessObserver = null;
        appContext = null;
    }

//    private void initProcessObserver() {
//        if(mProcessObserver != null){
//            return;
//        }
//        try {
//            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
//            Method getDefaultMethod = activityManagerNative.getMethod("getDefault");
//            Object iActivityManager = getDefaultMethod.invoke((Object[]) null, (Object[]) null);
//            if (iActivityManager != null) {
//                Method registerMethod = activityManagerNative.getMethod("registerProcessObserver", new Class[]{IProcessObserver.class});
//                mProcessObserver = new IProcessObserver.Stub() {
//
//                    @Override
//                    public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
////                        L.d("onForegroundActivitiesChanged");
////                        refreshRunningAppProcesses();
////                        if (cb != null)
////                            cb.onChanged(runningAppProcessInfoList);
//                    }
//
//                    @Override
//                    public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
////                        L.d("onProcessStateChanged");
//                        refreshRunningAppProcesses();
//                        if (cb != null)
//                            cb.onChanged(runningAppProcessInfoList);
//                    }
//
//                    @Override
//                    public void onProcessDied(int pid, int uid) throws RemoteException {
////                        L.d("onProcessDied");
//                        refreshRunningAppProcesses();
//                        if (cb != null)
//                            cb.onChanged(runningAppProcessInfoList);
//                    }
//                };
//                registerMethod.invoke(iActivityManager, mProcessObserver);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void h(String dirPath,ArrayList<String> keyList) {
        if(appContext == null)
            return;
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        for(File f:files){
            String fileName = f.getName();
            if(!keyList.contains(fileName)){
//                L.d("Calling delete:"+f.getAbsolutePath());
                f.delete();
            }
        }
    }

    public boolean i(String path){
        if(appContext == null)
            return false;
//        L.d("Calling isFileExist() directly");
        File f = new File(path);
        return f.exists();
    }

    public void j(String path){
        if(appContext == null)
            return;
//        L.d("Calling deleteFile() directly:"+path);
        File f = new File(path);
        f.delete();
    }

    public void k(String path,String content){
        if(appContext == null)
            return;
//        L.d("Calling writeFile() directly");
        File f = new File(path);
        try {
            if(!f.exists()){
                if(!f.createNewFile())
                    return;
            }
            FileOutputStream out = new FileOutputStream(f);
            out.write(content.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public boolean monitorAppProcess(MonitorCallback callback) {
//        this.cb = callback;
//        if(mProcessObserver == null){
//            initProcessObserver();
//        }
//        return mProcessObserver != null;
//    }

//    private void refreshRunningAppProcesses(){
//        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
//        runningAppProcessInfoList = am.r();
//    }


    @Override
    public void e(List<String> writeApps) {
        if(writeApps != null)
            this.whiteApps = writeApps;
    }

    @Override
    public List<String> d() {
//        L.d("Calling r() directly");
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcessList = am.getRunningAppProcesses();
        List<String> runningPackages = new ArrayList<>(runningProcessList.size());
        String topPackage = f();
        for(ActivityManager.RunningAppProcessInfo process:runningProcessList){
            String[] pkgList = process.pkgList;
            boolean canStop = true;
            for(String packageName:pkgList){
                if(packageName.equals(topPackage) || whiteApps.contains(packageName)){
                    canStop = false;
                    break;
                }
            }
            if(canStop)
                Collections.addAll(runningPackages, pkgList);
        }
        return runningPackages;
    }

    @Override
    public String f() {
        String curTopPackage = "";
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = am.getRunningTasks(1);
        if(taskList != null && taskList.size() > 0) {
            ActivityManager.RunningTaskInfo item = taskList.get(0);
            curTopPackage = item != null?item.topActivity.getPackageName():"";
        }
        return curTopPackage;
    }

    @Override
    public void g(String s) {
        SettingHelper.enableAccessibilityService(appContext,s);
    }

    public boolean c(String packageName){
        if(appContext == null)
            return false;
        ActivityManager mActivityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;
        try {
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(mActivityManager, packageName);  //packageName是需要强制停止的应用程序包名
            L.d("forceStopPackage:"+packageName);
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

//    public int setComponentEnabledSetting(ComponentName c, boolean desiredState){
//        L.d("setComponentEnabledSetting:"+android.os.Process.myPid()+","+android.os.Process.myUid());
//        if(appContext == null)
//            return -1;
//        PackageManager pm = appContext.getPackageManager();
////        if (appContext.checkCallingOrSelfPermission(Manifest.permission.CHANGE_COMPONENT_ENABLED_STATE)
////                == PackageManager.PERMISSION_GRANTED) {
//        if(true){
//            L.d("Calling setComponentEnabledState() directly");
//            pm.setComponentEnabledSetting(
//                    c, desiredState ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
//                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
//            return pm.getComponentEnabledSetting(c);
//        }
//        else{
//            L.d("no permission for setComponentEnabledState()");
//        }
//        return -1;
//    }
}
