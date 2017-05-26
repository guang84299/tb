package com.qianqi.mylook.boost;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.util.SparseArray;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.utils.FileUtils;
import com.qianqi.mylook.utils.L;
import com.qianqi.mylook.utils.ReflectUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/3/24.
 */

public class VpnHelper extends BroadcastReceiver{

    private File dir = null;
    private List<String> vpnList = new ArrayList<>();

    public VpnHelper(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("mylook.action.vp_update");
        MainApplication.getInstance().registerReceiver(this, filter);
        initDir();
        updateVpnList();
    }

    private void initDir(){
        try {
            Context masterContext = MainApplication.getInstance().createPackageContext(MainApplication.CORE_PKGS[0], 0);
            dir = masterContext.getFilesDir();
        } catch (PackageManager.NameNotFoundException e) {
            L.d("vpn", e);
        }
    }

    public void onDestroy(){
        MainApplication.getInstance().unregisterReceiver(this);
    }

    private void updateVpnList(){
        L.d("updateVpnList");
        if(dir == null){
            initDir();
            if(dir == null)
                return;
        }
        File logFile = new File(dir,"vp");
        vpnList = FileUtils.readFile(logFile);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        updateVpnList();
    }

    public boolean isVpn(String packageName) {
        if(vpnList == null)
            return false;
        else{
            return vpnList.contains(packageName);
        }
    }
}
