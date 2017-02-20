package com.qianqi.mylook.client;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.text.TextUtils;

import com.google.gson.internal.LinkedTreeMap;
import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.client.callback.MonitorCallback;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import xiaofei.library.hermes.annotation.ClassId;
import xiaofei.library.hermes.annotation.MethodId;

/**
 * Created by Administrator on 2017/1/4.
 */

@ClassId("MonitorCallback")
public class ProcessHelper implements MonitorCallback {

    private IMasterServer masterServer;

    public ProcessHelper() {
    }

    public void setMasterServer(IMasterServer server){
        this.masterServer = server;
//        if(this.masterServer != null){
//            boolean success = this.masterServer.monitorAppProcess(this);
//            L.d("monitor process:"+success);
//            EventBus.getDefault().post(new BusTag(BusTag.TAG_PROCESS_UPDATE,getProcessList()));
//        }
    }

    @MethodId("onChanged")
    public void onChanged(List<ActivityManager.RunningAppProcessInfo> list) {
        postProcess(list);
    }

    private void postProcess(List<ActivityManager.RunningAppProcessInfo> list){
        if(this.masterServer == null)
            return;
//        L.d("post process");
        List<ActivityManager.RunningAppProcessInfo> res = parseProcessList(list);
        EventBus.getDefault().post(new BusTag(BusTag.TAG_PROCESS_UPDATE,res));
    }

    public List<ActivityManager.RunningAppProcessInfo> getProcessList(){
        if(this.masterServer == null)
            return null;
        List list = this.masterServer.getRunningAppProcesses();
        return parseProcessList(list);
    }

    public ActivityManager.RunningTaskInfo getTopTask(){
        if(this.masterServer == null)
            return null;
        List list = this.masterServer.getRunningTasks();
        return parseTaskList(list);
    }

    private List<ActivityManager.RunningAppProcessInfo> parseProcessList(List list){
        if(list != null){
            List<ActivityManager.RunningAppProcessInfo> processInfoList = new ArrayList<>(list.size());
            for(Object item:list){
                LinkedTreeMap map = (LinkedTreeMap) item;
                ActivityManager.RunningAppProcessInfo processInfo = new ActivityManager.RunningAppProcessInfo();
                Set set = map.entrySet();
                for(Object entry:set){
                    LinkedTreeMap.Entry node = (LinkedTreeMap.Entry) entry;
                    String key = (String) node.getKey();
                    if(key.equals("importance")){
                        Double d = (Double) node.getValue();
                        processInfo.importance = d.intValue();
                    }
                    else if(key.equals("pid")){
                        Double d = (Double) node.getValue();
                        processInfo.pid = d.intValue();
                    }
                    else if(key.equals("pkgList")){
                        ArrayList pkgList = (ArrayList) node.getValue();
                        String[] array = new String[pkgList.size()];
                        for(int i = 0; i < pkgList.size();i++){
                            array[i] = pkgList.get(i).toString();
                        }
                        processInfo.pkgList = array;
                    }
                }
                processInfoList.add(processInfo);
            }
            return processInfoList;
        }
        return new ArrayList<>(0);
    }

    private ActivityManager.RunningTaskInfo parseTaskList(List list){
        if(list != null && list.size() > 0){
            Object item = list.get(0);
            LinkedTreeMap map = (LinkedTreeMap) item;
            ActivityManager.RunningTaskInfo processInfo = new ActivityManager.RunningTaskInfo();
            String packageName = null;
            String className = null;
            Set set = map.entrySet();
            for(Object entry:set){
                LinkedTreeMap.Entry node = (LinkedTreeMap.Entry) entry;
                String key = (String) node.getKey();
                if(key.equals("topActivity")){
                    LinkedTreeMap topActivity = (LinkedTreeMap) node.getValue();
                    set = topActivity.entrySet();
                    for(Object tmp:set){
                        node = (LinkedTreeMap.Entry) tmp;
                        key = (String) node.getKey();
                        if(key.equals("mClass")){
                            className = (String) node.getValue();
                        }
                        else if(key.equals("mPackage")){
                            packageName = (String) node.getValue();
                        }
                    }
                    break;
                }
            }
            if(!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)){
                processInfo.topActivity = new ComponentName(packageName,className);
                return processInfo;
            }
        }
        return null;
    }
}
