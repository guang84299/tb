package com.android.system.manager.server;

import android.app.ActivityManager;
import android.app.IProcessObserver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.android.system.manager.SService;
import com.android.system.manager.server.callback.MonitorCallback;
import com.android.system.manager.utils.L;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/12.
 */

public class MasterServerImpl extends MasterServer{
    private Context appContext;
    private IProcessObserver mProcessObserver;
    private MonitorCallback cb;
    private List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = new ArrayList<>(0);

    public MasterServerImpl(Context appContext){
        this.appContext = appContext;
        startSService();
        refreshRunningAppProcesses();
        initProcessObserver();
    }

    @Override
    public void destroy() {
        mProcessObserver = null;
        appContext = null;
    }

    public void startSService(){
//        L.d("Master onCreate:"+android.os.Process.myPid());
        Intent intent = new Intent();
        intent.setClass(appContext, SService.class);
        appContext.startService(intent);
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
//                        refreshRunningAppProcesses();
//                        if (cb != null)
//                            cb.onChanged(runningAppProcessInfoList);
                    }

                    @Override
                    public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
//                        L.d("onProcessStateChanged");
                        refreshRunningAppProcesses();
                        if (cb != null)
                            cb.onChanged(runningAppProcessInfoList);
                    }

                    @Override
                    public void onProcessDied(int pid, int uid) throws RemoteException {
//                        L.d("onProcessDied");
                        refreshRunningAppProcesses();
                        if (cb != null)
                            cb.onChanged(runningAppProcessInfoList);
                    }
                };
                registerMethod.invoke(iActivityManager, mProcessObserver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean monitorAppProcess(MonitorCallback callback) {
        this.cb = callback;
        if(mProcessObserver == null){
            initProcessObserver();
        }
        return mProcessObserver != null;
    }

    private void refreshRunningAppProcesses(){
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        runningAppProcessInfoList = am.getRunningAppProcesses();
    }

    @Override
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
//        L.d("Calling getRunningAppProcesses() directly");
        return runningAppProcessInfoList;
    }

    @Override
    public List<ActivityManager.RunningTaskInfo> getRunningTasks() {
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = am.getRunningTasks(1);
//        for(ActivityManager.RunningTaskInfo taskInfo:taskList){
//            L.d("task:"+taskInfo.topActivity.getPackageName());
//        }
        return taskList;
    }

    @Override
    public void enableAccessibilityService(String s) {
        SettingHelper.enableAccessibilityService(appContext,s);
    }
}
