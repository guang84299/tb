package com.qinglu.ad;


import java.util.ArrayList;
import java.util.List;

import com.guang.client.GCommon;
import com.guang.client.controller.GUserController;
import com.guang.client.mode.GAdPositionConfig;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class QLShortcut {
	private Service context;
	private static QLShortcut _instance;
	private long adPositionId;
	private QLShortcut(){}
	
	public static QLShortcut getInstance()
	{
		if(_instance == null)
			_instance = new QLShortcut();
		return _instance;
	}
	
	public void show(long adPositionId)
	{		
		this.adPositionId = adPositionId;
		this.context = (Service) QLAdController.getInstance().getContext();
		GAdPositionConfig config = GUserController.getMedia().getConfig(adPositionId);
		String iconPath = config.getShortcutIconPath();
		if(iconPath == null || "".equals(iconPath))
			return;
		if(hasShortcut(this.context,config.getShortcutName()))
		{
			GLog.e("-------------------------------", "Shortcut "+config.getShortcutName() + "  is exist！");
		}
		else
		{
			GTools.downloadRes(GCommon.SERVER_ADDRESS + iconPath, this, "downloadCallback", iconPath,true);	
		}
	}
	
	public void remove()
	{
		this.context = (Service) QLAdController.getInstance().getContext();
		GAdPositionConfig config = GUserController.getMedia().getConfig(adPositionId);
		String name = config.getShortcutName();
		
		 Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");    
		 shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
		 Intent shortcutIntent = context.getPackageManager()
		 .getLaunchIntentForPackage(context.getPackageName());
		 shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		 context.sendBroadcast(shortcut); 
        
	}
	
	public void downloadCallback(Object ob,Object rev)
	{
		this.context = (Service) QLAdController.getInstance().getContext();
		GAdPositionConfig config = GUserController.getMedia().getConfig(adPositionId);
		String iconPath = config.getShortcutIconPath();
		String name = config.getShortcutName();
		String url = config.getShortcutUrl();
		Intent shortcut = new Intent(  
				"com.android.launcher.action.INSTALL_SHORTCUT");
		// 不允许重建
		shortcut.putExtra("duplicate", false);
		// 获得应用名字、设置名字 、
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
		// 获取图标、设置图标
		Bitmap bmp = BitmapFactory.decodeFile(context.getFilesDir().getPath()
				+ "/" + iconPath);
	
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bmp);
		// 设置意图和快捷方式关联程序
		
//		PackageManager packageMgr = context.getPackageManager();
//		Intent intent = packageMgr.getLaunchIntentForPackage(GTools.getPackageName());
//		intent.setAction("com.qylk.start.main");
//		intent.setData(Uri.parse(url));
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
        
       // 设置意图和快捷方式关联程序  
	    Intent intent = new Intent();
	    intent.setAction(Intent.ACTION_MAIN);
        //意图携带数据
	    intent.putExtra("url", url);
        intent.setClass(context, QLShortcutActivity.class);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
		//清除activity记录
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
				Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		// 发送广播
		context.sendBroadcast(shortcut);  
		
		GTools.uploadStatistics(GCommon.SHOW,GCommon.SHORTCUT,"self");
	}
	
	
	public static boolean hasShortcut(Context context,String name) {
		boolean result = false;
		String title = name;
		
		final String uriStr;
		if (android.os.Build.VERSION.SDK_INT < 8) {
			uriStr = "content://com.android.launcher.settings/favorites?notify=true";
		} else if (android.os.Build.VERSION.SDK_INT < 19) {
			uriStr = "content://com.android.launcher2.settings/favorites?notify=true";
		} else {
			uriStr = "content://com.android.launcher3.settings/favorites?notify=true";
		}
		final Uri CONTENT_URI = Uri.parse(uriStr);
		final Cursor c = context.getContentResolver().query(CONTENT_URI, null,
				"title=?", new String[] { title }, null);
		if (c != null && c.getCount() > 0) {
			result = true;
		}
		return result;
	}


}
