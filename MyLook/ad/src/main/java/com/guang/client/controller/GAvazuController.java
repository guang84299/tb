package com.guang.client.controller;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.guang.client.GCommon;
import com.guang.client.mode.GSMOffer;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.qinglu.ad.QLAdController;
import com.qinglu.ad.QLBrowserSpotActivity;
import com.qinglu.ad.QLBannerActivity;

import android.content.Context;
import android.content.Intent;


public class GAvazuController {

	private static GAvazuController _instance = null;
	private GSMOffer offer;
	private final int Uid = 23164;
	private final int bannerSourceid = 26960;
	private final int spotSourceid = 27045;
	private final String url = "http://api.c.avazutracking.net/performance/v2/getcampaigns.php";
	private final String bannerUrl = "http://api.c.avazunativeads.com/s2s";
	private String browserName;
	private String appName;
	
	private long flow = 0;//流量
	private boolean isShowBanner = false;//是否显示banner标记
	private boolean isShowSpot = false;//是否显示插屏标记
	
	private long spotAdPositionId;
	private long bannerAdPositionId;
	
	
	
	private GAvazuController()
	{
		
	}
	
	public static GAvazuController getInstance()
	{
		if(_instance == null)
			_instance = new GAvazuController();
		return _instance;
	}
	
	public void init()
	{
		
	}
	
	public void showBanner(long adPositionId,final String appName)
	{
		this.appName = appName;
		this.bannerAdPositionId = adPositionId;
		
		if(isShowBanner)
			return;
		GLog.e("--------------", "banner start");
		isShowBanner = true;
		GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BANNER_APP, appName);
		new Thread(){
			public void run() {
				try {
					long t = (long) (GUserController.getMedia().getConfig(bannerAdPositionId).getBannerDelyTime()*60*1000);
					GLog.e("---------------------------", "banner sleep="+t);
					Thread.sleep(t);
					if(GTools.isAppInBackground(appName))
					{
						isShowBanner = false;
						GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BANNER_APP, "");
						return;
					}
					GLog.e("---------------------------", "Request banner");
					GTools.httpGetRequest(getUrl(true), GAvazuController.getInstance(), "revBannerAd", null);
					GTools.uploadStatistics(GCommon.REQUEST,GCommon.BANNER,"avazu");	
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
	public void revBannerAd(Object ob,Object rev)
	{
		try {
			GLog.e("--------revAd----------", "revBannerAd="+rev.toString());
			JSONObject json = new JSONObject(rev.toString());
			String status = json.getString("status");
			
			if("OK".equalsIgnoreCase(status))
			{
				JSONObject addata = json.getJSONObject("ads");
				JSONArray ads = addata.getJSONArray("ad");
				String link = null;
				String target = null;
				String sessionid = null;
				if(ads.length() > 0)
				{
					JSONObject ad = ads.getJSONObject(0);
					target = ad.getString("clkurl");
					sessionid = ad.getString("campaignid");
					
					JSONObject creatives = ad.getJSONObject("creatives");
					if(creatives.has("320x50"))
						link = creatives.getJSONArray("320x50").get(0).toString();
					else if(creatives.has("728x90"))
						link = creatives.getJSONArray("728x90").get(0).toString();
				}
				GLog.e("--------revAd----------", "link="+link+"  target="+target);
				if(link == null || "".equals(link.trim()) || target == null || "".equals(target.trim()))
				{
					isShowBanner = false;
					GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BANNER_APP, "");
					
					GLog.e("----------------------", "切换AppNext");
					GAPPNextController.getInstance().showBanner(bannerAdPositionId,appName);
				}
				else
				{
					String imageName = link.substring(link.length()/3*2,link.length());
					if(GUserController.getInstance().isAdNum(imageName, bannerAdPositionId))
					{
						GTools.downloadRes(link, this, "downloadBannerCallback", imageName,true);
						
						offer = new GSMOffer(sessionid, imageName, target);
					}
					else
					{
						GLog.e("----------------------", "切换AppNext");
						GAPPNextController.getInstance().showBanner(bannerAdPositionId,appName);
					}
					
					
				}
			}
			else
			{
				GLog.e("----------------------", "切换AppNext");
				GAPPNextController.getInstance().showBanner(bannerAdPositionId,appName);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			GLog.e("----------------------", "切换AppNext");
			GAPPNextController.getInstance().showBanner(bannerAdPositionId,appName);
		}	
		finally
		{
			isShowBanner = false;
			GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BANNER_APP, "");
		}
		
	}
	public void downloadBannerCallback(Object ob,Object rev)
	{
		isShowBanner = false;
		GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BANNER_APP, "");

		offer.setFinished(true);
		if(GTools.isAppInBackground(appName))
		{
			return;
		}
		Context context = QLAdController.getInstance().getContext();

		Intent intent = new Intent(context, QLBannerActivity.class);
		intent.putExtra("type", false);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		context.startActivity(intent); 
		
		int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_BANNER_NUM+bannerAdPositionId, 0);
		GTools.saveSharedData(GCommon.SHARED_KEY_BANNER_NUM+bannerAdPositionId, num+1);
		GTools.saveSharedData(GCommon.SHARED_KEY_BANNER_TIME+bannerAdPositionId,GTools.getCurrTime());	
		
		GLog.e("--------------", "banner success");
	}
	
	public void showSpot(long adPositionId,String browserName)
	{
		this.browserName = browserName;
		this.spotAdPositionId = adPositionId;
		
		if(isShowSpot)
			return;
		isShowSpot = true;
		GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BROWSERSPOT_APP, browserName);

		flow = GTools.getAppFlow(browserName);
		appFlowThread();
		GLog.e("--------------", "browser spot start");
	}
	public void appFlowThread()
	{
		new Thread(){
			public void run() {
				while(isShowSpot)
				{
					try {
						Thread.sleep(2000);
						long nflow = GTools.getAppFlow(browserName);
						long flows = (long) (GUserController.getMedia().getConfig(spotAdPositionId).getBrowerSpotFlow()*1024*1024);
						if(nflow - flow > flows)
						{
							flow = nflow;
							GTools.httpGetRequest(getUrl(true), GAvazuController.getInstance(), "revSpotAd", null);
							GTools.uploadStatistics(GCommon.REQUEST,GCommon.BROWSER_SPOT,"avazu");
							break;
						}
//						if(GTools.isAppInBackground(browserName))
//						{
//							GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BROWSERSPOT_APP, "");
//							isShowSpot = false;
//							return;
//						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	public void revSpotAd(Object ob,Object rev)
	{
		GLog.e("--------revSpotAd----------", "revSpotAd"+rev.toString());
		try {
			JSONObject json = new JSONObject(rev.toString());
			String status = json.getString("status");
			
			if("OK".equalsIgnoreCase(status))
			{
				JSONObject addata = json.getJSONObject("ads");
				JSONArray ads = addata.getJSONArray("ad");
				String link = null;
				String target = null;
				String sessionid = null;
				if(ads.length() > 0)
				{
					JSONObject ad = ads.getJSONObject(0);
					target = ad.getString("clkurl");
					sessionid = ad.getString("campaignid");
					
					JSONObject creatives = ad.getJSONObject("creatives");
					if(creatives.has("320x480"))
						link = creatives.getJSONArray("320x480").get(0).toString();
					else if(creatives.has("300x250"))
						link = creatives.getJSONArray("300x250").get(0).toString();
					else if(creatives.has("768x1024"))
						link = creatives.getJSONArray("768x1024").get(0).toString();
					else if(creatives.has("160x600"))
						link = creatives.getJSONArray("160x600").get(0).toString();
				}
				
				GLog.e("--------revAd----------", "link="+link+"  target="+target);
				if(link == null || "".equals(link.trim()) || target == null || "".equals(target.trim()))
				{
					isShowSpot = false;
					GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BANNER_APP, "");
					
					GLog.e("----------------------", "切换Oneway");
					GOnewayController.getInstance().showSpot(spotAdPositionId,browserName);
				}
				else
				{
					String imageName = link.substring(link.length()/3*2,link.length());
					if(GUserController.getInstance().isAdNum(imageName, spotAdPositionId))
					{
						GTools.downloadRes(link, this, "downloadSpotCallback", imageName,true);
						
						offer = new GSMOffer(sessionid, imageName, target);
					}
					else
					{
						GLog.e("----------------------", "切换Oneway");
						GOnewayController.getInstance().showSpot(spotAdPositionId,browserName);
					}
					
					
				}
				
			}
			else
			{
				GLog.e("----------------------", "切换Oneway");
				GOnewayController.getInstance().showSpot(spotAdPositionId,browserName);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			GLog.e("----------------------", "切换Oneway");
			GOnewayController.getInstance().showSpot(spotAdPositionId,browserName);
		}
		finally
		{
			isShowSpot = false;
			GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BROWSERSPOT_APP, "");
		}
//		GLog.e("--------revAd----------", "revAd"+rev.toString());
	}
	public void downloadSpotCallback(Object ob,Object rev)
	{
		isShowSpot = false;
		GTools.saveSharedData(GCommon.SHARED_KEY_TASK_BROWSERSPOT_APP, "");
		
		offer.setFinished(true);
		//判断是否在应用界面
		if(GTools.isAppInBackground(browserName))
		{
			GLog.e("------------------", "AppInBackground="+browserName);
			return;
		}
		Context context = QLAdController.getInstance().getContext();
		
		Intent intent = new Intent(context, QLBrowserSpotActivity.class);
		intent.putExtra("type", false);
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
		final long time = (long) (GUserController.getMedia().getConfig(spotAdPositionId).getBrowerSpotTwoTime()*60*1000);
		
		new Thread(){
			long currTime = time;
			public void run() {
				while(currTime>0 && !isShowSpot)
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
					if(nflow - flow > flows && !isShowSpot)
					{
						isShowSpot = true;
						flow = nflow;
						GTools.httpGetRequest(getUrl(true), GAvazuController.getInstance(), "revSpotAd", null);
					}
				}
			};
		}.start();
	}
	
	private String getUrl(boolean isBanner)
	{
		StringBuffer urlBuf = new StringBuffer();

		if(isBanner)
		{
			urlBuf.append(bannerUrl);
			urlBuf.append("?sourceid="+bannerSourceid);
			urlBuf.append("&page="+1);
			urlBuf.append("&pagenum="+1);
			urlBuf.append("&os=android");
		}
		else
		{
			urlBuf.append(url);
			urlBuf.append("?uid="+Uid + "&sourceid="+spotSourceid);
			urlBuf.append("&pagesize="+2);
			urlBuf.append("&category=102,103,104,105,106,107,108,109");
		}
		
		String url = urlBuf.toString();
		url = url.replaceAll(" ", "%20");
		return url;
	}

	public GSMOffer getOffer() {
		return offer;
	}

	public void setOffer(GSMOffer offer) {
		this.offer = offer;
	}
	
}
