package com.android.system.manager.f;

import android.app.ActivityManager;
import android.app.IProcessObserver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.android.system.manager.server.MasterConstant;
import com.android.system.manager.utils.SystemServicesHelper;
import com.android.system.manager.utils.L;
import com.android.system.manager.utils.ReflectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/19.
 */

public class M {
    public static final int G = 0;
    public static final int S = 1;
    public static final int P = 2;
    public static final int D = S;
    private Context t;
//    private Object fw;
    private List<ActivityManager.RunningAppProcessInfo> r = new ArrayList<>(0);
    private AutostartObserver ao;
    private SettingObserver so;
    private List<String> d = null;//disallowAutoStartList
//    private List<String> blockList = new ArrayList<>();
    private IProcessObserver po;
    private int p = D;

    public M(Context appContext){
        this.t = appContext;
//        clearLog();
        po();
        ls();
        la();
        ipo();
        rn();
        boolean s = i();
        if(s){
            s();
        }
    }

    private void s(){
        if(r == null)
            return;
        if(d == null)
            return;
        for(ActivityManager.RunningAppProcessInfo processInfo: r){
            String[] l = processInfo.pkgList;
            boolean p = true;
            for(String n:l){
                if(c(n)){
                    p = false;
                    break;
                }
            }
            if(p){
                for(String n:l){
                    s(n);
//                    logBlock("[stop]"+n);
                }
            }
        }
    }

//    private void clearLog(){
//        File dir = t.getFilesDir();
//        File logFile = new File(dir,"block");
//        logFile.delete();
//    }

    private void po(){
        ContentResolver resolver = t.getContentResolver();
        ao = new AutostartObserver(t, null);
        ao.setCallback(this);
        resolver.registerContentObserver(Uri.parse(AutostartObserver.URI), true, ao);
        so = new SettingObserver(t, null);
        so.setCallback(this);
        resolver.registerContentObserver(Uri.parse(SettingObserver.URI), true, so);
    }

    private void ipo() {
        if(po != null){
            return;
        }
        try {
            Class<?> a = Class.forName("android.app.ActivityManagerNative");
            Method gd = a.getMethod("getDefault");
            Object m = gd.invoke((Object[]) null, (Object[]) null);
            if (m != null) {
                Method r = a.getMethod("registerProcessObserver", new Class[]{IProcessObserver.class});
                po = new IProcessObserver.Stub() {

                    @Override
                    public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
//                        L.d("onForegroundActivitiesChanged");
//                        rn();
                    }

                    @Override
                    public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
//                        L.d("onProcessStateChanged");
                        rn();
                    }

                    @Override
                    public void onProcessDied(int pid, int uid) throws RemoteException {
//                        L.d("onProcessDied");
                        rn();
                    }
                };
                r.invoke(m, po);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized public void ls(){
        ContentResolver resolver = t.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse(SettingObserver.URI), null, "_key=\"p\"", null, null);
        if(cursor == null)
            return;
        if(cursor.moveToNext()){
            p = cursor.getInt(cursor.getColumnIndex("_value"));
        }
        cursor.close();
    }

    synchronized public void la(){
        List<String> tmp = new ArrayList<>();
        ContentResolver resolver = t.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse(AutostartObserver.URI), null, null, null, null);
        if(cursor == null)
            return;
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            String p = cursor.getString(cursor.getColumnIndex("packageName"));
            boolean a = cursor.getInt(cursor.getColumnIndex("allow")) == 1;
            if(!a){
                tmp.add(p);
            }
        }
        cursor.close();
        d = tmp;
    }

    public boolean i(){
//        L.d("a init fw!!!!!!!!!!!!!!");
//        if(fw != null){
//            L.d("has init fw!!!!!!!!!!!!!!");
//            return;
//        }
        Object s = SystemServicesHelper.getAMS(t);
        if(s != null){
            try {
                Object o = ReflectUtils.getValue(s,"mIntentFirewall");
                if(o.getClass().getName().contains("PP")){
//                    fw = o;
//                    L.d("has replace fw!!!!!!!!!!!!!!");
                    return false;
                }
                Object m = ReflectUtils.getValue(o,"mAms");
                Object h = ReflectUtils.getValue(o,"mHandler");
//                Class amsInterface = SystemServicesHelper.getSystemClassLoader().loadClass("com.android.server.firewall.IntentFirewall.AMSInterface");
                Class pp = SystemServicesHelper.getSystemClassLoader(t).loadClass("com.android.system.manager.f.PP");
                Constructor[] cons=pp.getDeclaredConstructors();
                if(cons.length > 0){
                    Constructor constructor = cons[0];//ReflectUtils.getConstructor(processFirewall,true,true,amsInterface,Handler.class);
                    Object w = constructor.newInstance(m,h);
                    Method[] methods = w.getClass().getDeclaredMethods();
//                    for(Method m:methods){
////                        L.d("fw:"+m.getName());
//                    }
                    ReflectUtils.callMethod(w,"b",new Class[]{o.getClass()},new Object[]{o});
//                    Class master = SystemServicesHelper.getSystemClassLoader(t).loadClass("com.qianqi.master.server.MasterServerImpl");
                    ReflectUtils.callMethod(w,"a",new Class[]{M.class},new Object[]{this});
                    ReflectUtils.setValue(s,"mIntentFirewall",w);
//                    L.d("replace fw success!!!!!!!!!!!!!!!!!");
                    return true;
                }
            } catch (Exception | Error e) {
                L.d("pp failed",e);
            }
        }
        return false;
    }

    private void rn(){
        ActivityManager am = (ActivityManager) t.getSystemService(Context.ACTIVITY_SERVICE);
        r = am.getRunningAppProcesses();
    }

    /*getRunningAppProcesses*/
    public List<ActivityManager.RunningAppProcessInfo> r() {
//        L.d("Calling r() directly");
        return r;
    }

    /*allowAutoStart*/
    public boolean c(String packageName) {
        if(p == P) {
//            L.d("performance mode,allow");
            return true;
        }
        for(String s: MasterConstant.CORE_PKGS){
            if(s.equals(packageName)){
                return true;
            }
        }
        if(d == null)
            return true;
        return !d.contains(packageName);
    }

    public boolean c(int u) {
        if(p == P) {
//            L.d("performance mode,allow");
            return true;
        }
        PackageManager pm = t.getPackageManager();
        String[] packages = pm.getPackagesForUid(u);
        if(packages == null || packages.length < 1){
            return true;
        }
        for(String packageName:packages){
            if(c(packageName)){
                return true;
            }
        }
        return false;
    }

//    public String getPackageForUid(int uid) {
//        PackageManager pm = t.getPackageManager();
//        String[] packages = pm.getPackagesForUid(uid);
//        if(packages == null || packages.length < 1){
//            return "null";
//        }
//        else return packages[0];
//    }

//    public void logBlock(String packageName){
//        if(!this.blockList.contains(packageName)){
//            this.blockList.add(packageName);
//            File dir = t.getFilesDir();
//            File logFile = new File(dir,"block");
//            FileUtils.writeFile(logFile,packageName+"\n");
//        }
//    }

    public boolean s(String n){
//        L.d("forceStopPackage:"+packageName);
        if(t == null)
            return false;
//        L.d("Calling forceStopPackage() directly");
        ActivityManager a = (ActivityManager) t.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;
        try {
            method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(a, n);  //packageName是需要强制停止的应用程序包名
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

    public void d() {
        t.getContentResolver().unregisterContentObserver(ao);
        t.getContentResolver().unregisterContentObserver(so);
    }
}
