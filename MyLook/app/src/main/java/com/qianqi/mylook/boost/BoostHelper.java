package com.qianqi.mylook.boost;

import android.app.ActivityManager;
import android.util.Log;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.client.MasterClient;
import com.qianqi.mylook.learning.UsagePredictor;
import com.qianqi.mylook.model.PackageFilter;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.utils.CollectionUtils;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/1/23.
 */

public class BoostHelper {

    private int mode;
    private BoostComparator comparator;

    public BoostHelper(){
        mode = PackageModel.getInstance(MainApplication.getInstance()).getPowerMode();
        comparator = new BoostComparator();
        comparator.setMode(mode);
    }

    public void updateMode(){
        mode = PackageModel.getInstance(MainApplication.getInstance()).getPowerMode();
        comparator.setMode(mode);
    }

    /*
    * 1.前台应用不杀，系统进程不杀，常驻应用不杀
    * 2.当前运行进程的应用按白名单分成两部分，优先杀白名单之外的
    * 3.两部分内部按使用情况排序，优先杀使用少的（机器学习预测使用概率较低的）
    * */
    public boolean boost() {
        if(mode == PackageModel.POWER_MODE_PERFORMANCE)
            return false;
        PackageFilter filter = new PackageFilter.Builder().running(true).persistent(false).top(false).qianqi(false).build();
        List<EnhancePackageInfo> runningPackageList = PackageModel.getInstance(MainApplication.getInstance()).getPackageList(filter);
        if(runningPackageList == null)
            return false;
        excludeByMode(runningPackageList);
        if(runningPackageList.size() > 1) {
            EventBus.getDefault().post(new BusTag(BusTag.TAG_FLUSH_LEARNING_DATA));
            UsagePredictor.getInstance().predict(runningPackageList);
            Collections.sort(runningPackageList, comparator);
        }
        for(EnhancePackageInfo p:runningPackageList){
            L.d("predict:"+p.getUsagePrediction()+","+p.getUsageQuickPrediction()+"#"+p.packageName);
        }
        if(runningPackageList.size() > 0){
            EnhancePackageInfo lastApp = runningPackageList.get(runningPackageList.size()-1);
            lastApp.setStopping(true);
            PackageModel.getInstance(MainApplication.getInstance()).setAutoStart(lastApp.packageName,false);
            boolean res = MasterClient.getInstance().forceStop(lastApp.packageName);
            L.d("boost:"+lastApp.packageName+","+res);
            Log.d("MyLookLog","boost:"+lastApp.packageName);
            return true;
        }
        return false;
    }

    private void excludeByMode(List<EnhancePackageInfo> runningPackageList){
        List<EnhancePackageInfo> firstList = new ArrayList<>();
        Iterator<EnhancePackageInfo> ite = runningPackageList.iterator();
        while(ite.hasNext()){
            EnhancePackageInfo p = ite.next();
            if(mode == PackageModel.POWER_MODE_GAME){
                if(PackageModel.getInstance(MainApplication.getInstance()).inGameMode(p.packageName)){
                    firstList.add(p);
                }
            }
            else if(mode == PackageModel.POWER_MODE_SMART){
                if(PackageModel.getInstance(MainApplication.getInstance()).inSmartMode(p.packageName)){
                    firstList.add(p);
                }
            }
        }
        if(firstList.size() != runningPackageList.size()){
            runningPackageList.removeAll(firstList);
        }
    }

    public int getMode() {
        return mode;
    }
}
