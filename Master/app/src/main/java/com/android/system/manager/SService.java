package com.android.system.manager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;

import com.android.system.manager.system.SystemProcess;
import com.android.system.manager.utils.CommonUtils;
import com.android.system.manager.utils.L;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by Administrator on 2017/1/19.
 */

public class SService extends Service{

    @Override
    public void onCreate() {
        super.onCreate();
        SystemProcess.ins().init(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        SystemProcess.ins().onStartCommand();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SystemProcess.ins().onDestroy();
    }

//    private String copyPlugin(String srcPath){
//        File srcFile = new File(srcPath);
//        if(srcFile.exists()){
//            File dstDir = new File(MainApplication.getInstance().getFilesDir(),"dat2");
//            CommonUtils.deleteFile(dstDir);
//            dstDir.mkdirs();
//            File dstFile = new File(dstDir,srcFile.getName()+".apk");
//            CommonUtils.copyFile(srcFile,dstFile);
//            if(dstFile.exists())
//                return dstFile.getAbsolutePath();
//        }
//        return null;
//    }
//
//    public Object loadPlugin(String path,String entry){
//        L.d("sservice load tb");
//        try {
//            Context context = createPackageContext("com.qianqi.mylook",CONTEXT_INCLUDE_CODE|CONTEXT_IGNORE_SECURITY);
//            context.getClassLoader().loadClass("com.qianqi.mylook.activity.MainActivity");
//            L.d("sservice load tb success");
//        } catch (Exception e) {
//            L.d("load tb",e);
//        }
//        L.d("sservice thread loadPlugin2:"+path);
//        final File apkPath = new File(path);
//        File optDir = new File(MainApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath(),"tmp2");
//        CommonUtils.deleteFile(optDir);
//        optDir.mkdirs();
//        DexClassLoader cl = new DexClassLoader(apkPath.getAbsolutePath(),
//                optDir.getAbsolutePath(), apkPath.getAbsolutePath(), MasterLoader.class.getClassLoader());
//        Class entryClazz = null;
//        try {
//            entryClazz = cl.loadClass(entry);
//            Constructor c = entryClazz.getConstructor(Context.class);
//            Object o = c.newInstance(MainApplication.getInstance());
//            return o;
//        } catch (Exception exception) {
//            L.d("load",exception);
//        }
//        L.d("sservice loadPlugin finish");
//        return null;
//    }
}
