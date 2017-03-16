package com.qianqi.mylook.boost;

import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioManager;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.client.MasterClient;
import com.qianqi.mylook.model.PackageFilter;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.utils.L;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Administrator on 2017/1/23.
 * 根据lowmemorykiller的阈值清理内存
 * TODO:内存阈值获取，进程数量限制，杀进程速度提升
 */

public class MemHelper {

    public static final int GC_COUNT = 10;
    public static final float DEFAULT_MIN_FREE = 0.2f;
    public static final float MIN_FREE_ADJUST = 0.03f;
    public static final int LEVEL = 1;
    private File zoneInfoFile = new File("/proc/zoneinfo");
//    private Object memInfoReader;
    private long minFree = 200*1024*1024;
    private ActivityManager am;
    private long totalMem = -1;
    private char[] zoneBuffer = new char[8192];
    private ProtectionMatcher protectionMatcher;
    private MemMatcher highMatcher;
    private MemMatcher freeMatcher;
    private MemMatcher fileMatcher;
    private MemMatcher shmemMatcher;
    private int tick = 0;
//    private long hiddenAppThreshold = -1;

    public MemHelper(){
//        try {
//            Class clazz = Class.forName("com.android.internal.util.MemInfoReader");
//            memInfoReader = clazz.newInstance();
//        } catch (Exception e) {
//            L.d("mem",e);
//        }
        protectionMatcher = new ProtectionMatcher();
        highMatcher = new MemMatcher(new char[]{'h','i','g','h'});
        freeMatcher = new MemMatcher(new char[]{'n','r','_','f','r','e','e','_','p','a','g','e','s'});
        fileMatcher = new MemMatcher(new char[]{'n','r','_','f','i','l','e','_','p','a','g','e','s'});
        shmemMatcher = new MemMatcher(new char[]{'n','r','_','s','h','m','e','m'});
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
        long totalSize = totalMem;
        long sysMinFree = readAndCheckSysMinFree();
        if(sysMinFree > -1){
            if(totalSize > -1){
                return (long) (sysMinFree + totalSize*MIN_FREE_ADJUST);
            }
        }
        else{
            if(totalSize > -1){
                L.d("default min free");
                return (long) (totalSize*DEFAULT_MIN_FREE + totalSize*MIN_FREE_ADJUST);
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
//            L.d("read minfree",e);
        }
        return -1;
    }

    class ProtectionMatcher{
        StringBuilder sb;
        String target = "protection: (0, 2080, 2080)";
        int index = -1;

        public ProtectionMatcher(){
            sb = new StringBuilder(128);
        }

        public long onChar(char c){
            if(c == '('){
                index = 0;
            }
            else if(c == ')'){
                String s = sb.substring(0,index);
                String[] split = s.split(",");
                long max = 0;
                for(String item:split){
                    max = Math.max(max,Long.parseLong(item.trim()));
                }
                index = -1;
                return max;
            }
            else{
                if(index >= 0){
                    sb.insert(index,c);
                    index++;
                }
            }
            return -1;
        }
    }

    class MemMatcher{
        StringBuilder sb;
        char[] matchTarget;
        String shmem_target = "nr_shmem     433";
        String file_target = "nr_file_pages 46232";
        String free_target = "nr_free_pages 8185";
        String high_target = "high     3206";
        int matchIndex = 0;
        boolean matchSuccess = false;
        int index = -1;

        public MemMatcher(char[] target){
            matchTarget = target;
            sb = new StringBuilder(128);
        }

        public long onChar(char c){
            if(!matchSuccess){
                if(c == matchTarget[matchIndex]){
                    if(matchIndex < matchTarget.length-1){
                        matchIndex++;
                    }
                    else{
                        matchIndex = 0;
                        matchSuccess = true;
                        index = 0;
                    }
                }
                else{
                    matchIndex = 0;
                }
            }
            else{
                if(Character.isDigit(c)){
                    sb.insert(index,c);
                    index++;
                }
                else{
                    if(index > 0){
                        String s = sb.substring(0,index);
                        index = -1;
                        matchIndex = 0;
                        matchSuccess = false;
                        return Long.parseLong(s);
                    }
                }
            }
            return -1;
        }
    }

    private long readLmkFree(){
        InputStreamReader reader = null;
        try{
            long freePage = 0;
            long filePage = 0;
            long shmem = 0;
            long reserve = 0;
            FileInputStream is = new FileInputStream(zoneInfoFile);
            reader = new InputStreamReader(is);
            int count = reader.read(zoneBuffer);
            int index = 0;
            while(index < count){
                char c = zoneBuffer[index];
                index++;
                long protection = protectionMatcher.onChar(c);
                if(protection > 0){
                    reserve += protection;
                }
                long high = highMatcher.onChar(c);
                if(high > 0){
                    reserve += high;
                }
                long free = freeMatcher.onChar(c);
                if(free > 0){
                    freePage += free;
                }
                long file = fileMatcher.onChar(c);
                if(file > 0){
                    filePage += file;
                }
                long sh = shmemMatcher.onChar(c);
                if(sh > 0){
                    shmem += sh;
                }
            }
            freePage = freePage - reserve;
            filePage = filePage - shmem;
            long max = Math.max(freePage,filePage);
//            L.d("free,file="+freePage*4*1024+","+filePage*4*1024);
            return max * 4 * 1024;
        } catch (Exception e){
            L.d("readFree",e);
        } catch (Error e){
            L.d("readFree",e);
        } finally {
            if(reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        tick++;
        if(tick >= GC_COUNT) {
            tick = 0;
            Runtime.getRuntime().gc();
        }
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
        if(runningSize < 4){
            L.d("running low");
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
