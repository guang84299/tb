package com.guang.client.controller;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.guang.client.GCommon;
import com.guang.client.mode.GAdPositionConfig;
import com.guang.client.mode.GOffer;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.qinglu.ad.QLAdController;
import com.qinglu.ad.QLAppSpotActivity;
import com.qinglu.ad.QLBannerActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class GAPPNextController {

	private static GAPPNextController _instance = null;
	private List<GOffer> installOffers;
	private List<GOffer> unInstallOffers;
	private GOffer spotOffer;
	private GOffer lockOffer;
	private GOffer bannerOffer;
	private GOffer gpOffer;
	private final String AdspaceId = "304af244-164f-4e4c-9bd0-374843427f22";
	private final String url = "http://admin.appnext.com/offerWallApi.aspx";
	
	private boolean isSpotRequesting = false;
	private boolean isInsallRequesting = false;
	private boolean isUnInstallRequesting = false;
	private boolean isLockRequesting = false;
	private boolean isBannerRequesting = false;
	private boolean isGPRequesting = false;
	
	private long spotAdPositionId;
//	private long installAdPositionId;
//	private long unInstallAdPositionId;
	private long lockAdPositionId;
	private long bannerAdPositionId;
	private long gpAdPositionId;
	
	private String bannerAppName;
	private String appName;
	
	private GAPPNextController()
	{
		installOffers = new ArrayList<GOffer>();
		unInstallOffers = new ArrayList<GOffer>();
	}
	
	public static GAPPNextController getInstance()
	{
		if(_instance == null)
			_instance = new GAPPNextController();
		
		return _instance;
	}
	
	//显示应用插屏
	public void showAppSpot(long adPositionId,String appName)
	{
		this.appName = appName;
		this.spotAdPositionId = adPositionId;
		if(isSpotRequesting)
			return;
		GLog.e("--------------", "appnext spot start!");
		spotOffer = null;
		isSpotRequesting = true;
		GTools.httpGetRequest(getUrl(1),this, "revAppSpotAd", null);
		GTools.uploadStatistics(GCommon.REQUEST,GCommon.APP_SPOT,"appNext");
	}
	public void revAppSpotAd(Object ob,Object rev)
	{
		try {
			JSONObject json = new JSONObject(rev.toString());
			JSONArray apps = json.getJSONArray("apps");
			if(apps != null && apps.length() > 0)
			{
				JSONObject app = apps.getJSONObject(0);
				
				String title = app.getString("title");
				String desc = app.getString("desc");
				String urlImg = app.getString("urlImg");
				String urlImgWide = app.getString("urlImgWide");
				String campaignId = app.getString("campaignId");
				String androidPackage = app.getString("androidPackage");
				String appSize = app.getString("appSize");
				String urlApp = app.getString("urlApp");
				
				String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
                String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());
                 
                if(GUserController.getInstance().isAdNum(imageName, spotAdPositionId))
                {
                	 GTools.downloadRes(urlImgWide, this, "downloadAppSpotCallback", imageName,true);
                     GTools.downloadRes(urlImg, this, "downloadAppSpotCallback", iconName,true);
                     spotOffer = new GOffer(campaignId, androidPackage, title,
                     		 desc, appSize, iconName, imageName,urlApp); 
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
	public void downloadAppSpotCallback(Object ob,Object rev)
	{
		if(spotOffer != null)
		{
			spotOffer.setPicNum(spotOffer.getPicNum()+1);
		}
		// 判断图片是否存在
		if(spotOffer.getPicNum()==2)
		{
			QLAppSpotActivity.hide();
			if(GTools.isAppInBackground(appName))
			{
				return;
			}
			Context context = QLAdController.getInstance().getContext();
			Intent intent = new Intent(context, QLAppSpotActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			context.startActivity(intent);	
			
			int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_APP_SPOT_NUM+spotAdPositionId, 0);
			GTools.saveSharedData(GCommon.SHARED_KEY_APP_SPOT_NUM+spotAdPositionId, num+1);
			GTools.saveSharedData(GCommon.SHARED_KEY_APP_SPOT_TIME+spotAdPositionId,GTools.getCurrTime());
			GLog.e("--------------", "app spot success!");
		}
		
	}
	
	//显示充电锁
	public void showLock()
	{
		if(isLockRequesting)
			return;
		GLog.e("--------------", "lock start!");
		List<GAdPositionConfig> list = GUserController.getMedia().getConfig(GCommon.CHARGLOCK);
		for(GAdPositionConfig config : list)
		{
			lockAdPositionId = config.getAdPositionId();
		}
		lockOffer = null;
		isLockRequesting = true;
		GTools.httpGetRequest(getUrl(1), this, "revLockAd", null);
		GTools.uploadStatistics(GCommon.REQUEST,GCommon.CHARGLOCK,"appNext");
	}
	public void revLockAd(Object ob,Object rev)
	{
		try {
			JSONObject json = new JSONObject(rev.toString());
			JSONArray apps = json.getJSONArray("apps");
			if(apps != null && apps.length() > 0)
			{
				JSONObject app = apps.getJSONObject(0);
				
				String title = app.getString("title");
				String desc = app.getString("desc");
				String urlImg = app.getString("urlImg");
				String urlImgWide = app.getString("urlImgWide");
				String campaignId = app.getString("campaignId");
				String androidPackage = app.getString("androidPackage");
				String appSize = app.getString("appSize");
				String urlApp = app.getString("urlApp");
				
				String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
                String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());
                 
                if(GUserController.getInstance().isAdNum(iconName, lockAdPositionId))
                {
                	 GTools.downloadRes(urlImgWide, this, "downloadLockCallback", imageName,true);
                     GTools.downloadRes(urlImg, this, "downloadLockCallback", iconName,true);
                     lockOffer = new GOffer(campaignId, androidPackage, title,
                     		 desc, appSize, iconName, imageName,urlApp);  
                     
                }
               
			}
		} catch (JSONException e) {
			e.printStackTrace();
			isLockRequesting = false;
		}	
		GLog.e("--------revAd----------", "revAd"+rev.toString());
	}
	public void downloadLockCallback(Object ob,Object rev)
	{
		if(lockOffer!=null)
		{
			lockOffer.setPicNum(lockOffer.getPicNum()+1);
			if(lockOffer.getPicNum() == 2)
				isLockRequesting = false;
		}
		
	}
	public boolean isCanShowLock()
	{
		// 判断图片是否存在
		return (lockOffer != null && lockOffer.getPicNum() == 2);
	}
	
	//显示安装
	public void showInstall()
	{
		if(isInsallRequesting)
			return;
		GLog.e("--------------", "install start!");
		installOffers.clear();
		isInsallRequesting = true;
		GTools.httpGetRequest(getUrl(2), GAPPNextController.getInstance(), "revInstallAd", null);
		GTools.uploadStatistics(GCommon.REQUEST,GCommon.APP_INSTALL,"appNext");
	}
	public void revInstallAd(Object ob,Object rev)
	{
		try {
			JSONObject json = new JSONObject(rev.toString());
			JSONArray apps = json.getJSONArray("apps");
			if(apps != null && apps.length() > 0)
			{
				for(int i=0;i<apps.length();i++)
				{
					JSONObject app = apps.getJSONObject(i);
					
					String title = app.getString("title");
					String desc = app.getString("desc");
					String urlImg = app.getString("urlImg");
					String urlImgWide = app.getString("urlImgWide");
					String campaignId = app.getString("campaignId");
					String androidPackage = app.getString("androidPackage");
					String appSize = app.getString("appSize");
					String urlApp = app.getString("urlApp");
					
					String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
	                String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());
	                 
//	                GTools.downloadRes(urlImgWide, this, "downloadLockCallback", imageName,true);
	                GTools.downloadRes(urlImg, this, "downloadInstallCallback", iconName,true);
	                installOffers.add(new GOffer(campaignId, androidPackage, title,
	                		 desc, appSize, iconName, imageName,urlApp)); 
	                
	                
				}
	
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		finally
		{
			isInsallRequesting = false;
		}
		GLog.e("--------revAd----------", "revAd"+rev.toString());
	}
	public void downloadInstallCallback(Object ob,Object rev)
	{		
		if(installOffers.size() >= 2)
		{
			if(installOffers.get(0).getPicNum() == 0)
				installOffers.get(0).setPicNum(1);
			else
			{
				installOffers.get(1).setPicNum(1);
				GTools.sendBroadcast(GCommon.ACTION_QEW_APP_INSTALL_UI);
				GLog.e("--------------", "install success!");
			}
		}
	}
	
	//显示卸载
	public void showUnInstall()
	{
		if(isUnInstallRequesting)
			return;
		GLog.e("--------------", "unInstall start!");
		unInstallOffers.clear();
		isUnInstallRequesting = true;
		GTools.httpGetRequest(getUrl(2), GAPPNextController.getInstance(), "revUnInstallAd", null);
		GTools.uploadStatistics(GCommon.REQUEST,GCommon.APP_UNINSTALL,"appNext");
	}
	public void revUnInstallAd(Object ob,Object rev)
	{
		try {
			JSONObject json = new JSONObject(rev.toString());
			JSONArray apps = json.getJSONArray("apps");
			if(apps != null && apps.length() > 0)
			{
				for(int i=0;i<apps.length();i++)
				{
					JSONObject app = apps.getJSONObject(i);
					
					String title = app.getString("title");
					String desc = app.getString("desc");
					String urlImg = app.getString("urlImg");
					String urlImgWide = app.getString("urlImgWide");
					String campaignId = app.getString("campaignId");
					String androidPackage = app.getString("androidPackage");
					String appSize = app.getString("appSize");
					String urlApp = app.getString("urlApp");
					
					String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
	                String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());
	                 
//	                GTools.downloadRes(urlImgWide, this, "downloadLockCallback", imageName,true);
	                GTools.downloadRes(urlImg, this, "downloadUnInstallCallback", iconName,true);
	                unInstallOffers.add(new GOffer(campaignId, androidPackage, title,
	                		 desc, appSize, iconName, imageName,urlApp)); 
	                
	                
				}
	
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		finally
		{
			isUnInstallRequesting = false;
		}
		GLog.e("--------revAd----------", "revAd"+rev.toString());
	}
	public void downloadUnInstallCallback(Object ob,Object rev)
	{
		if(unInstallOffers.size() >= 2)
		{
			if(unInstallOffers.get(0).getPicNum() == 0)
				unInstallOffers.get(0).setPicNum(1);
			else
			{
				unInstallOffers.get(1).setPicNum(1);
				GTools.sendBroadcast(GCommon.ACTION_QEW_APP_UNINSTALL_UI);
				GLog.e("--------------", "unInstall success!");
			}
		}
	}
	
	//显示banner
	public void showBanner(long adPositionId,String bannerAppName)
	{
		this.bannerAppName = bannerAppName;
		this.bannerAdPositionId = adPositionId;
		if(isBannerRequesting)
			return;
		GLog.e("--------------", "banner start!");
		bannerOffer = null;
		isBannerRequesting = true;
		GTools.httpGetRequest(getUrl(1),this, "revBannerAd", null);
		GTools.uploadStatistics(GCommon.REQUEST,GCommon.BANNER,"appNext");	
	}
	public void revBannerAd(Object ob,Object rev)
	{
		try {
			JSONObject json = new JSONObject(rev.toString());
			JSONArray apps = json.getJSONArray("apps");
			if(apps != null && apps.length() > 0)
			{
				JSONObject app = apps.getJSONObject(0);
				
				String title = app.getString("title");
				String desc = app.getString("desc");
				String urlImg = app.getString("urlImg");
				String urlImgWide = app.getString("urlImgWide");
				String campaignId = app.getString("campaignId");
				String androidPackage = app.getString("androidPackage");
				String appSize = app.getString("appSize");
				String urlApp = app.getString("urlApp");
				
				String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
                String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());
                if(GUserController.getInstance().isAdNum(imageName, bannerAdPositionId))
                {
//                  GTools.downloadRes(urlImgWide, this, "downloadBannerCallback", imageName,true);
                    GTools.downloadRes(urlImg, this, "downloadBannerCallback", iconName,true);
                    bannerOffer = new GOffer(campaignId, androidPackage, title,
                    		 desc, appSize, iconName, imageName,urlApp);  
                }

                 
             	
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		finally
		{
			isBannerRequesting = false;
		}
		GLog.e("--------revAd----------", "revAd"+rev.toString());
	}
	public void downloadBannerCallback(Object ob,Object rev)
	{
		if(bannerOffer != null)
		{
			bannerOffer.setPicNum(bannerOffer.getPicNum()+1);
		}
		// 判断图片是否存在
		if(bannerOffer.getPicNum()==1)
		{
			if(GTools.isAppInBackground(bannerAppName))
			{
				return;
			}
			Context context = QLAdController.getInstance().getContext();
			Intent intent = new Intent(context, QLBannerActivity.class);
			intent.putExtra("type", true);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			context.startActivity(intent);	
			
			int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_BANNER_NUM+bannerAdPositionId, 0);
			GTools.saveSharedData(GCommon.SHARED_KEY_BANNER_NUM+bannerAdPositionId, num+1);
			GTools.saveSharedData(GCommon.SHARED_KEY_BANNER_TIME+bannerAdPositionId,GTools.getCurrTime());	
			
			GLog.e("--------------", "banner success");
		}
		
	}
	
	

	//显示GPBREAK
	public void showGpBreak(long adPositionId,String appName)
	{
		this.appName = appName;
		this.gpAdPositionId = adPositionId;
		if(isGPRequesting)
			return;
		GLog.e("--------------", "gp break start!");
		gpOffer = null;
		isGPRequesting = true;
		GTools.httpGetRequest(getUrl(1),this, "reGPAd", null);
		GTools.uploadStatistics(GCommon.REQUEST,GCommon.GP_BREAK,"appNext");	
	}
	public void reGPAd(Object ob,Object rev)
	{
		try {
			JSONObject json = new JSONObject(rev.toString());
			JSONArray apps = json.getJSONArray("apps");
			if(apps != null && apps.length() > 0)
			{
				JSONObject app = apps.getJSONObject(0);
				
				String title = app.getString("title");
				String desc = app.getString("desc");
				String urlImg = app.getString("urlImg");
				String urlImgWide = app.getString("urlImgWide");
				String campaignId = app.getString("campaignId");
				String androidPackage = app.getString("androidPackage");
				String appSize = app.getString("appSize");
				String urlApp = app.getString("urlApp");
				
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
						GLog.e("--------------------", "切换广告源 mi");
						GMIController.getInstance().showGpBreak(gpAdPositionId,appName);
						GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,num+1);
					}
				}
			}
			else
			{
				int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,1);
				if(num<=5)
				{
					GLog.e("--------------------", "切换广告源 mi");
					GMIController.getInstance().showGpBreak(gpAdPositionId,appName);
					GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,num+1);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();

			int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,1);
			if(num<=5)
			{
				GLog.e("--------------------", "切换广告源 mi");
				GMIController.getInstance().showGpBreak(gpAdPositionId,appName);
				GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_TOP_NUM,num+1);
			}
		}	
		finally
		{
			isGPRequesting = false;
		}
		GLog.e("--------revAd----------", "revAd"+rev.toString());
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
//			GTools.sendBroadcast(GCommon.ACTION_QEW_APP_GP_BREAK);
			Context context = QLAdController.getInstance().getContext();
			Intent intent = new Intent();
			intent.putExtra("type","appNext");
			intent.setAction(GCommon.ACTION_QEW_APP_GP_BREAK);
			context.sendBroadcast(intent);
			GLog.e("--------------", "gp break success");
		}
	}

	//补刷GPBREAK
	public void showGpBrushBreak()
	{
		GLog.e("--------------", "gp brush break start!");
		GTools.httpGetRequest(getUrl(1),this, "reGPBrushAd", null);
	}

	public void reGPBrushAd(Object ob,Object rev)
	{
		try {
			JSONObject json = new JSONObject(rev.toString());
			JSONArray apps = json.getJSONArray("apps");
			if(apps != null && apps.length() > 0)
			{
				JSONObject app = apps.getJSONObject(0);

				String urlImg = app.getString("urlImg");
				String urlImgWide = app.getString("urlImgWide");
				String campaignId = app.getString("campaignId");

//				String imageName = urlImgWide.substring(urlImgWide.length()/3*2, urlImgWide.length());
				String iconName = urlImg.substring(urlImg.length()/3*2, urlImg.length());

//                GTools.downloadRes(urlImgWide, this, "downloadGPCallback", imageName,true);
				GTools.downloadRes(urlImg, this, null, iconName,true);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
		
	private String getUrl(int cnt)
	{
		StringBuffer urlBuf = new StringBuffer();
		urlBuf.append(url);
		urlBuf.append("?pimg=1&tid=API" + "&id="+AdspaceId);
		urlBuf.append("&format=all&response=json");
		urlBuf.append("&cnt="+cnt);
		return urlBuf.toString();
	}

	public GOffer getSpotOffer()
	{
		return spotOffer;
	}
	public GOffer getLockOffer()
	{
		return lockOffer;
	}
	public List<GOffer> getInstallOffer()
	{
		return installOffers;
	}
	public List<GOffer> getUnInstallOffer()
	{
		return unInstallOffers;
	}

	public GOffer getBannerOffer() {
		return bannerOffer;
	}

	public GOffer getGpOffer() {
		return gpOffer;
	}
	
}
