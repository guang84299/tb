package com.qianqi.mylook.learning;

import android.app.ActivityManager;
import android.util.Log;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.client.MasterClient;
import com.qianqi.mylook.model.PackageFilter;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.utils.L;
import com.qianqi.mylook.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/16.
 * 常驻应用不参与使用习惯的学习，因为永远不会回收他们的内存
 */

public class UsageManager {

    public static final int GC_COUNT = 10;
    private HashMap<String,AppStat> appMap;
    private TimeInfo timeInfo;
    private NetworkInfo networkInfo;
    private String topPackage = "";
    private int tick = 0;

    public UsageManager(){
        timeInfo = new TimeInfo();
        networkInfo = new NetworkInfo();
        refreshAppMap();
    }

    synchronized private void refreshAppMap(){
        PackageFilter filter = new PackageFilter.Builder().persistent(false).qianqi(false).build();
//        PackageFilter filter = new PackageFilter.Builder().hasActivity(true).persistent(false).build();
        List<EnhancePackageInfo> pkgList = null;
        try{
            pkgList = PackageModel.getInstance(MainApplication.getInstance()).getPackageList(filter);
        }
        catch (ConcurrentModificationException e)
        {
            Log.e("-------","ConcurrentModificationException  learn!!!");
        }
        if(pkgList != null){
            if(appMap == null){
                appMap = new HashMap<>(pkgList.size());
            }
            ArrayList<String> pkgNameList = new ArrayList<>();
            for(EnhancePackageInfo pkg:pkgList){
                pkgNameList.add(pkg.packageName);
                AppStat item = appMap.get(pkg.packageName);
                if(item == null){
                    item = new AppStat(pkg.packageName);
                    appMap.put(pkg.packageName,item);
                }
            }
            Iterator<String> ite = appMap.keySet().iterator();
            while(ite.hasNext()){
                String name = ite.next();
                if(!pkgNameList.contains(name)){
                    ite.remove();
                }
            }
        }
        else{
            if(appMap != null){
                appMap.clear();
                appMap = null;
            }
        }
    }

    private boolean check(){
        if(appMap == null){
            refreshAppMap();
            return false;
        }
//        long timestamp = System.currentTimeMillis();
//        if(!timeCutting.isValid(timestamp)){
//            refreshAppMap();
//            return false;
//        }
        return true;
    }

    synchronized private void updateStat(String topPackage){
        tick++;
        if(tick >= GC_COUNT) {
            tick = 0;
            Runtime.getRuntime().gc();
        }
        long time = System.currentTimeMillis();
        timeInfo.setTime(time);
        int networkType = NetworkUtils.getConnectedType(MainApplication.getInstance());
        networkInfo.setState(networkType);
        for (String s : appMap.keySet()) {
            AppStat item = appMap.get(s);
            item.addRecord(timeInfo, networkInfo, topPackage);
        }
    }

    public void onTimeChanged(){
//        if(!check())
//            return;
//        updateStat(false);
    }

    public void onNetworkChanged(){
        if(!check())
            return;
        updateStat(PackageModel.getInstance(MainApplication.getInstance()).getTopPackageName());
    }

    public void onPackageChanged(){
        refreshAppMap();
    }

    public void onTaskChanged(){
        if(!check())
            return;
        String curTopPackage = PackageModel.getInstance(MainApplication.getInstance()).getTopPackageName();
        if(!curTopPackage.equals(topPackage) && appMap.containsKey(curTopPackage)){
            topPackage = curTopPackage;
//            L.d("update top:"+topPackage);
            updateStat(topPackage);
        }
    }

    synchronized public void onFlushData() {
        if(appMap == null)
            return;
        for (String s : appMap.keySet()) {
            AppStat item = appMap.get(s);
            if (item != null) {
                item.flush();
            }
        }
    }
}
