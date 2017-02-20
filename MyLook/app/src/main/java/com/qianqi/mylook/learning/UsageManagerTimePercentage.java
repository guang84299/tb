//package com.qianqi.mylook.learning;
//
//import android.app.ActivityManager;
//
//import com.qianqi.mylook.MainApplication;
//import com.qianqi.mylook.bean.EnhancePackageInfo;
//import com.qianqi.mylook.client.MasterClient;
//import com.qianqi.mylook.model.PackageModel;
//import com.qianqi.mylook.utils.L;
//import com.qianqi.mylook.utils.NetworkUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//
///**
// * Created by Administrator on 2017/1/16.
// */
//
//public class UsageManagerTimePercentage {
//
//    private HashMap<String,AppStat> dataMap;
//    private NetworkInfo networkInfo;
//    private TimeCutting timeCutting;
//
//    public UsageManagerTimePercentage(){
//        reset();
//    }
//
//    private void reset(){
//        ArrayList<EnhancePackageInfo> pkgList = PackageModel.getInstance(MainApplication.getInstance()).getPackageList();
//        if(pkgList != null){
//            if(dataMap == null){
//                dataMap = new HashMap<>(pkgList.size());
//            }
//            if(timeCutting == null)
//                timeCutting = new TimeCutting();
//            long time = System.currentTimeMillis();
//            timeCutting.start(time);
//            if(networkInfo == null)
//                networkInfo = new NetworkInfo();
//            networkInfo.start();
//            ActivityManager.RunningTaskInfo topTask = MasterClient.getInstance().getTopTask();
//            String topPackage = topTask != null?topTask.topActivity.getPackageName():"";
//            ArrayList<String> pkgNameList = new ArrayList<>();
//            for(EnhancePackageInfo pkg:pkgList){
//                pkgNameList.add(pkg.packageName);
//                AppStat item = dataMap.get(pkg.packageName);
//                if(item != null){
////                    item.setDate(date);
//                    if(pkg.packageName.equals(topPackage)){
//                        item.start(true);
//                    }
//                    else{
//                        item.start(false);
//                    }
//                }
//                else{
//                    item = new AppStat();
//                    item.setPackageName(pkg.packageName);
////                    item.setDate(date);
//                    if(pkg.packageName.equals(topPackage)){
//                        item.start(true);
//                    }
//                    else{
//                        item.start(false);
//                    }
//                    dataMap.put(pkg.packageName,item);
//                }
//            }
//            Iterator<String> ite = dataMap.keySet().iterator();
//            while(ite.hasNext()){
//                String name = ite.next();
//                if(!pkgNameList.contains(name)){
//                    ite.remove();
//                }
//            }
//
//        }
//        else{
//            if(dataMap != null){
//                dataMap.clear();
//                dataMap = null;
//            }
//        }
//    }
//
//    private boolean check(){
//        if(dataMap == null){
//            reset();
//            return false;
//        }
//        long timestamp = System.currentTimeMillis();
//        if(!timeCutting.isValid(timestamp)){
//            reset();
//            return false;
//        }
//        return true;
//    }
//
//    private void updateStat(boolean packageChanged){
//        long time = System.currentTimeMillis();
//        int networkType = NetworkUtils.getConnectedType(MainApplication.getInstance());
//        ActivityManager.RunningTaskInfo topTask = MasterClient.getInstance().getTopTask();
//        String topPackage = topTask != null?topTask.topActivity.getPackageName():"";
////        L.d("top package:"+topPackage);
//        ArrayList<String> pkgNameList = null;
//        if(packageChanged){
//            ArrayList<EnhancePackageInfo> pkgList = PackageModel.getInstance(MainApplication.getInstance()).getPackageList();
//            if(pkgList != null){
//                pkgNameList = new ArrayList<>(pkgList.size());
//                for(EnhancePackageInfo pkg:pkgList) {
//                    pkgNameList.add(pkg.packageName);
//                }
//                Iterator<String> ite = dataMap.keySet().iterator();
//                while(ite.hasNext()){
//                    String name = ite.next();
//                    if(!pkgNameList.contains(name)){
//                        ite.remove();
//                    }
//                }
//            }
//        }
//        while(timeCutting.canStep(time)){
//            int unit = timeCutting.getUnit();
//            String date = timeCutting.getDate();
//            timeCutting.step();
//            networkInfo.stop(timeCutting.getDelta());
//            Iterator<String> ite = dataMap.keySet().iterator();
//            while(ite.hasNext()){
//                AppStat item = dataMap.get(ite.next());
//                item.stop(timeCutting.getDelta());
//                L.d(item.getPackageName()+":"+unit+" "+ networkInfo.getOutput()+" "+item.getVisiblePercentage());
//                RecordItem record = new RecordItem(date,unit,networkInfo,item);
//                UsageCache.write(MainApplication.getInstance(),record);
//                item.start(item.isVisible());
//            }
//            networkInfo.start();
//        }
//        if(timeCutting.forward(time)){
//            networkInfo.forward(timeCutting.getDelta());
//            networkInfo.setState(networkType);
//            Iterator<String> ite = dataMap.keySet().iterator();
//            while(ite.hasNext()){
//                AppStat item = dataMap.get(ite.next());
//                item.forward(timeCutting.getDelta());
//                item.setVisible(item.getPackageName().equals(topPackage));
////                L.d(item.getPackageName()+":"+unit+" "+networkInfo.getOutput()+" "+item.getOutput());
//            }
//            if(packageChanged && pkgNameList != null){
//                for(String pkgName:pkgNameList){
//                    AppStat item = dataMap.get(pkgName);
//                    if(item == null){
//                        item = new AppStat();
//                        item.setPackageName(pkgName);
////                    item.setDate(date);
//                        if(pkgName.equals(topPackage)){
//                            item.start(true);
//                        }
//                        else{
//                            item.start(false);
//                        }
//                        dataMap.put(pkgName,item);
//                    }
//                }
//            }
//        }
//        else{
//            reset();
//        }
//    }
//
//    public void onTimeChanged(){
//        if(!check())
//            return;
//        updateStat(false);
//    }
//
//    public void onNetworkChanged(){
//        if(!check())
//            return;
//        updateStat(false);
//    }
//
//    public void onPackageChanged(){
//        if(!check())
//            return;
//        updateStat(true);
//    }
//
//    public void onTaskChanged(){
//        if(!check())
//            return;
//        updateStat(false);
//    }
//}
