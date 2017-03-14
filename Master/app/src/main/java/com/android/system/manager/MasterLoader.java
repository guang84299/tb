package com.android.system.manager;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.support.servicemanager.ServiceManager;
import com.android.system.manager.utils.CommonUtils;
import com.android.system.manager.utils.L;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by Administrator on 2017/3/8.
 */

public class MasterLoader implements ILoader{
    private static MasterLoader instance = null;
    private Object masterPlugin = null;

    public static MasterLoader ins(){
        if(instance == null){
            synchronized (MasterLoader.class){
                if(instance == null){
                    instance = new MasterLoader();
                }
            }
        }
        return instance;
    }

    /*load*/
    public void o(String path,String entry,String serviceEntry){
        String newPath = copyPlugin(path);
        if(!TextUtils.isEmpty(newPath)){
            masterPlugin = loadPlugin(newPath,entry);
        }
        else{
            L.d("load failed");
        }
    }

    private String copyPlugin(String srcPath){
        File srcFile = new File(srcPath);
        if(srcFile.exists()){
            File dstDir = new File(MainApplication.getInstance().getFilesDir(),"dat");
            CommonUtils.deleteFile(dstDir);
            dstDir.mkdirs();
            File dstFile = new File(dstDir,srcFile.getName());
            CommonUtils.copyFile(srcFile,dstFile);
            if(dstFile.exists())
                return dstFile.getAbsolutePath();
        }
        return null;
    }

    public Object loadPlugin(String path,String entry){
        final File apkPath = new File(path);
        File optDir = new File(MainApplication.getInstance().getFilesDir().getAbsolutePath(),"tmp");
        CommonUtils.deleteFile(optDir);
        optDir.mkdirs();
        DexClassLoader cl = new DexClassLoader(apkPath.getAbsolutePath(),
                optDir.getAbsolutePath(), apkPath.getAbsolutePath(), MasterLoader.class.getClassLoader());
        Class entryClazz = null;
        try {
            entryClazz = cl.loadClass(entry);
            Constructor c = entryClazz.getConstructor(new Class[]{Context.class,Class.class});
            Object o = c.newInstance(new Object[]{MainApplication.getInstance(), ServiceManager.class});
            return o;
        } catch (Exception exception) {
            L.d("load",exception);
        }
        return null;
    }

    public void onDestroy(){
        if(masterPlugin != null){
            try {
                Method m = masterPlugin.getClass().getMethod("d");
                m.setAccessible(true);
                m.invoke(masterPlugin);
            } catch (Exception e) {
                L.d("onDestroy",e);
            }
        }
    }
}
