package com.android.system.manager.system;

import android.app.ActivityManager;
import android.app.IProcessObserver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.android.internal.util.MemInfoReader;
import com.android.system.manager.MasterConstant;
import com.android.system.manager.utils.FileUtils;
import com.android.system.manager.utils.L;
import com.android.system.manager.utils.ReflectUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
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
    private int pk = D;

    public M(Context appContext){
        this.t = appContext;
//        clearLog();
        po();
        ls();
        la();
        ipo();
        rn();
        rm();
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
            L.d("process",e);
        }
    }

    synchronized public void ls(){
        ContentResolver resolver = t.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse(SettingObserver.URI), null, "_key=\"powerMode\"", null, null);
        if(cursor == null)
            return;
        if(cursor.moveToNext()){
            pk = cursor.getInt(cursor.getColumnIndex("_value"));
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

    private long wl(Object processList){
        try {
            MemInfoReader minfo = new MemInfoReader();
            minfo.readMemInfo();
            long mTotalMemMb = minfo.getTotalSize()/(1024*1024);
            if(mTotalMemMb > 600){
                return -1;
            }
            Object t = ReflectUtils.getValue(processList,"LMK_TARGET");
            Object mOomAdj = ReflectUtils.getValue(processList,"mOomAdj");
            Object mOomMinFree = ReflectUtils.getValue(processList,"mOomMinFree");
            if(t == null || mOomAdj == null || mOomMinFree == null ||
                    !(t instanceof Byte) || !(mOomAdj instanceof int[]) || !(mOomMinFree instanceof int[]))
                return -1;
            byte Target = (byte) t;
            int[] oomAdj = (int[]) mOomAdj;
            int[] oomMinFree = (int[]) mOomMinFree;
//            String s = "";
//            for(int i:oomMinFree){
//                s += i+",";
//            }
//            L.d("minfree="+s);
            ByteBuffer buf = ByteBuffer.allocate(4 * (2*oomAdj.length + 1));
            buf.putInt(Target);
            int PAGE_SIZE = 4*1024;
            for (int i=0; i<oomAdj.length; i++) {
                if(i == oomAdj.length - 1){
                    buf.putInt((oomMinFree[i-1]*1024)/PAGE_SIZE);
                }
                else{
                    buf.putInt((oomMinFree[i]*1024)/PAGE_SIZE);
                }
                buf.putInt(oomAdj[i]);
            }
            ReflectUtils.callMethod(processList,"writeLmkd",new Class[]{ByteBuffer.class},new Object[]{buf});
            return oomMinFree[oomAdj.length-2]* 1024;
        } catch (Exception e) {
            L.d("wl",e);
        }
        return -1;
    }

    public void rm(){
        Object s = SystemServicesHelper.getAMS(t);
        if(s != null) {
            try {
                Object o = ReflectUtils.getValue(s, "mProcessList");
                if (o == null) {
                    return;
                }
                long m2 = wl(o);
                if(m2 <= 0){
                    Object m = ReflectUtils.callMethod(o,"getMemLevel",new Class[]{Integer.TYPE},new Object[]{Integer.MAX_VALUE});
                    if(m instanceof Long) {
//                    L.d("pl:"+m);
                        m2 = (long) m;
                    }
                }
                if(m2 > 0){
                    File dir = SystemProcess.ins().getContext().getFilesDir();
                    File logFile = new File(dir,"m");
                    m2 = (long) Math.pow(m2/1024/1024,2);
                    FileUtils.writeFile(logFile,m2+"",false);
                    Intent intent = new Intent();
                    intent.setAction("mylook.action.m_update");
                    SystemProcess.ins().getContext().sendBroadcast(intent);
                    Log.d("s-s","m="+m2);
                }
                else
                    Log.d("s-s","m=-1");
            } catch (Exception | Error e) {
                L.d("pl failed",e);
            }
        }
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
                if(o == null){
                    return false;
                }
                if(o.getClass().getName().contains("PP")){
//                    fw = o;
//                    L.d("has replace fw!!!!!!!!!!!!!!");
                    return true;
                }
                Object m = ReflectUtils.getValue(o,"mAms");
                Object h = ReflectUtils.getValue(o,"mHandler");
//                Class amsInterface = SystemServicesHelper.getSystemClassLoader().loadClass("com.android.server.firewall.IntentFirewall.AMSInterface");
                Class pp = SystemServicesHelper.getSystemClassLoader(t).loadClass("com.android.system.manager.system.PP");
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
        if(pk == P) {
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
        if(pk == P) {
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
        } catch (Exception e) {
            L.d("forceStopPackage",e);
        }
        return false;
    }

    public void d() {
        t.getContentResolver().unregisterContentObserver(ao);
        t.getContentResolver().unregisterContentObserver(so);
    }
}
