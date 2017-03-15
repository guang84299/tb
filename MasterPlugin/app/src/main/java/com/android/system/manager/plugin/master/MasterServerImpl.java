package com.android.system.manager.plugin.master;

import android.app.ActivityManager;
import android.content.Context;

import com.android.system.manager.plugin.utils.FileUtils;
import com.android.system.manager.plugin.utils.L;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/1/12.
 */

public class MasterServerImpl implements MS {
    private static MS instance = new MasterServerImpl();
    private List<String> whiteApps = new ArrayList<>(0);
//    private IProcessObserver mProcessObserver;
//    private List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = new ArrayList<>(0);

    public static MS ins(){
        return instance;
    }

    public MasterServerImpl(){

    }

    public void a(Context context){
//        refreshRunningAppProcesses();
//        initProcessObserver();
    }

    public void b() {
//        mProcessObserver = null;
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
//        L.d("Calling isFileExist() directly");
        File f = new File(path);
        return f.exists();
    }

    public void j(String path){
//        L.d("Calling deleteFile() directly:"+path);
        File f = new File(path);
        f.delete();
    }

    public void k(String path,String content){
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String l() {
        File dir = MasterProcess.ins().getContext().getFilesDir();
        File logFile = new File(dir,"af");
        List<String> logs = FileUtils.readFile(logFile);
        if(logs.size() > 0)
            return logs.get(0);
        else
            return "";
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
        ActivityManager am = (ActivityManager) MasterProcess.ins().getContext().getSystemService(Context.ACTIVITY_SERVICE);
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
        ActivityManager am = (ActivityManager) MasterProcess.ins().getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = am.getRunningTasks(1);
        if(taskList != null && taskList.size() > 0) {
            ActivityManager.RunningTaskInfo item = taskList.get(0);
            curTopPackage = item != null?item.topActivity.getPackageName():"";
        }
        return curTopPackage;
    }

    @Override
    public void g(String s) {
        SettingHelper.enableAccessibilityService(MasterProcess.ins().getContext(),s,true);
    }

    public boolean c(String packageName){
        ActivityManager mActivityManager = (ActivityManager) MasterProcess.ins().getContext().getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;
        try {
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(mActivityManager, packageName);  //packageName是需要强制停止的应用程序包名
//            L.d("forceStopPackage:"+packageName);
            return true;
        } catch (Exception e) {
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


    @Override
    public void m(boolean bool) {
        L.DEBUG = bool;
    }
}
