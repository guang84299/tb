package com.android.system.manager.server;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.android.system.manager.server.callback.MonitorCallback;
import com.android.system.manager.utils.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import xiaofei.library.hermes.Hermes;
import xiaofei.library.hermes.annotation.ClassId;
import xiaofei.library.hermes.annotation.MethodId;

/**
 * Created by Administrator on 2017/1/3.
 */

@ClassId("MasterServer")
public class MasterServer {
    private static MasterServer instance = new MasterServer();
    public MasterServerImpl impl;
    private Context appContext;

    public MasterServer(){

    }

    public void start(Context context){
        L.d("master start");
        this.appContext = context;
        impl = new MasterServerImpl(context);
        initHermes();
    }

    public void destroy(){
        if(impl != null){
            impl.destroy();
            impl = null;
        }
    }

    private void initHermes(){
        Hermes.init(appContext);
        Hermes.register(MasterServer.class);
        Hermes.register(MonitorCallback.class);
    }

    public boolean allowAutoStart(String packageName){
        return impl.allowAutoStart(packageName);
    }

    public boolean allowAutoStart(int uid){
        return impl.allowAutoStart(uid);
    }

    @MethodId("GetInstance")
    public static MasterServer getInstance(){
//        L.d("getInstance");
        return instance;
    }

    @MethodId("exeCmd")
    public void exeCmd(MonitorCallback callback) {
        //...
    }

    @MethodId("setComponentEnabledSetting")
    public int setComponentEnabledSetting(ComponentName c,boolean desiredState){
        L.d("setComponentEnabledSetting:"+android.os.Process.myPid()+","+android.os.Process.myUid());
        if(appContext == null)
            return -1;
        PackageManager pm = appContext.getPackageManager();
//        if (appContext.checkCallingOrSelfPermission(Manifest.permission.CHANGE_COMPONENT_ENABLED_STATE)
//                == PackageManager.PERMISSION_GRANTED) {
        if(true){
            L.d("Calling setComponentEnabledState() directly");
            pm.setComponentEnabledSetting(
                    c, desiredState ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            return pm.getComponentEnabledSetting(c);
        }
        else{
            L.d("no permission for setComponentEnabledState()");
        }
        return -1;
    }

    @MethodId("forceStopPackage")
    public boolean forceStopPackage(String packageName){
        L.d("forceStopPackage:"+packageName);
        if(appContext == null)
            return false;
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

    @MethodId("monitorAppProcess")
    public boolean monitorAppProcess(final MonitorCallback callback) {
        return impl.monitorAppProcess(callback);
    }

    @MethodId("getRunningAppProcesses")
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        return impl.getRunningAppProcesses();
    }

    @MethodId("getRunningTasks")
    public List<ActivityManager.RunningTaskInfo> getRunningTasks(){
        return impl.getRunningTasks();
    }

    @MethodId("enableAccessibilityService")
    public void enableAccessibilityService(String s){
        impl.enableAccessibilityService(s);
    }

    @MethodId("clearDir")
    public void clearDir(String dirPath,ArrayList<String> keyList) {
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

    @MethodId("isFileExist")
    public boolean isFileExist(String path){
        if(appContext == null)
            return false;
//        L.d("Calling isFileExist() directly");
        File f = new File(path);
        return f.exists();
    }

    @MethodId("deleteFile")
    public void deleteFile(String path){
        if(appContext == null)
            return;
//        L.d("Calling deleteFile() directly:"+path);
        File f = new File(path);
        f.delete();
    }

    @MethodId("writeFile")
    public void writeFile(String path,String content){
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
}
