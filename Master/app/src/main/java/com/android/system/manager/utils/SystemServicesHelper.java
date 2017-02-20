package com.android.system.manager.utils;

import android.content.Context;

import com.android.system.manager.fw.FWManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * Created by Administrator on 2017/1/11.
 */

public class SystemServicesHelper {

    private static Context systemContext = null;
    private static ClassLoader classLoader = null;

    public static Context getSystemContext(){
        if(systemContext == null){
            Object activityThread = null;
            try {
                activityThread = ReflectUtils.invokeStaticMethod(SystemServicesHelper.class.getClassLoader(),"android.app.ActivityThread","currentActivityThread",null,null);
                //                L.d("isSystem:"+ReflectUtils.invokeStaticMethod(SystemServicesHelper.class.getClassLoader(),"android.app.ActivityThread","isSystem",null,null));
                systemContext = (Context) ReflectUtils.invokeMethod(activityThread,"getSystemContext");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return systemContext;
    }

    public static ClassLoader getSystemClassLoader(final Context context){
        if(classLoader == null){
            Object activityThread = null;
            try {
                Context systemContext = getSystemContext();
//               classLoader = new ClassLoader(SystemServicesHelper.class.getClassLoader()){
//                    @Override
//                    public Class<?> loadClass(String name) throws ClassNotFoundException {
//                        try {
//                            return systemContext.getClassLoader().loadClass(name);
//                        } catch (ClassNotFoundException e) {
//                            // ClassNotFoundException thrown if class not found
//                            // from the non-null parent class loader
//                        }
//                        return super.loadClass(name);
//                    }
//                };
                classLoader = new DexClassLoader(context.getApplicationInfo().sourceDir,
                        context.getApplicationInfo().dataDir, context.getApplicationInfo().nativeLibraryDir,
                        systemContext.getClassLoader()){

                    @Override
                    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//                        L.d("load class:"+name);
                        if(name.endsWith("FWManager")){
                            return FWManager.class;
                        }
                        return super.loadClass(name, resolve);
                    }

                    @Override
                    protected Class<?> findClass(String name) throws ClassNotFoundException {
//                        L.d("find class:"+name);
                        try {
                            Class clazz = super.findClass(name);
//                            L.d("dex find class:"+name);
                            return clazz;
                        } catch (ClassNotFoundException e) {
                            // ClassNotFoundException thrown if class not found
                            // from the non-null parent class loader
                        }
                        return getParent().loadClass(name);
                    }
                };
            } catch (Exception e) {
                StringWriter s = new StringWriter();
                PrintWriter writer = new PrintWriter(s);
                e.printStackTrace(writer);
                L.d(s.toString());
            }
        }
        return classLoader;
    }

    public static Object getAMS(Context context){
        try {
            ClassLoader classLoader = getSystemClassLoader(context);
            Class systemServiceManagerClazz = getSystemClassLoader(context).loadClass("com.android.server.SystemServiceManager");
            Object systemServiceManager = ReflectUtils.invokeStaticMethod(classLoader,"com.android.server.LocalServices","getService",Class.class,systemServiceManagerClazz);
//            Class localServices = classLoader.loadClass("com.android.server.LocalServices");
//            Field[] fields = localServices.getFields();
//            L.d("localService fields size="+fields.length);
//            if(fields.length > 0){
//                L.d("localService fields 0="+fields[0].getName());
//            }
//            Field field = localServices.getField("sLocalServiceObjects");
//            field.setAccessible(true);
//            ArrayMap map = (ArrayMap) field.get(null);
//            L.d("localService size="+map.keySet().size());
            ArrayList mServices = (ArrayList) ReflectUtils.getValue(systemServiceManager,"mServices");
            for(Object obj:mServices){
                L.d("find service:"+obj.getClass().getName());
                if(obj.getClass().getName().contains("ActivityManagerService$Lifecycle")){
//                    Class lifeCycle = Class.forName("com.android.server.am.ActivityManagerService.Lifecycle");
//                    Method method = ReflectUtils.getMethod(lifeCycle,"getService");
//                    Object res = ReflectUtils.invokeMethod(method,obj);obj
                    return ReflectUtils.invokeMethod(obj,"getService");
                }
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
        return null;
    }
}
