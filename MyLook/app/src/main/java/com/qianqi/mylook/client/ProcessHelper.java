package com.qianqi.mylook.client;

import android.app.ActivityManager;

import com.android.system.manager.plugin.master.MS;
import com.qianqi.mylook.utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/4.
 */

public class ProcessHelper {

    private MS masterServer;

    public ProcessHelper() {
    }

    public void setMasterServer(MS server){
        this.masterServer = server;
//        if(this.masterServer != null){
//            boolean success = this.masterServer.monitorAppProcess(this);
//            L.d("monitor process:"+success);
//            EventBus.getDefault().post(new BusTag(BusTag.TAG_REQUEST_PROCESS_UPDATE,getProcessList()));
//        }
    }

//    public void onChanged(List<ActivityManager.RunningAppProcessInfo> list) {
//        postProcess(list);
//    }

//    private void postProcess(List<ActivityManager.RunningAppProcessInfo> list){
//        if(this.masterServer == null)
//            return;
////        L.d("post process");
//        List<ActivityManager.RunningAppProcessInfo> res = parseProcessList(list);
//        EventBus.getDefault().post(new BusTag(BusTag.TAG_REQUEST_PROCESS_UPDATE,res));
//    }

    public List<String> getProcessList(){
        if(this.masterServer == null) {
            L.d("master=null");
            return null;
        }
        return this.masterServer.d();
    }

    public String getTopTask(){
        if(this.masterServer == null)
            return null;
        String top = this.masterServer.f();
        return top != null?top:"";
    }

    private List<ActivityManager.RunningAppProcessInfo> parseProcessList(List list){
//        if(list != null){
//            List<ActivityManager.RunningAppProcessInfo> processInfoList = new ArrayList<>(list.size());
//            for(Object item:list){
//                LinkedTreeMap map = (LinkedTreeMap) item;
//                ActivityManager.RunningAppProcessInfo processInfo = new ActivityManager.RunningAppProcessInfo();
//                Set set = map.entrySet();
//                for(Object entry:set){
//                    LinkedTreeMap.Entry node = (LinkedTreeMap.Entry) entry;
//                    String key = (String) node.getKey();
//                    if(key.equals("importance")){
//                        Double d = (Double) node.getValue();
//                        processInfo.importance = d.intValue();
//                    }
//                    else if(key.equals("pid")){
//                        Double d = (Double) node.getValue();
//                        processInfo.pid = d.intValue();
//                    }
//                    else if(key.equals("pkgList")){
//                        ArrayList pkgList = (ArrayList) node.getValue();
//                        String[] array = new String[pkgList.size()];
//                        for(int i = 0; i < pkgList.size();i++){
//                            array[i] = pkgList.get(i).toString();
//                        }
//                        processInfo.pkgList = array;
//                    }
//                }
//                processInfoList.add(processInfo);
//            }
//            return processInfoList;
//        }
        return new ArrayList<ActivityManager.RunningAppProcessInfo>(0);
    }

    private ActivityManager.RunningTaskInfo parseTaskList(List list){
//        if(list != null && list.size() > 0){
//            Object item = list.get(0);
//            LinkedTreeMap map = (LinkedTreeMap) item;
//            ActivityManager.RunningTaskInfo processInfo = new ActivityManager.RunningTaskInfo();
//            String packageName = null;
//            String className = null;
//            Set set = map.entrySet();
//            for(Object entry:set){
//                LinkedTreeMap.Entry node = (LinkedTreeMap.Entry) entry;
//                String key = (String) node.getKey();
//                if(key.equals("topActivity")){
//                    LinkedTreeMap topActivity = (LinkedTreeMap) node.getValue();
//                    set = topActivity.entrySet();
//                    for(Object tmp:set){
//                        node = (LinkedTreeMap.Entry) tmp;
//                        key = (String) node.getKey();
//                        if(key.equals("mClass")){
//                            className = (String) node.getValue();
//                        }
//                        else if(key.equals("mPackage")){
//                            packageName = (String) node.getValue();
//                        }
//                    }
//                    break;
//                }
//            }
//            if(!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(className)){
//                processInfo.topActivity = new ComponentName(packageName,className);
//                return processInfo;
//            }
//        }
        return null;
    }
}
