package com.qianqi.mylook.boost;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.UserHandle;
import android.support.annotation.RequiresApi;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.utils.L;
import com.qianqi.mylook.utils.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/3/24.
 */

public class LocationHelper extends BroadcastReceiver{

    private AppOpsManager mAppOpsManager;
    private static int[] mHighPowerRequestAppOpArray = null;
    private HashMap<String,Long> activeRequests = new HashMap<>();
    private boolean init = false;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public LocationHelper(){
        try {
            Object op = ReflectUtils.getValue(AppOpsManager.class,"OP_MONITOR_HIGH_POWER_LOCATION");
            if(!(op instanceof Integer)){
                return;
            }
            mHighPowerRequestAppOpArray = new int[1];
            mHighPowerRequestAppOpArray[0] = (int)op;
            Object action = ReflectUtils.getValue(LocationManager.class,"HIGH_POWER_REQUEST_CHANGE_ACTION");
            if(!(action instanceof String)){
                return;
            }
            // Register to listen for changes in location settings.
            IntentFilter filter = new IntentFilter();
            filter.addAction((String) action);
//            filter.addAction(LocationManager.MODE_CHANGED_ACTION);
            MainApplication.getInstance().registerReceiver(this, filter);
            mAppOpsManager = (AppOpsManager) MainApplication.getInstance().getSystemService(Context.APP_OPS_SERVICE);
            updateActiveLocationRequests();
            init = true;
        } catch (Exception e) {
            L.d("location",e);
        }
    }

    public void onDestroy(){
        if(init){
            MainApplication.getInstance().unregisterReceiver(this);
        }
    }

    public boolean isLongTimeActive(String packageName){
        Long startTime = activeRequests.get(packageName);
        if(startTime == null)
            return false;
        long curTime = System.currentTimeMillis();
        return (curTime - startTime) > 60*1000;
    }

    private void updateActiveLocationRequests(){
        List<String> requests = getActiveLocationRequests();
        if(requests == null) {
            activeRequests.clear();
            return;
        }
        Iterator<String> ite = activeRequests.keySet().iterator();
        while (ite.hasNext()){
            String packageName = ite.next();
            boolean found = false;
            for(String s : requests){
                if(s.equals(packageName)){
                    found = true;
                    requests.remove(s);
                    break;
                }
            }
            if(!found){
                ite.remove();
                L.d("cancel location:"+packageName);
            }
        }
        long time = System.currentTimeMillis();
        for(String s : requests){
            activeRequests.put(s,time);
            L.d("request location:"+s);
        }
    }

    private List<String> getActiveLocationRequests() {
        if(mAppOpsManager == null || mHighPowerRequestAppOpArray == null)
            return null;
        try {
            Object packagesObj = ReflectUtils.callMethod(mAppOpsManager,"getPackagesForOps",
                    new Class[]{int[].class},new Object[]{mHighPowerRequestAppOpArray});
            if(packagesObj == null || !(packagesObj instanceof List))
                return null;
            List<String> res = new ArrayList<>(0);
            List packages = (List) packagesObj;
            final int numPackages = packages.size();
            for (int packageInd = 0; packageInd < numPackages; packageInd++) {
                Object packageOp = packages.get(packageInd);
                Object opEntriesObj = ReflectUtils.callMethod(packageOp,"getOps",new Class[0],new Object[0]);
                if(opEntriesObj == null || !(opEntriesObj instanceof List))
                    continue;
                List<Object> opEntries = (List<Object>) opEntriesObj;
                boolean use = false;
                final int numOps = opEntries.size();
                for (int opInd = 0; opInd < numOps; opInd++) {
                    Object opEntry = opEntries.get(opInd);
                    Object opObject = ReflectUtils.callMethod(opEntry,"getOp",new Class[0],new Object[0]);
                    Object isRunningObject = ReflectUtils.callMethod(opEntry,"isRunning",new Class[0],new Object[0]);
                    if(opObject != null && opObject instanceof Integer){
                        int op = (int) opObject;
                        if(op == mHighPowerRequestAppOpArray[0]){
                            if(isRunningObject != null && isRunningObject instanceof Boolean){
                                boolean isRunning = (boolean) isRunningObject;
                                if(isRunning){
                                    use = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if(use){
                    Object packageNameObj = ReflectUtils.callMethod(packageOp,"getPackageName",new Class[0],new Object[0]);
                    if(packageNameObj != null && packageNameObj instanceof String) {
                        String packageName = (String) packageNameObj;
                        if(!res.contains(packageName))
                            res.add(packageName);
                    }
                }
            }
            return res;
        } catch (Exception e) {
            L.d("location",e);
        }
        return null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        updateActiveLocationRequests();
    }
}
