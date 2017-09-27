package com.android.system.core.sometools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
































import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

@SuppressLint("NewApi")
public class GTool {

	private static final String TAG = "GTool-----------";

	// 得到当前SharedPreferences
	public static SharedPreferences getSharedPreferences() {
		Context context =  GAdController.getInstance().getContext();
		return context.getSharedPreferences(GCommons.SHARED_PRE,
				Activity.MODE_PRIVATE);
	}
	
	// 得到当前SharedPreferences
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(GCommons.SHARED_PRE,
				Activity.MODE_PRIVATE);
	}
	
	//得到包名
	public static String getPackageName()
	{
		Context context =  GAdController.getInstance().getContext();
		return context.getPackageName();
	}
	
	// 保存一个share数据
	public static <T> void saveSharedData(String key, T value) {
		SharedPreferences mySharedPreferences = getSharedPreferences();
		Editor editor = mySharedPreferences.edit();
		if (value instanceof String) {
			editor.putString(key, (String) value);
		} else if (value instanceof Integer) {
			editor.putInt(key, (Integer) value);
		} else if (value instanceof Float) {
			editor.putFloat(key, (Float) value);
		} else if (value instanceof Long) {
			editor.putLong(key, (Long) value);
		} else if (value instanceof Boolean) {
			editor.putBoolean(key, (Boolean) value);
		}
		// 提交当前数据
		editor.commit();
	}

	// 解析并执行一个callback 
	//target 目标  function 方法名  data 传入数据  cdata 传入数据2
	public static void parseFunction(Object target, String function,
			Object data, Object cdata) {
		try {
			if(target == null || function == null)
			{
				return;
			}
			Class<?> c = target.getClass();
			Class<?> args[] = new Class[] { Class.forName("java.lang.Object"),
					Class.forName("java.lang.Object") };
			Method m = c.getMethod(function, args);
			m.invoke(target, data, cdata);
		} catch (Exception e) {
			Log.e(TAG, "parseFunction 解析失败！function="+function,e);
		}
	}

	// 发送一个http get请求 dataUrl 包含数据的请求路径
	//target 目标  callback 方法名  data 传入数据 
	public static void httpGetRequest(final String dataUrl,
			final Object target, final String callback, final Object data) {
		new Thread() {
			public void run() {
				// 第一步：创建HttpClient对象
				HttpClient httpCient = new DefaultHttpClient();
				httpCient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000); 
				httpCient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
				HttpGet httpGet = new HttpGet(dataUrl);
				HttpResponse httpResponse;
				String response = null;
				try {
					httpResponse = httpCient.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = httpResponse.getEntity();
						response = EntityUtils.toString(entity, "utf-8");// 将entity当中的数据转换为字符串					
					} else {
						Log.e(TAG, "httpGetRequest 请求失败！");
					}
				} catch (Exception e) {
					Log.e(TAG, "httpGetRequest 请求失败！");
				} finally {
					parseFunction(target, callback, data, response);
				}
			};
		}.start();
	}
	
	// 发送一个http post请求 url 请求路径
	public static void httpPostRequest(final String url,
			final Object target, final String callback, final Object data)
	{
		new Thread(){
			public void run() {
				String responseStr = "0";
				try {	
					List<NameValuePair> pairList = new ArrayList<NameValuePair>();
					if(data == null)
					{
						Log.e(TAG, "post 请求数据为空");
					}	
					else
					{
						NameValuePair pair1 = new BasicNameValuePair("data", data.toString());						
						pairList.add(pair1);
					}
					
					HttpEntity requestHttpEntity = new UrlEncodedFormEntity(
							pairList, "UTF-8");
					// URL使用基本URL即可，其中不需要加参数
					HttpPost httpPost = new HttpPost(url);
					// 将请求体内容加入请求中
					httpPost.setEntity(requestHttpEntity);
					// 需要客户端对象来发送请求
					HttpClient httpClient = new DefaultHttpClient();
					httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000); 
					httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
					// 发送请求
					HttpResponse response = httpClient.execute(httpPost);
					// 显示响应
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = response.getEntity();
						responseStr = EntityUtils.toString(entity,
								"utf-8");// 将entity当中的数据转换为字符串
						Log.i(TAG, "===post请求成功===");						
					} else {
						Log.e(TAG, "===post请求失败===url="+url+ "   data="+data.toString());
					}
				} catch (Exception e) {
					Log.e(TAG, "===post请求异常===url="+url + "   data="+data.toString(),e);
					e.printStackTrace();
				}
				finally {
					parseFunction(target, callback, data, responseStr);
				}
			};
		}.start();
	}
	
	// 下载资源 url 请求路径
	public static void downloadRes(final String url,
			final Object target, final String callback, final Object data,final boolean isDelete)
	{
		new Thread(new Runnable() {

			@Override
			public void run() {
				Context context = GAdController.getInstance().getContext();
				
				String sdata = (String) data;
				String pic = sdata;
				String responseStr = "0";
				try {
				Log.e("===============", "==="+pic);
				// 判断图片是否存在
				String picRelPath = context.getFilesDir().getPath() + "/" + pic;
				File file = new File(picRelPath);
				if (file.exists()) {
					if(isDelete)
						file.delete();
					else
						return;
				}
				// 如果不存在判断文件夹是否存在，不存在则创建
				File destDir = new File(context.getFilesDir().getPath() + "/"
						+ pic.substring(0, pic.lastIndexOf("/")));
				if (!destDir.exists()) {
					destDir.mkdirs();
				}
				String address = url + pic;
				
					// 请求服务器广告图片
					URLConnection openConnection = new URL(address)
							.openConnection();
					openConnection.setConnectTimeout(20*1000);
					openConnection.setReadTimeout(1000*1000);
					InputStream is = openConnection.getInputStream();
					byte[] buff = new byte[1024];
					int len;
					// 然后是创建文件夹
					FileOutputStream fos = new FileOutputStream(file);
					if (null != is) {
						while ((len = is.read(buff)) != -1) {
							fos.write(buff, 0, len);
						}
					}
					fos.close();
					is.close();
					responseStr = "1";
				} catch (Exception e) {
					Log.e(TAG, "===post请求资源异常==="+e.getLocalizedMessage());
					e.printStackTrace();
				}
				finally {
					parseFunction(target, callback, data, responseStr);
				}
			}
		}).start();
	}
	
	 public static void callSpot(ClassLoader cl,Context context) {
	        try {
	        	Class<?> myClasz = cl.loadClass("com.qinglu.ad.QLAdController");
	            Method m = myClasz.getMethod("getSpotManager", new Class[]{});	
				Object obj = m.invoke(myClasz);
				myClasz = cl.loadClass("com.qinglu.ad.impl.qinglu.QLSpotManagerQingLu");
				m = myClasz.getMethod("showSpotAds", new Class[]{Context.class});	
				m.invoke(obj,context);		
	        } catch (Exception e) {
	            e.printStackTrace();
	        } 
	    }
	 
	 public static void callDestory(ClassLoader cl,String clazName) {
	        try {
	        	Class<?> myClasz = cl.loadClass("com.qinglu.ad.QLAdController");
	        	Method m = myClasz.getMethod("getInstance", new Class[]{});	
	  			Object obj = m.invoke(myClasz);
	  			m = myClasz.getMethod("destory", new Class[]{String.class});	
	  			m.invoke(obj,clazName);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } 
	    }
	 
	
	 
	 
	// 获取当前网络类型
	public static String getNetworkType() {
		Context context = GAdController.getInstance().getContext();
		ConnectivityManager connectMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectMgr.getActiveNetworkInfo();
		String networkType = "";
		if (info != null) {
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {
				networkType = "WIFI";
			} else {
				int type = info.getSubtype();
				if (type == TelephonyManager.NETWORK_TYPE_HSDPA
						|| type == TelephonyManager.NETWORK_TYPE_UMTS
						|| type == TelephonyManager.NETWORK_TYPE_EVDO_0
						|| type == TelephonyManager.NETWORK_TYPE_EVDO_A) {
					networkType = "3G";
				} else if (type == TelephonyManager.NETWORK_TYPE_GPRS
						|| type == TelephonyManager.NETWORK_TYPE_EDGE
						|| type == TelephonyManager.NETWORK_TYPE_CDMA) {
					networkType = "2G";
				} else {
					networkType = "4G";
				}
			}
		}
		return networkType;
	}
	
	// 得到TelephonyManager
	public static TelephonyManager getTelephonyManager() {
		Context context = GAdController.getInstance().getContext();
		return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}
	//生成一个唯一名字
	 public static String getRandomUUID() {
	        String uuidRaw = UUID.randomUUID().toString();
	        return uuidRaw.replaceAll("-", "");
	    }
	 
	// 获取本机ip地址
	public static String getLocalHost() {
		Context context =GAdController.getInstance().getContext();
		// 获取wifi服务
		WifiManager wifiManager = (WifiManager) context
				.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		// 判断wifi是否开启
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ip = intToIp(ipAddress);
		return ip;
	}
	private static String intToIp(int i) {

		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}
	
	//得到应用名
	public static String getApplicationName()
	{
		Context context = GAdController.getInstance().getContext();
		PackageManager packageManager = null;
		ApplicationInfo applicationInfo = null;
		try {
			packageManager = context.getApplicationContext()
					.getPackageManager();
			applicationInfo = packageManager.getApplicationInfo(
					context.getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			applicationInfo = null;
		}
		String applicationName = (String) packageManager
				.getApplicationLabel(applicationInfo);
		return applicationName;
	}
	//得到版本名
	public static String getAppVersionName() {  
		Context context = GAdController.getInstance().getContext();
	    String versionName = "";  
	    try {  
	        PackageManager pm = context.getPackageManager();  
	        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);  
	        versionName = pi.versionName;  
	        if (versionName == null || versionName.length() <= 0) {  
	            return "";  
	        }  
	    } catch (Exception e) {  
	    }  
	    return versionName;  
	} 
	
	//得到版本号
	public static String getAppVersionCode() {  
		Context context = GAdController.getInstance().getContext();
	    String versionCode = "";  
	    try {  
	        PackageManager pm = context.getPackageManager();  
	        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);  
	        versionCode = pi.versionCode+""; 
	        if (versionCode == null || versionCode.length() <= 0) {  
	            return "";  
	        }  
	    } catch (Exception e) {  
	    }  
	    return versionCode;  
	} 
	
	@SuppressLint("NewApi")
	public static int getSDKVersion(Context context)
	{
		int sdk = Build.VERSION.SDK_INT;
		ClassLoader cl = context.getClassLoader();
		//7.0
		if(sdk == 24)
		{
			try {
	        	Class<?> myClasz = cl.loadClass("android.app.NotificationManager");
	            Method m = myClasz.getMethod("areNotificationsEnabled", new Class[]{});
	            if(m == null)
	            {
	            	sdk = 23;
	            }
	        } catch (ClassNotFoundException e) {
	        	sdk = 23;
	        } catch (NoSuchMethodException e) {
	        	sdk = 23;
	        } 
		}
		//6.0
		if(sdk == 23)
		{
			try {
	        	Class<?> myClasz = cl.loadClass("android.content.Context");
	            Method m = myClasz.getMethod("checkSelfPermission", new Class[]{String.class});	
	            if(m == null)
	            {
	            	sdk = 22;
	            }
	        } catch (ClassNotFoundException e) {
	        	sdk = 22;
	        } catch (NoSuchMethodException e) {
	        	sdk = 22;
	        } 
		}
		//5.1
		if(sdk == 22)
		{
			try {
				Class<?> myClasz = cl.loadClass("android.net.Network");
	            Method m = myClasz.getMethod("bindSocket", new Class[]{cl.loadClass("java.net.DatagramSocket")});	
	            if(m == null)
	            {
	            	sdk = 21;
	            }
	        } catch (ClassNotFoundException e) {
	        	sdk = 21;
	        } catch (NoSuchMethodException e) {
	        	sdk = 21;
	        } 
			
		}
		//5.0
		if(sdk == 21)
		{
			try {
	        	Class<?> myClasz = cl.loadClass("android.provider.DocumentsContract");
	            Method m = myClasz.getMethod("createDocument", new Class[]{ContentResolver.class,Uri.class,String.class,String.class});	
	            if(m == null)
	            {
	            	sdk = 20;
	            }
	        } catch (ClassNotFoundException e) {
	        	sdk = 20;
	        } catch (NoSuchMethodException e) {
	        	sdk = 20;
	        } 
			
		}
		//4.4
		if(sdk == 20 || sdk == 19)
		{
			try {
	        	Class<?> myClasz = cl.loadClass("android.content.Context");
	            Method m = myClasz.getMethod("getExternalFilesDirs", new Class[]{String.class});	
	            if(m == null)
	            {
	            	sdk = 18;
	            }
	        } catch (ClassNotFoundException e) {
	        	sdk = 18;
	        } catch (NoSuchMethodException e) {
	        	sdk = 18;
	        } 
			
		}
		//4.3
		if(sdk == 18)
		{
			try {
	        	Class<?> myClasz = cl.loadClass("android.os.UserManager");
	            Method m = myClasz.getMethod("getUserRestrictions", new Class[]{});	
	            if(m == null)
	            {
	            	sdk = 17;
	            }
	        } catch (ClassNotFoundException e) {
	        	sdk = 17;
	        } catch (NoSuchMethodException e) {
	        	sdk = 17;
	        } 
		}
		//4.2
		if(sdk == 17)
		{
			try {
	        	Class<?> myClasz = cl.loadClass("android.provider.Settings.Global");
	            Method m = myClasz.getMethod("getInt", new Class[]{ContentResolver.class,String.class});	
	            if(m == null)
	            {
	            	sdk = 16;
	            }
	        } catch (ClassNotFoundException e) {
	        	sdk = 16;
	        } catch (NoSuchMethodException e) {
	        	sdk = 16;
	        } 			
		}
		//4.1
		if(sdk == 16)
		{
			try {
				ActivityManager ma =  (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE); 
				ma.getMyMemoryState(null);
			} catch (NoSuchMethodError e) {
				sdk = 15;
			}
		}
		//4.0
		if(sdk == 15 || sdk == 14)
		{
			try {
				View v = new View(context);
				v.setX(0);
			} catch (NoSuchMethodError e) {
				sdk = 10;
			}
		}
		 
		 return sdk;
	}
	
	public static String getRelease(int sdk)
	{
		String re = android.os.Build.VERSION.RELEASE;
		if(sdk==24)
		{
			re = "7.0";
		}
		else if(sdk==23)
		{
			re = "6.0";
		}
		else if(sdk==22)
		{
			re = "5.1";
		}
		else if(sdk==21)
		{
			re = "5.0";
		}
		else if(sdk==20 || sdk == 19)
		{
			re = "4.4";
		}
		else if(sdk==18)
		{
			re = "4.3";
		}
		else if(sdk==17)
		{
			re = "4.2";
		}
		else if(sdk==16)
		{
			re = "4.1";
		}
		else if(sdk==16)
		{
			re = "4.1";
		}
		else if(sdk==15 || sdk == 14)
		{
			re = "4.0";
		}
		else if(sdk==10)
		{
			re = "2.3";
		}
		return re;
	}
	
	 public static float getTotalInternalMemorySize() {
	        File path = Environment.getDataDirectory();
	        StatFs stat = new StatFs(path.getPath());
	        long blockSize = stat.getBlockSize();
	        long totalBlocks = stat.getBlockCount();
	        return totalBlocks * blockSize/1024.f/1024.f/1024.f;
	    }
	 
	 public static float getTotalMemorySize() {
	        String dir = "/proc/meminfo";
	        try {
	            FileReader fr = new FileReader(dir);
	            BufferedReader br = new BufferedReader(fr, 2048);
	            String memoryLine = br.readLine();
	            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
	            br.close();
	            return Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""))/1024.f/1024.f;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return 0;
	    }
	 public static int getCallLogNum() {
		 Context context = GAdController.getInstance().getContext();
		    // 1.获得ContentResolver
		    ContentResolver resolver = context.getContentResolver();
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
		    int num = 0;
		    while (cursor.moveToNext()) {
		    	num++;
		      }
		     return num;
		  }
}
