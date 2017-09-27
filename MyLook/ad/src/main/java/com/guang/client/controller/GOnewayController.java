package com.guang.client.controller;




import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.guang.client.GCommon;
import com.guang.client.mode.GSMOffer;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.qinglu.ad.QLAdController;
import com.qinglu.ad.QLBrowserSpotActivity;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;


public class GOnewayController {

	private static GOnewayController _instance = null;
	private GSMOffer spotOffer;
	private final String token = "9252ce810d014be7a521f90507e384f8";
	private final String url = "http://api.oneway.mobi/adserver/v1/offers/flow";
	
	private boolean isSpotRequesting = false;
	
	private String browserName;
	private long spotAdPositionId;
	
	private long flow = 0;//流量
	
	private GOnewayController()
	{
	}
	
	public static GOnewayController getInstance()
	{
		if(_instance == null)
			_instance = new GOnewayController();
		
		return _instance;
	}
	
	//显示应用插屏
	public void showSpot(long adPositionId,String browserName)
	{
		this.browserName = browserName;
		this.spotAdPositionId = adPositionId;
		if(isSpotRequesting)
			return;
		GLog.e("--------------", "browser spot start!");
		spotOffer = null;
		isSpotRequesting = true;
		GLog.e("--------------", "getUrl()="+getUrl());
		GTools.httpGetRequest(getUrl(),this, "revSpotAd", null);
		GTools.uploadStatistics(GCommon.REQUEST,GCommon.BROWSER_SPOT,"OneWay");
	}
	public void revSpotAd(Object ob,Object rev)
	{
		try {
			JSONObject json = new JSONObject(rev.toString());
			if("200".equals(json.getString("status")))
			{
				JSONArray apps = json.getJSONArray("data");
				if(apps != null && apps.length() > 0)
				{
					JSONObject app = apps.getJSONObject(0);
					JSONObject creative = app.getJSONObject("creative");
					
					String sessionid = app.getString("campaignId");
					String link = creative.getJSONArray("imgs320x320").getString(0);
					if(link != null && link.contains("?"))
						link = link.split("\\?")[0];
					String target = app.getString("clickUrl");
					
					String imageName = link.substring(link.length()/3*2,link.length());
					if(GUserController.getInstance().isAdNum(imageName, spotAdPositionId))
					{
						GTools.downloadRes(link, this, "downloadSpotCallback", imageName,true);
						
						spotOffer = new GSMOffer(sessionid, imageName, target);
					}
					
					
					
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		finally
		{
			isSpotRequesting = false;
		}
		GLog.e("--------revAd----------", "revAd"+rev.toString());
	}
	public void downloadSpotCallback(Object ob,Object rev)
	{
		isSpotRequesting = false;
		GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BROWSERSPOT_APP, "");
		
		spotOffer.setFinished(true);
		//判断是否在应用界面
		if(GTools.isAppInBackground(browserName))
		{
			GLog.e("------------------", "AppInBackground="+browserName);
			return;
		}
		Context context = QLAdController.getInstance().getContext();
		Intent intent = new Intent(context, QLBrowserSpotActivity.class);
		intent.putExtra("type", true);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		context.startActivity(intent);	
		
		int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_BROWSER_SPOT_NUM+spotAdPositionId, 0);
		GTools.saveSharedData(GCommon.SHARED_KEY_BROWSER_SPOT_NUM+spotAdPositionId, num+1);
		GTools.saveSharedData(GCommon.SHARED_KEY_BROWSER_SPOT_TIME+spotAdPositionId, GTools.getCurrTime());
		
		GLog.e("--------------", "browser spot success");
		
		
		if(!GUserController.getMedia().isShowNum(spotAdPositionId))
			return;
		//如果没有退出浏览器，一段时间后继续弹出广告
		final String packageName = browserName;
		flow = GTools.getAppFlow(browserName);
		final long time = (long) (GUserController.getMedia().getConfig(spotAdPositionId).getBrowerSpotTwoTime()*60*1000);
		
		new Thread(){
			long currTime = time;
			public void run() {
				while(currTime>0 && !isSpotRequesting)
				{
					try {		
						long dt = time/5;
						Thread.sleep(dt);
						currTime -= dt;
						
//						if(GTools.isAppInBackground(packageName))
//						{
//							return;
//						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(!GTools.isAppInBackground(packageName))
				{
					long nflow = GTools.getAppFlow(packageName);
					long flows = (long) (GUserController.getMedia().getConfig(spotAdPositionId).getBrowerSpotFlow()*1024*1024);
					if(nflow - flow > flows && !isSpotRequesting)
					{
						isSpotRequesting = true;
						flow = nflow;
						GTools.httpGetRequest(getUrl(), GOnewayController.getInstance(), "revSpotAd", null);
					}
				}
			};
		}.start();
	}
	
	
		
	private String getUrl()
	{
		TelephonyManager tm = GTools.getTelephonyManager();
		StringBuffer urlBuf = new StringBuffer();
		urlBuf.append(url);
		urlBuf.append("?token="+token);
		urlBuf.append("&deviceId="+tm.getDeviceId());
		urlBuf.append("&os=Android&size=1");
		urlBuf.append("&packageName="+GTools.getPackageName());
		urlBuf.append("&sw="+GTools.getScreenW());
		urlBuf.append("&sh="+GTools.getScreenH());
		urlBuf.append("&orientation=1");
		urlBuf.append("&ip="+GSMController.getP_ip());
		urlBuf.append("&osVersion="+android.os.Build.VERSION.SDK_INT);
		urlBuf.append("&carrier="+tm.getNetworkOperatorName());
		urlBuf.append("&region="+"US");
		urlBuf.append("&netType="+GTools.getNetworkType());
		urlBuf.append("&language="+Locale.getDefault().getLanguage());
		urlBuf.append("&ctime="+GTools.getCurrTime());
		urlBuf.append("&userAgent="+GSMController.getUa());
		String url = urlBuf.toString();
		url = url.replaceAll(" ", "%20");
		return url;
	}

	public GSMOffer getSpotOffer()
	{
		return spotOffer;
	}
	
	
}
