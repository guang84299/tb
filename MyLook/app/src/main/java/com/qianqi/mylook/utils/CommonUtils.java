package com.qianqi.mylook.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.qianqi.mylook.MainApplication;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2017/1/9.
 */

public class CommonUtils {

    public static void exit(){
        MobclickAgent.onKillProcess(MainApplication.getInstance());
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public static int dp2px(float dpValue) {
        final float scale = MainApplication.getInstance().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String getProcessName(Context context){
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process: list) {
            if(process.pid == pid)
            {
                processName = process.processName;
            }
        }
        return processName;
    }

    public static boolean isSameArray(float[] src,float[] dst){
        if(src == null || dst == null)
            return false;
        if(src.length == dst.length){
            for(int i = 0;i<src.length;i++){
                if(src[i] != dst[i]){
                    return false;
                }
            }
            return true;
        }
        else return false;
    }

    public static int getAppVersion(Context context) {
        PackageManager pm = context.getPackageManager();//得到PackageManager对象
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);//得到PackageInfo对象，封装了一些软件包的信息在里面
            int appVersion = pi.versionCode;//获取清单文件中versionCode节点的值
            return appVersion;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return -1;
    }

    public static String getAppChannel(Context context){
        ApplicationInfo appInfo = null;
        try {
            appInfo = MainApplication.getInstance().getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String channel = appInfo.metaData.getString("UMENG_CHANNEL");
            return channel;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return "";
    }

    public static final boolean isGpsEnabled(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
//        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps;
    }


    /** * 清除本应用所有的数据 * * @param context * @param filepath */
    public static void cleanApplicationData(Context context, String... filepath) {
        deleteDir(new File("/data/data/"+ context.getPackageName()).getAbsolutePath());
        deleteDir(context.getCacheDir().getAbsolutePath());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir().getAbsolutePath());
        }
        deleteDir(new File("/data/data/"+ context.getPackageName() + "/databases").getAbsolutePath());
        deleteDir(new File("/data/data/"+context.getPackageName() + "/shared_prefs").getAbsolutePath());
        deleteDir(context.getFilesDir().getAbsolutePath());
//        deleteDir(FileUtils.getStorageFile(context,"").getAbsolutePath());
        File updatef = FileUtils.getStorageFile(context,"updatenum");
        if(updatef != null && updatef.exists())
            updatef.delete();
    }


    //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
//        Log.e("-------",dir.getAbsolutePath());
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

}
