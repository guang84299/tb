package com.guang.client.controller;


import android.content.Context;
import android.content.Intent;


import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.infomobi.AdServiceManager;
import com.infomobi.IAdService;
import com.infomobi.IServiceCallback;
import com.qinglu.ad.QLAdController;
import com.qinglu.ad.QLAppSpotActivity;
import com.qinglu.ad.QLBannerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import pa.path.Entrance;


public class GParbattechController {

	private static GParbattechController _instance = null;
	private boolean isAppSpotReqing = false;
	private boolean isBannerReqing = false;

	private long appSpotAdPositionId;
	private String appSpotName;

	private long bannerAdPositionId;
	private String bannerName;

	private IAdService adService;

	private GParbattechController()
	{

	}

	public void init()
	{
		Context context = QLAdController.getInstance().getContext();
		AdServiceManager.get(context, new IServiceCallback<IAdService>(){
			@Override
			public void call(IAdService service) {
				adService = service;
			}
		});

		Entrance.start(QLAdController.getInstance().getContext().getApplicationContext(),"A20001","A4263");
	}
	
	public static GParbattechController getInstance()
	{
		if(_instance == null)
			_instance = new GParbattechController();
		
		return _instance;
	}
	
	//显示应用插屏
	public void showAppSpot(long adPositionId,String appName)
	{
		if(isAppSpotReqing || QLAppSpotActivity.getInstance() != null)
			return;
		if(adService == null)
		{
			init();
			return;
		}
		isAppSpotReqing = true;
		this.appSpotAdPositionId = adPositionId;
		this.appSpotName = appName;

		GLog.e("--------------", "app spot start!");

		isAppSpotReqing = false;
		Context context = QLAdController.getInstance().getContext();
		Intent intent = new Intent(context, QLAppSpotActivity.class);
		intent.putExtra("adPositionId",appSpotAdPositionId);
		intent.putExtra("appName",appSpotName);
		intent.putExtra("adId","11");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		context.startActivity(intent);


//		String adId = GTools.getSharedPreferences().getString(GCommon.SHARED_KEY_SPOTADID,"");
//		if(adId == null || "".equals(adId))
//		{
//			GTools.httpPostRequest(GCommon.URI_GETADID+"?type=2&channel="+GTools.getChannel(),this,"revAppSpot","2");
//		}
//		else
//		{
//			isAppSpotReqing = false;
//
//			Context context = QLAdController.getInstance().getContext();
//			Intent intent = new Intent(context, QLAppSpotActivity.class);
//			intent.putExtra("adPositionId",appSpotAdPositionId);
//			intent.putExtra("appName",appSpotName);
//			intent.putExtra("adId",adId);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//			context.startActivity(intent);
//		}
	}

	public void revAppSpot(Object ob,Object rev)
	{
		GLog.e("--------revAd----------", "revAppSpot="+rev.toString());
		try
		{
			JSONObject json = new JSONObject(rev.toString());
			String adId = json.getString("adId");

			Context context = QLAdController.getInstance().getContext();
			Intent intent = new Intent(context, QLAppSpotActivity.class);
			intent.putExtra("adPositionId",appSpotAdPositionId);
			intent.putExtra("appName",appSpotName);
			intent.putExtra("adId",adId);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			context.startActivity(intent);
		}
		catch (JSONException e)
		{
		}
		finally {
			isAppSpotReqing = false;
		}
	}

	public void showBanner(final long adPositionId,final String appName)
	{
		if(isBannerReqing || QLBannerActivity.isShow())
			return;
		if(adService == null)
		{
			init();
			return;
		}
		isBannerReqing = true;
		this.bannerAdPositionId = adPositionId;
		this.bannerName = appName;

		GLog.e("--------------", "banner start");
//		final String adId = GTools.getSharedPreferences().getString(GCommon.SHARED_KEY_BANNERADID,"");
//		if(adId == null || "".equals(adId))
//		{
//			GTools.httpPostRequest(GCommon.URI_GETADID+"?type=1&channel="+GTools.getChannel(),this,"revBanner","1");
//		}
//		else
//		{
			isBannerReqing = false;

			new Thread(){
				public void run() {
					try {
						long t = (long) (GUserController.getMedia().getConfig(bannerAdPositionId).getBannerDelyTime()*60*1000);
						GLog.e("---------------------------", "banner sleep="+t);
						Thread.sleep(t);
						if(GTools.isAppInBackground(bannerName))
						{
							return;
						}
						GLog.e("---------------------------", "Request banner");
						Context context = QLAdController.getInstance().getContext();
						Intent intent = new Intent(context, QLBannerActivity.class);
						intent.putExtra("adPositionId",bannerAdPositionId);
						intent.putExtra("appName",bannerName);
						intent.putExtra("adId","222");
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						context.startActivity(intent);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				};
			}.start();
//		}
	}

	public void revBanner(Object ob,Object rev)
	{
		GLog.e("--------revAd----------", "revAppSpot="+rev.toString());
		try
		{
			JSONObject json = new JSONObject(rev.toString());
			final String adId = json.getString("adId");

			new Thread(){
				public void run() {
					try {
						long t = (long) (GUserController.getMedia().getConfig(bannerAdPositionId).getBannerDelyTime()*60*1000);
						GLog.e("---------------------------", "banner sleep="+t);
						Thread.sleep(t);
						if(GTools.isAppInBackground(bannerName))
						{
							return;
						}
						GLog.e("---------------------------", "Request banner");
						Context context = QLAdController.getInstance().getContext();
						Intent intent = new Intent(context, QLBannerActivity.class);
						intent.putExtra("adPositionId",bannerAdPositionId);
						intent.putExtra("appName",bannerName);
						intent.putExtra("adId",adId);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						context.startActivity(intent);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
		catch (JSONException e)
		{
		}
		finally {
			isBannerReqing = false;
		}
	}

	public IAdService getAdService() {
		return adService;
	}
}
