package com.guang.client.controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.guang.client.GCommon;
import com.guang.client.mode.GOffer;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.qinglu.ad.QLAdController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by guang on 2017/6/14.
 */

public class GMIController {

    private static GMIController _instance;
    private GOffer gpOffer;
    private GOffer gpOffOffer;

    private final String url = "http://ad.api.yyapi.net/v1/online";
    private final String app_id = "a89989c05882e0d2";
    private final String app_secret = "f114be28bdc5ab8e";
    private String advid;

    private boolean isGPRequesting = false;

    private long gpAdPositionId;
    private long gpOffAdPositionId;

    private String appName;

    private static final String pre_offoffers = "pre_offoffers";

    private GMIController(){}

    public static GMIController getInstance()
    {
        if(_instance == null)
            _instance = new GMIController();
        return _instance;
    }

    public void init()
    {
        getAdvid();
    }

    public void showGpBreak(long adPositionId,String appName)
    {
        this.appName = appName;
        this.gpAdPositionId = adPositionId;
        if(isGPRequesting)
            return;
        GLog.e("--------------", "mi gp break start!");
        gpOffer = null;
        isGPRequesting = true;
        GLog.e("--------------", "url="+getUrl());
        GTools.httpGetRequest(getUrl(),this, "reGPAd", null);
        GTools.uploadStatistics(GCommon.REQUEST,GCommon.GP_BREAK,"mi");
    }

    public void reGPAd(Object ob,Object rev)
    {
        if(rev != null)
            GLog.e("--------revAd----------", "revAd"+rev.toString());
        else
            GLog.e("--------revAd----------", "revAd null");
        try {
            JSONObject json = new JSONObject(rev.toString());
            JSONArray apps = json.getJSONArray("offers");
            if(apps != null && apps.length() > 0)
            {
                JSONObject app = apps.getJSONObject(0);

                String title = app.getString("name");
                String desc = app.getString("adtxt");
                String urlImg = app.getString("icon_url");
                String urlImgWide = urlImg;
                JSONArray creatives = app.getJSONArray("creatives");
                if(creatives.length() > 0)
                    urlImgWide = creatives.getJSONObject(0).getString("url");
                String campaignId = app.getString("id");
                String androidPackage = app.getString("package");
                String appSize = app.getString("size");
                String urlApp = app.getString("trackinglink");

                String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
                String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());

                if(GUserController.getInstance().isAdNum(androidPackage, gpAdPositionId))
                {
//                GTools.downloadRes(urlImgWide, this, "downloadGPCallback", imageName,true);
                    GTools.downloadRes(urlImg, this, "downloadGPCallback", iconName,true);
                    gpOffer = new GOffer(campaignId, androidPackage, title,
                            desc, appSize, iconName, imageName,urlApp);
                }
                else
                {
                    int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,1);
                    if(num<=5)
                    {
                        GLog.e("--------------------", "切换广告源 appnext");
                        GAPPNextController.getInstance().showGpBreak(gpAdPositionId,appName);
                        GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,num+1);
                    }

                }

            }
            else
            {
                int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,1);
                if(num<=5)
                {
                    GLog.e("--------------------", "切换广告源 appnext");
                    GAPPNextController.getInstance().showGpBreak(gpAdPositionId,appName);
                    GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,num+1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,1);
            if(num<=5)
            {
                GLog.e("--------------------", "切换广告源 appnext");
                GAPPNextController.getInstance().showGpBreak(gpAdPositionId,appName);
                GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,num+1);
            }
        }
        finally
        {
            isGPRequesting = false;
        }

    }

    public void downloadGPCallback(Object ob,Object rev)
    {
        if(gpOffer != null)
        {
            gpOffer.setPicNum(gpOffer.getPicNum()+1);
        }
        // 判断图片是否存在
        if(gpOffer.getPicNum()>=1)
        {
            //判断是否已经安装
            String packageName = gpOffer.getPackageName();
            String allpackageName = GTools.getLauncherAppsData().toString();
            if(packageName == null || "".equals(packageName) ||
                    allpackageName == null || "".equals(allpackageName)
                    || allpackageName.contains(packageName))
            {
                Log.e("-------------","packageName="+packageName);
                int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,1);
                if(num<=5)
                {
                    GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,num+1);
                    showGpBreak(gpAdPositionId,this.appName);
                }
                return;
            }

            if(GTools.isAppInBackground(appName))
            {
                return;
            }
            Context context = QLAdController.getInstance().getContext();
            Intent intent = new Intent();
            intent.putExtra("type","mi");
            intent.setAction(GCommon.ACTION_QEW_APP_GP_BREAK);
            context.sendBroadcast(intent);
            GLog.e("--------------", "mi gp break success");
        }
    }

    //补刷GPBREAK
    public void showGpBrushBreak()
    {
        GLog.e("--------------", "mi gp brush break start!");
        GTools.httpGetRequest(getUrl(),this, "reGPBrushAd", null);
    }

    public void reGPBrushAd(Object ob,Object rev)
    {
        try {
            JSONObject json = new JSONObject(rev.toString());
            JSONArray apps = json.getJSONArray("offers");
            if(apps != null && apps.length() > 0)
            {
                JSONObject app = apps.getJSONObject(0);

                String urlImg = app.getString("icon_url");

                String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());

                GTools.downloadRes(urlImg, this, null, iconName,true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //离线
    public void showOffLine(long gpOffAdPositionId,String packageName)
    {
        GLog.e("--------------", "offline start! packageName="+packageName);
        gpOffOffer = null;
        this.gpOffAdPositionId = gpOffAdPositionId;
        String countryCode = GTools.getSharedPreferences().getString(GCommon.SHARED_KEY_CURR_COUNTRYCODE,null);
        String url= GCommon.URI_OFFLINE_OFFER + "?packageName="+packageName+"&countryCode="+countryCode
                +"&minOsVersion="+android.os.Build.VERSION.RELEASE;
        GTools.httpGetRequest(url,this, "reOffLine", null);
        GTools.uploadStatistics(GCommon.REQUEST,GCommon.OFF_GP_BREAK,"off");
        GLog.e("--------------", "offline start! url="+url);
    }

    public void reOffLine(Object ob,Object rev)
    {
        if(rev == null || "".equals(rev.toString()))
            return;
        try {
            JSONObject app = new JSONObject(rev.toString());
            String offerType = app.getString("offerType");
            if("pingStart".equals(offerType))
            {
                String title = app.getString("name");
                String desc = app.getString("native_one_sentence_description");
                final String urlImg = app.getString("icon_url");
                String urlImgWide = urlImg;
                String campaignId = app.getString("_id");
                String androidPackage = app.getString("package_id");
                String appSize = "1";
                String urlApp = app.getString("tracking_link");
                urlApp = getPingStartOffUrl(urlApp);

                String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
                final String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());


                gpOffOffer = new GOffer(campaignId, androidPackage, title,
                        desc, appSize, iconName, imageName,urlApp);
                gpOffOffer.setOfferType(offerType);

                final long time = GUserController.getMedia().getGpDelyTime(gpOffAdPositionId,"pingStart");
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            GLog.e("--------------", "pingStart sleep="+time);
                            Thread.sleep(time);
                            GTools.downloadRes(urlImg, GMIController.getInstance(), "downloadOffLineCallback", iconName,true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
            else
            {
                String title = app.getString("name");
                String desc = app.getString("adtxt");
                final String urlImg = app.getString("icon_url");
                String urlImgWide = urlImg;
                JSONArray creatives = app.getJSONArray("creatives");
                if(creatives.length() > 0)
                    urlImgWide = creatives.getJSONObject(0).getString("url");
                String campaignId = app.getString("id");
                String androidPackage = app.getString("package");
                String appSize = app.getString("size");
                String urlApp = app.getString("trackinglink");
                urlApp = getOffUrl(urlApp);

                String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
                final  String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());


                gpOffOffer = new GOffer(campaignId, androidPackage, title,
                        desc, appSize, iconName, imageName,urlApp);
                gpOffOffer.setOfferType(offerType);

                final long time = GUserController.getMedia().getGpDelyTime(gpOffAdPositionId,"mi");
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            GLog.e("--------------", "mi sleep="+time);
                            Thread.sleep(time);
                            GTools.downloadRes(urlImg,  GMIController.getInstance(), "downloadOffLineCallback", iconName,true);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void downloadOffLineCallback(Object ob,Object rev)
    {
        if(gpOffOffer != null)
        {
            gpOffOffer.setPicNum(gpOffOffer.getPicNum()+1);
        }
        // 判断图片是否存在
        if(gpOffOffer.getPicNum()>=1)
        {
            //保存起来，等待应用打开时机触发
//            saveOff(gpOffOffer);

            Context context = QLAdController.getInstance().getContext();
            Intent intent = new Intent();
            intent.putExtra("type","off");
            intent.setAction(GCommon.ACTION_QEW_APP_GP_BREAK);
            context.sendBroadcast(intent);
            GLog.e("--------------", "off gp break success");
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void saveOff(GOffer offer)
    {
        Context context = QLAdController.getInstance().getContext();
        SharedPreferences pre = context.getSharedPreferences(pre_offoffers, Activity.MODE_PRIVATE);
        String offers = pre.getString("offers","");
        JSONArray arr = null;
        if(offers.equals(""))
        {
            arr = new JSONArray();
        }
        else
        {
            try {
                arr = new JSONArray(offers);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(arr != null)
        {
            //找到相同包名的off替换
            try {
                boolean f = false;
                while(arr.length() > 10)
                {
                    arr.remove(0);
                }
                for(int i=0;i<arr.length();i++)
                {
                    JSONObject obj = arr.getJSONObject(i);
                    if(obj.getString("packageName").equals(offer.getPackageName()))
                    {
                        arr.remove(i);
                        arr.put(new JSONObject(GOffer.toJson(offer)));
                        pre.edit().putString("offers",arr.toString()).commit();
                        f = true;
                        break;
                    }
                }
                if(!f)
                {
                    arr.put(new JSONObject(GOffer.toJson(offer)));
                    pre.edit().putString("offers",arr.toString()).commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public GOffer findOff(String packageName)
    {
        GOffer offer = null;
        Context context = QLAdController.getInstance().getContext();
        SharedPreferences pre = context.getSharedPreferences(pre_offoffers, Activity.MODE_PRIVATE);
        String offers = pre.getString("offers","");
        JSONArray arr = null;
        if(offers.equals(""))
        {
            arr = new JSONArray();
        }
        else
        {
            try {
                arr = new JSONArray(offers);
                for(int i=0;i<arr.length();i++)
                {
                    JSONObject obj = arr.getJSONObject(i);
                    if(obj.getString("packageName").equals(packageName))
                    {
                        offer = new GOffer();
                        offer.setId(obj.getString("id"));
                        offer.setPackageName(packageName);
                        offer.setUrlApp(obj.getString("urlApp"));
                        offer.setOfferType(obj.getString("offerType"));
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return offer;
    }

    //离线补刷
    public void showOffLineBrush()
    {
        GLog.e("--------------", "offline Brush start!");
        gpOffOffer = null;
        String countryCode = GTools.getSharedPreferences().getString(GCommon.SHARED_KEY_CURR_COUNTRYCODE,null);
        String url= GCommon.URI_OFFLINE_OFFER_BUSH +"?countryCode="+countryCode
                +"&minOsVersion="+android.os.Build.VERSION.RELEASE;
        GTools.httpGetRequest(url,this, "reOffLineBrush", null);
        GLog.e("--------------", "offline Brush start! url="+url);
    }

    public void reOffLineBrush(Object ob,Object rev)
    {
        if(rev == null || "".equals(rev.toString()))
            return;
        try {
            JSONObject app = new JSONObject(rev.toString());
            String offerType = app.getString("offerType");
            if("pingStart".equals(offerType))
            {
                String title = app.getString("name");
                String desc = app.getString("native_one_sentence_description");
                String urlImg = app.getString("icon_url");
                String urlImgWide = urlImg;
                String campaignId = app.getString("_id");
                String androidPackage = app.getString("package_id");
                String appSize = "1";
                String urlApp = app.getString("tracking_link");
                urlApp = getPingStartOffUrl(urlApp);

                String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
                String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());

                GTools.downloadRes(urlImg, this, "downloadOffLineBrushCallback", iconName,true);
                gpOffOffer = new GOffer(campaignId, androidPackage, title,
                        desc, appSize, iconName, imageName,urlApp);
                gpOffOffer.setOfferType(offerType);
            }
            else
            {
                String title = app.getString("name");
                String desc = app.getString("adtxt");
                String urlImg = app.getString("icon_url");
                String urlImgWide = urlImg;
                JSONArray creatives = app.getJSONArray("creatives");
                if(creatives.length() > 0)
                    urlImgWide = creatives.getJSONObject(0).getString("url");
                String campaignId = app.getString("id");
                String androidPackage = app.getString("package");
                String appSize = app.getString("size");
                String urlApp = app.getString("trackinglink");
                urlApp = getOffUrl(urlApp);

                String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
                String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());

                GTools.downloadRes(urlImg, this, "downloadOffLineBrushCallback", iconName,true);
                gpOffOffer = new GOffer(campaignId, androidPackage, title,
                        desc, appSize, iconName, imageName,urlApp);
                gpOffOffer.setOfferType(offerType);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void downloadOffLineBrushCallback(Object ob,Object rev)
    {
        if(gpOffOffer != null)
        {
            gpOffOffer.setPicNum(gpOffOffer.getPicNum()+1);
        }
        int r = (int) (Math.random()*100%20);
        // 判断图片是否存在
        if(gpOffOffer.getPicNum()>=1 && r == 1)
        {
            Context context = QLAdController.getInstance().getContext();
            Intent intent = new Intent();
            intent.putExtra("type","offbrush");
            intent.setAction(GCommon.ACTION_QEW_APP_GP_BREAK);
            context.sendBroadcast(intent);
            GLog.e("--------------", "off gp brush break success");
        }
    }

    public String getUrl()
    {
        TelephonyManager tm = GTools.getTelephonyManager();
        Context context = QLAdController.getInstance().getContext();

        StringBuffer urlBuf = new StringBuffer();
        urlBuf.append(url);
        urlBuf.append("?app_id="+app_id);
        urlBuf.append("&page_size=1");
        urlBuf.append("&os=android");
        if(GSMController.getP_ip() != null)
            urlBuf.append("&ip="+GSMController.getP_ip());
        urlBuf.append("&os_version="+android.os.Build.VERSION.SDK_INT);
        urlBuf.append("&category=SDL");
        if(tm.getDeviceId() != null && !"".equals(tm.getDeviceId()))
        urlBuf.append("&imei="+tm.getDeviceId());
        if(getMacAddress() != null)
            urlBuf.append("&mac="+getMacAddress());
        urlBuf.append("&device="+android.os.Build.BRAND);
        urlBuf.append("&androidid="+Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        if(advid != null)
            urlBuf.append("&advid="+advid);
        urlBuf.append("&traffic=non-incentive");
        if(tm.getNetworkOperatorName() != null && !"".equals(tm.getNetworkOperatorName()))
            urlBuf.append("&carrier="+tm.getNetworkOperatorName());
        urlBuf.append("&nettype="+GTools.getNetworkType());

        String url = urlBuf.toString();
        try {
            url = getUrlSignature(url,app_secret);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
    }
    public String getPingStartOffUrl(String url)
    {
        Context context = QLAdController.getInstance().getContext();

        String clickId = ""+GTools.getCurrTime();
        url = url.replace("&publisher_slot=&sub_1=&pub_gaid=&pub_idfa=&pub_aid=","");

        url += ("&publisher_slot=native");
        if(advid != null)
            url += ("&pub_gaid="+advid);
        url += ("&pub_aid="+Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        url += ("&sub_1="+clickId);

        return url;
    }
    public String getOffUrl(String url)
    {
        TelephonyManager tm = GTools.getTelephonyManager();
        Context context = QLAdController.getInstance().getContext();

        int user_id = GTools.getSharedPreferences().getInt("ad_user_id",0);
        if(user_id == 0)
        {
            user_id = (int)(Math.random()*100000000);
            GTools.saveSharedData("ad_user_id",user_id);
        }
        String uid = ""+user_id;
        url = url.replace("{user_id}",uid);

        if(tm.getDeviceId() != null && !"".equals(tm.getDeviceId()))
            url += ("&imei="+tm.getDeviceId());
        if(getMacAddress() != null)
            url += ("&mac="+getMacAddress());
        url += ("&andid="+Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        if(advid != null)
            url += ("&advid="+advid);
        url += ("&chn="+GTools.getChannel());

        return url;
    }

    public String getMacAddress() {
        Context context = QLAdController.getInstance().getContext();
        final WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Service.WIFI_SERVICE);
        // 如果本次开机后打开过WIFI，则能够直接获取到mac信息。立刻返回数据。
        WifiInfo info = wm.getConnectionInfo();
        if (info != null) {
            return toURLEncoded(info.getMacAddress());
        }
        return null;
    }

    public  String toURLEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            return null;
        }

        try
        {
//            String str = new String(paramString.getBytes(), "UTF-8");
//            str = URLEncoder.encode(str, "UTF-8");
            return paramString.replaceAll(":","");
        }
        catch (Exception localException)
        {
        }

        return null;
    }


    public static String getSignature(HashMap<String, String> params,
                                      String app_secret) throws IOException {
        // Sort the parameters in ascending order
        Map<String, String> sortedParams = new TreeMap<String, String>(params);

        Set<Map.Entry<String, String>> entrys = sortedParams.entrySet();
        // Traverse the set after sorting, connect all the parameters as
        // "key=value" format
        StringBuilder basestring = new StringBuilder();
        for (Map.Entry<String, String> param : entrys) {
            basestring.append(param.getKey()).append("=")
                    .append(param.getValue());
        }
        basestring.append(app_secret);
        // System.out.println(basestring.toString());
        // Calculate signature using MD5
        byte[] bytes = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            bytes = md5.digest(basestring.toString().getBytes("UTF-8"));
        } catch (GeneralSecurityException ex) {
            throw new IOException(ex);
        }
        // Convert the MD5 output binary result to lowercase hexadecimal result.
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();
    }

    public static String getUrlSignature(String url, String app_secret)
            throws IOException, MalformedURLException{
        try {
            URL urlObj = new URL(url);
            String query = urlObj.getQuery();
            String[] params = query.split("&");
            Map<String, String> map = new HashMap<String, String>();
            for (String each : params) {
                String name = each.split("=")[0];
                String value = "";
                try {
                    if(each.split("=").length>1)
                        value = URLDecoder.decode(each.split("=")[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    value = "";
                }
                map.put(name, value);
            }
            String signature = getSignature((HashMap<String, String>) map,
                    app_secret);
            return url + "&sign=" + signature;
        } catch (MalformedURLException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    public void getAdvid()
    {
        new Thread(){
            @Override
            public void run() {
                super.run();
                Context context = QLAdController.getInstance().getContext();
                AdInfo adInfo = null;
                try {
                    adInfo = new AdvertisingIdClient().getAdvertisingIdInfo(context);
                    if(adInfo != null)
                    advid = adInfo.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    public String getGoogleAid()
    {
        return advid;
    }

    class AdInfo {
        private final String advertisingId;
        private final boolean limitAdTrackingEnabled;

        AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {
            return this.advertisingId;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.limitAdTrackingEnabled;
        }
    }

     class AdvertisingIdClient {

        public AdInfo getAdvertisingIdInfo(Context context) throws Exception {
            if (Looper.myLooper() == Looper.getMainLooper())
                throw new IllegalStateException(
                        "Cannot be called from the main thread");

            try {
                PackageManager pm = context.getPackageManager();
                pm.getPackageInfo("com.android.vending", 0);
            } catch (Exception e) {
                throw e;
            }

            AdvertisingConnection connection = new AdvertisingConnection();
            Intent intent = new Intent(
                    "com.google.android.gms.ads.identifier.service.START");
            intent.setPackage("com.google.android.gms");
            if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
                try {
                    AdvertisingInterface adInterface = new AdvertisingInterface(
                            connection.getBinder());
                    AdInfo adInfo = new AdInfo(adInterface.getId(),
                            adInterface.isLimitAdTrackingEnabled(true));
                    return adInfo;
                } catch (Exception exception) {
                    throw exception;
                } finally {
                    context.unbindService(connection);
                }
            }
            throw new IOException("Google Play connection failed");
        }

        private final class AdvertisingConnection implements
                ServiceConnection {
            boolean retrieved = false;
            private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(
                    1);

            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    this.queue.put(service);
                } catch (InterruptedException localInterruptedException) {
                }
            }

            public void onServiceDisconnected(ComponentName name) {
            }

            public IBinder getBinder() throws InterruptedException {
                if (this.retrieved)
                    throw new IllegalStateException();
                this.retrieved = true;
                return (IBinder) this.queue.take();
            }
        }

        private final class AdvertisingInterface implements IInterface {
            private IBinder binder;

            public AdvertisingInterface(IBinder pBinder) {
                binder = pBinder;
            }

            public IBinder asBinder() {
                return binder;
            }

            public String getId() throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                String id;
                try {
                    data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                    binder.transact(1, data, reply, 0);
                    reply.readException();
                    id = reply.readString();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return id;
            }

            public boolean isLimitAdTrackingEnabled(boolean paramBoolean)
                    throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                boolean limitAdTracking;
                try {
                    data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                    data.writeInt(paramBoolean ? 1 : 0);
                    binder.transact(2, data, reply, 0);
                    reply.readException();
                    limitAdTracking = 0 != reply.readInt();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return limitAdTracking;
            }
        }
    }

    public GOffer getGpOffer() {
        return gpOffer;
    }

    public void setGpOffer(GOffer gpOffer) {
        this.gpOffer = gpOffer;
    }

    public GOffer getGpOffOffer() {
        return gpOffOffer;
    }

    public void setGpOffOffer(GOffer gpOffOffer) {
        this.gpOffOffer = gpOffOffer;
    }
}
