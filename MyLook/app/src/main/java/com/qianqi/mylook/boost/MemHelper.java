package com.qianqi.mylook.boost;

import android.app.ActivityManager;
import android.content.Context;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.client.MasterClient;
import com.qianqi.mylook.model.PackageFilter;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.utils.L;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * Created by Administrator on 2017/1/23.
 * 根据lowmemorykiller的阈值清理内存
 *
 */

public class MemHelper {

    public static final float MIN_FREE_ADJUST = 0.03f;
    public static final int LEVEL = 1;
//    private Object memInfoReader;
    private long minFree = 200*1024*1024;
    private ActivityManager am;
    private long totalMem = -1;
//    private long hiddenAppThreshold = -1;

    public MemHelper(){
//        try {
//            Class clazz = Class.forName("com.android.internal.util.MemInfoReader");
//            memInfoReader = clazz.newInstance();
//        } catch (Exception e) {
//            L.d("mem",e);
//        }
        am = (ActivityManager) MainApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        totalMem = memoryInfo.totalMem;
        long sysMin = getMinFreeMem();
        if(sysMin > 0)
            minFree = sysMin;
//        hiddenAppThreshold = getThreshold(memoryInfo);
    }

//    private long getThreshold(ActivityManager.MemoryInfo memoryInfo){
//        try {
//            long systemThreshold = (long) ReflectUtils.getValue(memoryInfo,"hiddenAppThreshold");
//            return (long) (systemThreshold + totalMem*MIN_FREE_ADJUST);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }

    private long getMinFreeMem(){
        long sysMinFree = readAndCheckSysMinFree();
        if(sysMinFree > -1){
            long totalSize = totalMem;
            if(totalSize > -1){
                return (long) (sysMinFree + totalSize*MIN_FREE_ADJUST);
            }
        }
        return -1;
    }

    public long readAndCheckSysMinFree(){
        try{
            File readFile = new File("/sys/module/lowmemorykiller/parameters/minfree");
            BufferedReader reader = new BufferedReader(new FileReader(readFile));
            String params = reader.readLine();
            if(params != null){
                String[] split = params.split(",");
                if(split.length >= 2){
                    long lastFree = Long.parseLong(split[split.length-1]);
                    long levelFree = Long.parseLong(split[split.length-LEVEL]);
                    if(lastFree != levelFree){
                        split[split.length-1] = split[split.length-LEVEL];
                        String newParams = "";
                        for(String s:split){
                            newParams = newParams + s + ",";
                        }
//                        L.d("write:"+newParams.substring(0,newParams.length()-1));
                        MasterClient.getInstance().writeFile(readFile.getPath(),newParams.substring(0,newParams.length()-1));
                    }
                    return levelFree * 4 * 1024;
                }
            }
        } catch (Exception e){
            L.d("read minfree",e);
        }
        return -1;
    }

    private long readLmkFree(){
        try{
            long freePage = 0;
            long filePage = 0;
            long shmem = 0;
            long reserve = 0;
            File readFile = new File("/proc/zoneinfo");
            BufferedReader reader = new BufferedReader(new FileReader(readFile));
            String line = null;
            while((line = reader.readLine()) != null){
                line = line.trim();
                String[] split = line.split("\\s+");
                if(split.length == 2){
                    String key = split[0];
                    if(key.equals("nr_free_pages")){
                        freePage += Long.parseLong(split[1]);
                    }
                    else if(key.equals("nr_file_pages")){
                        filePage += Long.parseLong(split[1]);
                    }
                    else if(key.equals("nr_shmem")){
                        shmem += Long.parseLong(split[1]);
                    }
                    else if(key.equals("high")){
                        reserve += Long.parseLong(split[1]);
                    }
                }
                if(line.startsWith("protection:")){
                    String value = line.substring(line.indexOf("(")+1,line.indexOf(")"));
                    split = value.split(",");
                    long max = 0;
                    for(String s:split){
                        max = Math.max(max,Long.parseLong(s.trim()));
                    }
                    reserve += max;
                }
            }
            freePage = freePage - reserve;
            filePage = filePage - shmem;
            long max = Math.max(freePage,filePage);
//            L.d("free,file="+freePage*4*1024+","+filePage*4*1024);
            return max * 4 * 1024;
        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

//    public void readMemInfo(){
//        if(memInfoReader == null){
//            return;
//        }
//        try {
//            ReflectUtils.invokeMethod(memInfoReader,"readMemInfo");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public long getTotalSize(){
//        if(memInfoReader == null){
//            return -1;
//        }
//        try {
//            return (long) ReflectUtils.invokeMethod(memInfoReader,"getTotalSize");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }
//
//    public long getFreeSize(){
//        if(memInfoReader == null){
//            return -1;
//        }
//        try {
//            return (long) ReflectUtils.invokeMethod(memInfoReader,"getFreeSize");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }
//
//    public long getCachedSize(){
//        if(memInfoReader == null){
//            return -1;
//        }
//        try {
//            return (long) ReflectUtils.invokeMethod(memInfoReader,"getCachedSize");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return -1;
//    }
//
//    public long getCachedPssSize(){
//        List<ActivityManager.RunningAppProcessInfo> runningProcessList = MasterClient.getInstance().getProcessList();
//        if(runningProcessList == null){
//            return 0;
//        }
//        long size = 0;
//        for(ActivityManager.RunningAppProcessInfo processInfo:runningProcessList){
//            if(processInfo.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND){
//                size += processInfo.
//            }
//        }
//        long[] pss = ActivityManagerNative.getDefault().getProcessPss(pids);
//    }

    public long getFreeMem(){
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    public boolean timeToBoost() {
        if(minFree == -1){
            return false;
        }
//        readMemInfo();
//        long free = getFreeSize()+getCachedSize();
        int runningSize = 0;
        PackageFilter filter = new PackageFilter.Builder().running(true).system(false).qianqi(false).build();
        List<EnhancePackageInfo> runningPackageList = PackageModel.getInstance(MainApplication.getInstance()).getPackageList(filter);
        if(runningPackageList != null){
            runningSize = runningPackageList.size();
        }
        if(runningSize < 5){
            return false;
        }
        long free = readLmkFree();
        if(free <= 0){
            return false;
        }
        L.d("threshold,free="+minFree/1024+","+free/1024+"                 ("+totalMem+")");
        boolean lowMem = free <= minFree;
        if(lowMem) {
            return true;
        }
        if(runningSize > 7){
            return true;
        }
        return false;
    }
}
