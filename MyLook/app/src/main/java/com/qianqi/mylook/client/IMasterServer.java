package com.qianqi.mylook.client;

import android.app.ActivityManager;
import android.content.ComponentName;

import com.qianqi.mylook.client.callback.CommandCallback;
import com.qianqi.mylook.client.callback.MonitorCallback;

import java.util.ArrayList;
import java.util.List;

import xiaofei.library.hermes.annotation.Background;
import xiaofei.library.hermes.annotation.ClassId;
import xiaofei.library.hermes.annotation.MethodId;

/**
 * Created by Administrator on 2017/1/3.
 */

@ClassId("MasterServer")
public interface IMasterServer {

    @MethodId("exeCmd")
    void exeCmd(@Background CommandCallback callback);

    @MethodId("setComponentEnabledSetting")
    int setComponentEnabledSetting(ComponentName c,boolean desiredState);

    @MethodId("forceStopPackage")
    boolean forceStopPackage(String packageName);

    @MethodId("monitorAppProcess")
    boolean monitorAppProcess(final MonitorCallback callback);

    @MethodId("getRunningAppProcesses")
    List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();

    @MethodId("getRunningTasks")
    List<ActivityManager.RunningTaskInfo> getRunningTasks();

    @MethodId("clearDir")
    void clearDir(String dirPath,ArrayList<String> keyList);

    @MethodId("deleteFile")
    void deleteFile(String path);

    @MethodId("writeFile")
    void writeFile(String path,String content);

    @MethodId("isFileExist")
    boolean isFileExist(String path);

    @MethodId("enableAccessibilityService")
    public void enableAccessibilityService(String s);
}
