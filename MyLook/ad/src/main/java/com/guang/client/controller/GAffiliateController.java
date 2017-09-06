package com.guang.client.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.guang.client.GCommon;
import com.guang.client.mode.GOffer;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.qinglu.ad.QLAdController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guang on 2017/8/17.
 */

public class GAffiliateController {

    private static GAffiliateController _instance;
    private final String GP_AFFI_OFFERS = "gp_affi_offers";
    private final String GP_PASS_AFFI_OFFERS = "gp_pass_affi_offers";
    private GOffer gpAffiOffer;
    private GAffiliateController(){}

    public static GAffiliateController getInstance()
    {
        if(_instance == null)
            _instance = new GAffiliateController();
        return _instance;
    }

    public void init()
    {
        String countryCode = GTools.getSharedPreferences().getString(GCommon.SHARED_KEY_CURR_COUNTRYCODE,null);
        GTools.httpGetRequest(GCommon.URI_GET_GP_AFFI_OFFERS+"?countryCode="+countryCode,this, "revGPAffiAdOffers", null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void revGPAffiAdOffers(Object ob, Object rev)
    {
        if(rev != null)
        {
            GLog.e("------------------", "revGPAffiAdOffers"+rev.toString());
            try {
                JSONArray offers = new JSONArray(rev.toString());

                String allapps = GTools.getLauncherAppsData().toString();
                List<String> clears = new ArrayList<String>();
                //添加展示标记
                for(int i=0;i<offers.length();i++)
                {
                    JSONObject obj = offers.getJSONObject(i);
                    String packageName = obj.getString("package_name");
                    //去除已经安装的
                    if(allapps.contains(packageName))
                    {
                        clears.add(packageName);
                    }
                    else
                    {
                        //去掉已经展示超过限制的
                        String pass = GTools.getSharedPreferences().getString(GP_PASS_AFFI_OFFERS,"");
                        if(pass != null && !"".equals(pass) && pass.contains(packageName))
                        {
                            clears.add(packageName);
                        }
                    }
                    obj.put("showNum",0);
                }
                while(clears.size()>0)
                {
                    String app = clears.get(0);

                    for(int i=0;i<offers.length();i++)
                    {
                        JSONObject obj = offers.getJSONObject(i);
                        String packageName = obj.getString("package_name");
                        if(app.equals(packageName))
                        {
                            offers.remove(i);
                            break;
                        }
                    }

                    clears.remove(0);
                }
                //按照优先级排序
                JSONArray arr = new JSONArray();

                while (offers.length()>0)
                {
                    arr.put(offers.getJSONObject(0));
                    offers.remove(0);
                }

                GTools.saveSharedData(GP_AFFI_OFFERS,arr.toString());
                GLog.e("------------------", "affi offers"+arr.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public boolean showGpBreak(long adPositionId,String appName)
    {
        gpAffiOffer = null;
        String s = GTools.getSharedPreferences().getString(GP_AFFI_OFFERS,"");
        if("".equals(s))
            return false;

        try {
            JSONArray offers = new JSONArray(s);
            JSONObject o = null;
            int showNum = 0;
            int maxNum = GUserController.getMedia().getConfig(adPositionId).getAdShowNum();
            for(int i=0;i<offers.length();i++)
            {
                JSONObject obj = offers.getJSONObject(i);
                showNum = obj.getInt("showNum");
                if(showNum < maxNum)
                {
                    o = obj;
                }
            }
            if(o != null)
            {
                o.put("showNum",showNum+1);
                GTools.saveSharedData(GP_AFFI_OFFERS,offers.toString());

                long id = o.getInt("offer_id");
                String packageName = o.getString("package_name");
                String name = o.getString("offer_name");
                String gpUrl = o.getString("preview_link");
                String trackUrl = o.getString("tracking_link");

                if(showNum+1 >= maxNum)
                {
                    String pass = GTools.getSharedPreferences().getString(GP_PASS_AFFI_OFFERS,"");
                    GTools.saveSharedData(GP_PASS_AFFI_OFFERS,pass+","+packageName);
                }

                String gaid = GMIController.getInstance().getGoogleAid();
                TelephonyManager tm = GTools.getTelephonyManager();
                String device_id = tm.getDeviceId();

                trackUrl = trackUrl.replace("{gaid}",gaid);
                trackUrl = trackUrl.replace("{device_id}",device_id);

                gpAffiOffer = new GOffer(id+"",packageName,name,gpUrl,trackUrl);

                Context context = QLAdController.getInstance().getContext();
                Intent intent = new Intent();
                intent.putExtra("type","affi_gp");
                intent.setAction(GCommon.ACTION_QEW_APP_GP_BREAK);
                context.sendBroadcast(intent);
                GLog.e("--------------", "affi_gp break success");

                return true;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }


    public GOffer getGpOffer() {
        return gpAffiOffer;
    }
}
