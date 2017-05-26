package com.android.system.manager.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFocusInfo;
import android.media.audiopolicy.IAudioPolicyCallback;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.net.VpnInfo;
import com.android.system.manager.utils.FileUtils;
import com.android.system.manager.utils.L;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/28.
 */

public class VpnHelper {

    private List<String> vpnList;
    private BroadcastReceiver connReceiver;
    private Method getAllVpnInfoMethod;
    private Object service;

    public VpnHelper(){
        followVpn();
    }

    private void followVpn(){
        L.d("vpn:follow");
        try {
            Class serviceManagerClazz = Class.forName("android.os.ServiceManager");
            Method getServiceMethod = serviceManagerClazz.getMethod("getService", String.class);
            service = getServiceMethod.invoke(null, Context.CONNECTIVITY_SERVICE);
            if(service == null)
                return;
            L.d(service.getClass().getName());
            Method[] methods = service.getClass().getDeclaredMethods();
            getAllVpnInfoMethod = null;
            for(Method m:methods){
                //L.d(m.getName());
                if(m.getName().equals("getAllVpnInfo")){
                    getAllVpnInfoMethod = m;
                    getAllVpnInfoMethod.setAccessible(true);
                    break;
                }
            }
            if(getAllVpnInfoMethod == null){
                L.d("getAllVpnInfoMethod=null");
            }
            else{
                initCB();
                Log.d("mylooklog","v=true");
            }
        } catch (Exception e) {
            L.d("vpn",e);
        }
    }

    private void updateVpn(){
        L.d("updateVpn");
        if(service == null || getAllVpnInfoMethod == null)
            return;
        Object ret = null;
        try {
            ret = getAllVpnInfoMethod.invoke(service);
        } catch (Exception e) {
            L.d("vpn", e);
        }
        if(ret == null || !(ret instanceof VpnInfo[]))
            return;
        VpnInfo[] infoList = (VpnInfo[]) ret;
        List<String> newList = new ArrayList<>(infoList.length);
        PackageManager pm = SystemProcess.ins().getContext().getPackageManager();
        for(VpnInfo info:infoList){
            int uid = info.ownerUid;
            String[] packageNames = pm.getPackagesForUid(uid);
            if(packageNames == null)
                continue;
            for(String packageName:packageNames) {
                if (!TextUtils.isEmpty(packageName))
                    newList.add(packageName);
            }
        }
        boolean changed = false;
        if(vpnList != null){
            for(String s:newList){
                boolean remove = vpnList.remove(s);
                if(!remove) {
                    changed = true;
                    break;
                }
            }
            if(vpnList.size() > 0)
                changed = true;
        }
        else
            changed = true;
        if(changed){
            vpnList = newList;
            File dir = SystemProcess.ins().getContext().getFilesDir();
            File logFile = new File(dir,"vp");
            logFile.setReadable(true, false);
            FileUtils.writeFile(logFile, "", false);
            for(String s:vpnList){
                //L.d("vpn:write="+ s);
                FileUtils.writeFile(logFile, s+"\n", true);
            }
            Intent intent = new Intent();
            intent.setAction("mylook.action.vp_update");
            SystemProcess.ins().getContext().sendBroadcast(intent);
        }
    }

    private void initCB(){
        if(connReceiver == null){
            Context context = SystemProcess.ins().getContext();
            connReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                        updateVpn();
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(connReceiver,filter);
        }
    }

    public void onDestroy() {
        if(connReceiver != null){
            Context context = SystemProcess.ins().getContext();
            context.unregisterReceiver(connReceiver);
            connReceiver = null;
        }
    }
}
