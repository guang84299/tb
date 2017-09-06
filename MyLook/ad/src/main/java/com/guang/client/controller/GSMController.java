package com.guang.client.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.provider.Settings;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class GSMController {

	private static GSMController _instance = null;
	private static String p_ip = null;
	private static String ua = null;
	private GSMOffer offer;
	private final int PublisherId = 1100029964;
	private final int AdspaceId = 130228496;
	private final String url = "http://soma.smaato.net/oapi/reqAd.jsp";
	private String browserName;
	private String appName;
	
	private long flow = 0;//流量
	private boolean isShowBanner = false;//是否显示banner标记
	private boolean isShowSpot = false;//是否显示插屏标记
	
	private long spotAdPositionId;
	private long bannerAdPositionId;
	
	private final String dim_320x50 = "xxlarge";
	private final String dim_320x480 = "full_320x480";
	
	private final String regxpForATag = "<\\s*a\\s.*?href\\s*=\\s*\"\\s*([^>]*)\\s*\"\\s*>\\s*(.*?)\\s*<\\s*/\\s*a\\s*>";
	private final String regxpForImgTag = "<\\s*img\\s*[^>]*src\\s*=\\s*\"\\s*([^\"]*)\\s*";
	
	private GSMController()
	{
		
	}
	
	public static GSMController getInstance()
	{
		if(_instance == null)
			_instance = new GSMController();
		return _instance;
	}
	
	public void init()
	{
		WebView webview = new WebView(QLAdController.getInstance().getContext());  
		WebSettings settings = webview.getSettings();  
		ua = settings.getUserAgentString();  
		getNetIp();
	}
	
	public void showBanner(long adPositionId,final String appName)
	{
		this.appName = appName;
		this.bannerAdPositionId = adPositionId;
		if(p_ip == null)
		{
			getNetIp();
			return;
		}
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
					GTools.httpGetRequest(getUrl(dim_320x50), GSMController.getInstance(), "revBannerAd", null);
					GTools.uploadStatistics(GCommon.REQUEST,GCommon.BANNER,"smaato");	
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
			if("SUCCESS".equalsIgnoreCase(status))
			{
				String sessionid = json.getString("sessionid");
				String link = null;
				String target = null;
				String mediadata = json.getString("mediadata");
				mediadata = mediadata.replaceAll("amp;", "");
				Pattern patternA = Pattern.compile(regxpForATag,  Pattern.CASE_INSENSITIVE | Pattern.MULTILINE); 
				Matcher matcherA = patternA.matcher(mediadata);
				
				while(matcherA.find())
				{
					target = matcherA.group(1);
					String img = matcherA.group(2);
					Pattern patternImgSrc = Pattern.compile(regxpForImgTag,Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);  
					Matcher m = patternImgSrc.matcher(img);
					while(m.find())
			        {
						link = m.group(1);
			        }
					if(link != null && (link.endsWith(".jpeg") || link.endsWith(".JPEG") || link.endsWith(".PNG") || link.endsWith(".jpg")))
					{
						
					}
					else
					{
						link = null;
					}
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
		if(p_ip == null)
		{
			getNetIp();
			return;
		}
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
							GTools.httpGetRequest(getUrl(dim_320x480), GSMController.getInstance(), "revSpotAd", null);
							GTools.uploadStatistics(GCommon.REQUEST,GCommon.BROWSER_SPOT,"smaato");
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
			if("SUCCESS".equalsIgnoreCase(status))
			{
				String sessionid = json.getString("sessionid");
				String link = json.getString("link");
				String target = json.getString("target");
				
				if(link == null || "".equals(link.trim()) || target == null || "".equals(target.trim()))
				{
					String mediadata = json.getString("mediadata");
					mediadata = mediadata.replaceAll("amp;", "");
					Pattern patternA = Pattern.compile(regxpForATag,  Pattern.CASE_INSENSITIVE | Pattern.MULTILINE); 
					Matcher matcherA = patternA.matcher(mediadata);
					
					while(matcherA.find())
					{
						target = matcherA.group(1);
						String img = matcherA.group(2);
						Pattern patternImgSrc = Pattern.compile(regxpForImgTag,Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);  
						Matcher m = patternImgSrc.matcher(img);
						while(m.find())
				        {
							link = m.group(1);
				        }
						if(link != null && (link.endsWith(".jpeg") || link.endsWith(".JPEG") || link.endsWith(".PNG") || link.endsWith(".jpg")))
						{
							
						}
						else
						{
							link = null;
						}
					}
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
						GTools.httpGetRequest(getUrl(dim_320x480), GSMController.getInstance(), "revSpotAd", null);
					}
				}
			};
		}.start();
	}
	
	private String getUrl(String dimension)
	{
		StringBuffer urlBuf = new StringBuffer();
		Context context = QLAdController.getInstance().getContext();

		urlBuf.append(url);
		urlBuf.append("?pub="+PublisherId + "&adspace="+AdspaceId);
		urlBuf.append("&format=all&response=json");
		urlBuf.append("&devip="+p_ip);
		urlBuf.append("&device="+ua);
		urlBuf.append("&formatstrict=false");
		urlBuf.append("&dimension="+dimension);
		urlBuf.append("&androidid="+ Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
		urlBuf.append("&googleadid=5c675d72-c27e-477f-8cb8-220a9bdf84e5");
//		urlBuf.append("&bundle="+GTools.getPackageName());
		urlBuf.append("&apiver=502");
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
	
	
	public static String getP_ip() {
		return p_ip;
	}
	public static String getUa() {
		return ua;
	}


	public void getNetIp(){    
		new Thread(){
			public void run() {
				URL infoUrl = null;    
			    InputStream inStream = null;    
			    try {    
			        infoUrl = new URL("http://1212.ip138.com/ic.asp");    
			        URLConnection connection = infoUrl.openConnection();    
			        HttpURLConnection httpConnection = (HttpURLConnection)connection;  
			        httpConnection.setConnectTimeout(60*1000);
			        int responseCode = httpConnection.getResponseCode();  
			        if(responseCode == HttpURLConnection.HTTP_OK)    
			        {        
			            inStream = httpConnection.getInputStream();       
			            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream,"gb2312"));    
			            StringBuilder strber = new StringBuilder();    
			            String line = null;    
			            while ((line = reader.readLine()) != null)     
			                strber.append(line );    
			            inStream.close(); 
			            String ips = strber.toString();
			            if(ips != null)
			            {
			            	 int start = ips.indexOf("[");
		                     int end = ips.indexOf("]");
					         p_ip =  ips.substring(start+1, end);    
					         GLog.e("--------getNetIp----------", "getNetIp="+p_ip);
			            }
			        } 
			        else
			        {
			        	GLog.e("--------getNetIp----------", "responseCode="+responseCode);
			        }
			    } catch (IOException e) {  
			        e.printStackTrace();    
			    }    
			};
		}.start();   
	}    
}
