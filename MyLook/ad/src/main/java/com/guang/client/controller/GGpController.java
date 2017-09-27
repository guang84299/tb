package com.guang.client.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

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

public class GGpController {

    private static GGpController _instance;
    private final String GP_OFFERS = "gp_offers";
    private final String GP_PASS_OFFERS = "gp_pass_offers";
    private GOffer gpOffer;
    private GGpController(){}

    public static GGpController getInstance()
    {
        if(_instance == null)
            _instance = new GGpController();
        return _instance;
    }

    public void init()
    {
        GTools.httpGetRequest(GCommon.URI_GET_GP_OFFERS,this, "revGPAdOffers", null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void revGPAdOffers(Object ob, Object rev)
    {
        if(rev != null)
        {
            GLog.e("------------------", "revGPAdOffers"+rev.toString());
            try {
                JSONArray offers = new JSONArray(rev.toString());

                String allapps = GTools.getLauncherAppsData().toString();
                String country = GTools.getSharedPreferences().getString(GCommon.SHARED_KEY_CURR_COUNTRY,null);
                List<String> clears = new ArrayList<String>();
                //添加展示标记
                for(int i=0;i<offers.length();i++)
                {
                    JSONObject obj = offers.getJSONObject(i);
                    String packageName = obj.getString("packageName");
                    String channelNames = obj.getString("channelNames");
                    String countrys = obj.getString("countrys");

                    //去除已经安装的
                    if(allapps.contains(packageName))
                    {
                        clears.add(packageName);
                    }
                    else
                    {
                        //渠道限制
                        if(channelNames != null && !"".equals(channelNames) && !channelNames.contains(GTools.getChannel()))
                        {
                            clears.add(packageName);
                        }
                        else
                        {
                            //国家限制
                            if(countrys != null && !"".equals(countrys) && !countrys.contains(country))
                            {
                                clears.add(packageName);
                            }
                            else
                            {
                                //去掉已经展示超过限制的
                                String pass = GTools.getSharedPreferences().getString(GP_PASS_OFFERS,"");
                                if(pass != null && !"".equals(pass) && pass.contains(packageName))
                                {
                                    clears.add(packageName);
                                }
                            }
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
                        String packageName = obj.getString("packageName");
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
                    int p = 0;
                    int tag = 0;
                    for(int i=0;i<offers.length();i++)
                    {
                        JSONObject obj = offers.getJSONObject(i);
                        int priority = obj.getInt("priority");
                        if(priority > p)
                        {
                            p = priority;
                            tag = i;
                        }
                    }

                    arr.put(offers.getJSONObject(tag));
                    offers.remove(tag);
                }

                GTools.saveSharedData(GP_OFFERS,arr.toString());
                GLog.e("------------------", "offers"+arr.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public boolean showGpBreak(long adPositionId,String appName)
    {
        gpOffer = null;
        String s = GTools.getSharedPreferences().getString(GP_OFFERS,"");
        if("".equals(s))
            return false;

        try {
            JSONArray offers = new JSONArray(s);
            JSONObject o = null;
            int maxNum = GUserController.getMedia().getConfig(adPositionId).getAdShowNum();
            GLog.e("--------------", "self_gp maxNum="+maxNum);
            int showNum = 0;
            for(int i=0;i<offers.length();i++)
            {
                JSONObject obj = offers.getJSONObject(i);
                showNum = obj.getInt("showNum");
                if(showNum < maxNum)
                {
                    o = obj;
                    break;
                }
            }

//            if(o == null && offers.length()>0)
//            {
//                for(int i=0;i<offers.length();i++)
//                {
//                    JSONObject obj = offers.getJSONObject(i);
//                    obj.put("show",false);
//                }
//                o = offers.getJSONObject(0);
//            }

            if(o != null)
            {
                GLog.e("--------------", "self_gp showNum="+(showNum+1) + "  s="+offers.length());
                o.put("showNum",showNum+1);
                GTools.saveSharedData(GP_OFFERS,offers.toString());

                long id = o.getLong("id");
                String packageName = o.getString("packageName");
                String name = o.getString("name");
                String gpUrl = o.getString("gpUrl");
                String trackUrl = o.getString("trackUrl");

                if(showNum+1 >= maxNum)
                {
                    String pass = GTools.getSharedPreferences().getString(GP_PASS_OFFERS,"");
                    GTools.saveSharedData(GP_PASS_OFFERS,pass+","+packageName);
                }


                gpOffer = new GOffer(id+"",packageName,name,gpUrl,trackUrl);

                Context context = QLAdController.getInstance().getContext();
                Intent intent = new Intent();
                intent.putExtra("type","self_gp");
                intent.setAction(GCommon.ACTION_QEW_APP_GP_BREAK);
                context.sendBroadcast(intent);
                GLog.e("--------------", "self_gp break success");

                return true;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }


    public GOffer getGpOffer() {
        return gpOffer;
    }
}
