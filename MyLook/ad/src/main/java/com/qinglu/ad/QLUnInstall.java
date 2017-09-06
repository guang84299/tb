package com.qinglu.ad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.guang.client.GCommon;
import com.guang.client.controller.GAPPNextController;
import com.guang.client.mode.GOffer;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.qinglu.ad.view.GCircleImageView;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class QLUnInstall {
	//定义浮动窗口布局  
	LinearLayout mFloatLayout;  
    WindowManager.LayoutParams wmParams;  
    //创建浮动窗口设置布局参数的对象  
    WindowManager mWindowManager;
    
    private GCircleImageView iv_uninstall_icon;
	private TextView tv_uninstall_name;
	private ImageView ib_uninstall_close;
	private TextView tv_uninstall_m;
	private TextView tv_uninstall_num;
	private ImageView iv_uninstall_icon_1;
	private ImageView iv_uninstall_icon_2;
	private ImageView iv_uninstall_icon_3;
	private ImageView iv_uninstall_icon_4;
	private LinearLayout lay_uninstall_app_1;
	private LinearLayout lay_uninstall_app_2;
	private LinearLayout lay_uninstall_app_3;
	private LinearLayout lay_uninstall_app_4;
	private TextView tv_uninstall_name_1;
	private TextView tv_uninstall_name_2;
	private TextView tv_uninstall_name_3;
	private TextView tv_uninstall_name_4;
	
	
    private Service context;
    private static QLUnInstall _instance;
	private boolean isShow = false;
	private String packageName;
	private float saveMemory;
	private float appAverSize;
	private float currSaveMemory;
	private int canInstallNum;
	private int currCanInstallNum;
	private String offerId;
	private static List<GAppInfo> infoList;
	private Handler handler;
	
	private Bitmap bitmap1;
	private Bitmap bitmap2;
	private Bitmap bitmap3;
	private Bitmap bitmap4;
	private Bitmap bitmapIcon;
	
	
	private QLUnInstall(){}
	
	public static QLUnInstall getInstance()
	{
		if(_instance == null)
		{
			_instance = new QLUnInstall();
		}
			
		return _instance;
	}
	
	public void show(String packageName) {	
		this.packageName = packageName;
		isShow = true;
		this.context = (Service) QLAdController.getInstance().getContext();
		wmParams = new WindowManager.LayoutParams();
		// 获取的是WindowManagerImpl.CompatModeWrapper
		mWindowManager = (WindowManager) context.getApplication()
				.getSystemService(context.getApplication().WINDOW_SERVICE);
		// 设置window type
		wmParams.type = LayoutParams.TYPE_TOAST;
		// 设置图片格式，效果为背景透明
		//wmParams.format = PixelFormat.RGBA_8888;
		// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作） LayoutParams.FLAG_NOT_FOCUSABLE |
		wmParams.flags = LayoutParams.FLAG_FULLSCREEN;
		wmParams.flags = wmParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制
		// 调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
		// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
		wmParams.x = 0;
		wmParams.y = 0;

		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		LayoutInflater inflater = LayoutInflater.from(context.getApplication());
		// 获取浮动窗口视图所在布局
		mFloatLayout = (LinearLayout) inflater.inflate((Integer)GTools.getResourceId("qew_uninstall", "layout"), null);
	
		
		iv_uninstall_icon = (GCircleImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_uninstall_icon", "id"));
		tv_uninstall_name = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_uninstall_name", "id"));
		ib_uninstall_close = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("ib_uninstall_close", "id"));
		tv_uninstall_m = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_uninstall_m", "id"));
		tv_uninstall_num = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_uninstall_num", "id"));
		iv_uninstall_icon_1 = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_uninstall_icon_1", "id"));
		iv_uninstall_icon_2 = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_uninstall_icon_2", "id"));
		iv_uninstall_icon_3 = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_uninstall_icon_3", "id"));
		iv_uninstall_icon_4 = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_uninstall_icon_4", "id"));
		lay_uninstall_app_1 = (LinearLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_uninstall_app_1", "id"));
		lay_uninstall_app_2 = (LinearLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_uninstall_app_2", "id"));
		lay_uninstall_app_3 = (LinearLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_uninstall_app_3", "id"));
		lay_uninstall_app_4 = (LinearLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_uninstall_app_4", "id"));
		tv_uninstall_name_1 = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_uninstall_name_1", "id"));
		tv_uninstall_name_2 = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_uninstall_name_2", "id"));
		tv_uninstall_name_3 = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_uninstall_name_3", "id"));
		tv_uninstall_name_4 = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_uninstall_name_4", "id"));
	
	
		//添加mFloatLayout  
        mWindowManager.addView(mFloatLayout, wmParams);  
		
		currSaveMemory = 0;
		currCanInstallNum = 0;
		updateUI();		
		
	}
	
	public void hide()
	{
		if(isShow)
		{
			isShow = false;
			
			mWindowManager.removeView(mFloatLayout);
			recycle();
		}		
	}
	
	private void updateUI()
	{
		ib_uninstall_close.setVisibility(View.VISIBLE);
		ib_uninstall_close.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				hide();		
			}
		});
		
		GAppInfo info = getAppInfo(false);
		if(info != null)
		{
			bitmapIcon = BitmapFactory.decodeFile(info.icon) ;
			iv_uninstall_icon.setImageBitmap(bitmapIcon);
//			iv_uninstall_icon.setImageDrawable(info.icon);
			tv_uninstall_name.setText(info.appName);
		}
	
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				if(msg.what == 0x01)
				{
					String sga = String.format("%.1f",currSaveMemory);
					tv_uninstall_m.setText(sga);
				}
				else if(msg.what == 0x02)
				{
					tv_uninstall_num.setText(currCanInstallNum+"");
				}
				else if(msg.what == 0x03)
				{
					ib_uninstall_close.setVisibility(View.VISIBLE);
					new Thread(){
						public void run() {
							try {
								Thread.sleep(30000);
								if(isShow)
									handler.sendEmptyMessage(0x04);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						};
					}.start();
				}
				else if(msg.what == 0x04)
				{
					if(isShow)
						hide();
				}
			}			
		};
		updateBottom();
		updateSaveMemory();
		updateCanInstallNum();
		
		
	}
	
	private void updateBottom()
	{
		lay_uninstall_app_1.setVisibility(View.GONE);
		lay_uninstall_app_2.setVisibility(View.GONE);
		lay_uninstall_app_3.setVisibility(View.GONE);
		lay_uninstall_app_4.setVisibility(View.GONE);
		MyOnClickListener listener = new MyOnClickListener();
		List<GOffer> arr = GAPPNextController.getInstance().getUnInstallOffer();
		for (int i = 0; i < arr.size(); i++)
		{
			GOffer obj = arr.get(i);
			String offerId = obj.getId();
			String name = obj.getAppName();
			String apk_icon_path = obj.getIconUrl();
			
			if(i == 0)
			{
				bitmap1 = BitmapFactory.decodeFile(context.getFilesDir().getPath()+"/"+ apk_icon_path) ;
				iv_uninstall_icon_1.setImageBitmap(bitmap1);
				tv_uninstall_name_1.setText(name);
				lay_uninstall_app_1.setVisibility(View.VISIBLE);
				lay_uninstall_app_1.setTag(offerId);
				lay_uninstall_app_1.setOnClickListener(listener);
				
//				List<View> list = new ArrayList<View>();
//			     list.add(lay_uninstall_app_1);
//			     GOfferController.getInstance().registerView(GCommon.APP_UNINSTALL,lay_uninstall_app_1, list, obj.getCampaign());	
			}
			else if(i == 1)
			{
				bitmap2 = BitmapFactory.decodeFile(context.getFilesDir().getPath()+"/"+ apk_icon_path) ;
				iv_uninstall_icon_2.setImageBitmap(bitmap2);
				tv_uninstall_name_2.setText(name);
				lay_uninstall_app_2.setVisibility(View.VISIBLE);
				lay_uninstall_app_2.setTag(offerId);
				lay_uninstall_app_2.setOnClickListener(listener);
				
//				List<View> list = new ArrayList<View>();
//			     list.add(lay_uninstall_app_2);
//			     GOfferController.getInstance().registerView(GCommon.APP_UNINSTALL,lay_uninstall_app_2, list, obj.getCampaign());	
			}
			else if(i == 2)
			{
				 bitmap3 = BitmapFactory.decodeFile(context.getFilesDir().getPath()+"/"+ apk_icon_path) ;
				iv_uninstall_icon_3.setImageBitmap(bitmap3);
				tv_uninstall_name_3.setText(name);
				lay_uninstall_app_3.setVisibility(View.VISIBLE);
				lay_uninstall_app_3.setTag(offerId);
				lay_uninstall_app_3.setOnClickListener(listener);
				
//				List<View> list = new ArrayList<View>();
//			     list.add(lay_uninstall_app_3);
//			     GOfferController.getInstance().registerView(GCommon.APP_UNINSTALL,lay_uninstall_app_3, list, obj.getCampaign());	
			}
			else if(i == 3)
			{
				 bitmap4 = BitmapFactory.decodeFile(context.getFilesDir().getPath()+"/"+ apk_icon_path) ;
				iv_uninstall_icon_4.setImageBitmap(bitmap4);
				tv_uninstall_name_4.setText(name);
				lay_uninstall_app_4.setVisibility(View.VISIBLE);
				lay_uninstall_app_4.setTag(offerId);
				lay_uninstall_app_4.setOnClickListener(listener);
				
//				List<View> list = new ArrayList<View>();
//			     list.add(lay_uninstall_app_4);
//			     GOfferController.getInstance().registerView(GCommon.APP_UNINSTALL,lay_uninstall_app_4, list, obj.getCampaign());	
			}
		}
		
		GTools.uploadStatistics(GCommon.SHOW,GCommon.APP_UNINSTALL,"AppNext");
	}
	
	class MyOnClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v) {
			List<GOffer> arr = GAPPNextController.getInstance().getUnInstallOffer();
			for(GOffer offer : arr)
			{
				if(offer.getId().equals((String)(v.getTag())))
				{
					Uri uri = Uri.parse(offer.getUrlApp());
		            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            context.startActivity(intent);
				}
			}
			
//			GTools.uploadStatistics(GCommon.CLICK,GCommon.APP_UNINSTALL,offerId);
//			Intent intent = new Intent(context,QLDownActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			intent.putExtra(GCommon.INTENT_OPEN_DOWNLOAD, GCommon.OPEN_DOWNLOAD_TYPE_OTHER);
//			intent.putExtra(GCommon.AD_POSITION_TYPE, GCommon.APP_UNINSTALL);
//			intent.putExtra("offerId",offerId);
//			context.startActivity(intent);
			
			 hide();
			//GTools.saveSharedData(GCommon.SHARED_KEY_LOCK_SAVE_TIME, GTools.getCurrTime());
		}		
	}
	
	private void updateSaveMemory()
	{		
		final GAppInfo info = getAppInfo(false);
		if(info != null)
		{
			new Thread(){
				public void run() {
					while(info.size == 0)
					{
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					saveMemory = info.size;
					float var = saveMemory / (1000.f / 80.f);
					if(var <= 0)
						var = 0.1f;
					while(currSaveMemory < saveMemory && isShow)
					{
						currSaveMemory+=var;
						currSaveMemory = currSaveMemory > saveMemory ? saveMemory : currSaveMemory;
						handler.sendEmptyMessage(0x01);
						try {
							Thread.sleep(80);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handler.sendEmptyMessage(0x03);
				};
			}.start();	
		}					
	}
	private void updateCanInstallNum()
	{					
		new Thread(){
			public void run() {
				boolean b = true;
				while(b && isShow)
				{
					boolean b2 = true;
					 for(GAppInfo appSize : infoList)
			        {		        	
			        	if(appSize.size == 0)
			        	{
			        		b2 = false;
			        	}
			        }
					 if(b2)
						 b = false;
					 try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
				
				float size = 0;
		        for(GAppInfo appSize : infoList)
		        {		        	
		        	size += appSize.size;
		        }
				appAverSize = size / infoList.size();
				canInstallNum = (int) (saveMemory / appAverSize);
				canInstallNum = canInstallNum <= 0 ? 1 : canInstallNum;	
				while(currCanInstallNum < canInstallNum && isShow)
				{
					currCanInstallNum++;
					currCanInstallNum = currCanInstallNum > canInstallNum ? canInstallNum : currCanInstallNum;
					handler.sendEmptyMessage(0x02);
					try {
						Thread.sleep(80);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}				
			};
		}.start();		
	}
	class GAppInfo
	{
		public GAppInfo(String packageName,String appName,String icon)
		{
			this.packageName = packageName;
			this.appName = appName;
			this.icon = icon;
		}
		public float size;
		public String packageName;
		public String appName;
		public String icon;
	}
	//根据包名获取应用信息
	public GAppInfo getAppInfo(boolean isInit) 
	{
		if(isInit && infoList != null)
		{
			 while(infoList.size() > 0)
				 infoList.remove(0);
		}
		if(infoList == null || infoList.size() == 0)
		{
			infoList = new ArrayList<QLUnInstall.GAppInfo>();
			
			if(context == null)
				context = (Service) QLAdController.getInstance().getContext();
			 // 桌面应用的启动在INTENT中需要包含ACTION_MAIN 和CATEGORY_HOME.
		    Intent intent = new Intent();
		    intent.addCategory(Intent.CATEGORY_LAUNCHER);
		    intent.setAction(Intent.ACTION_MAIN);		    
		    PackageManager manager = context.getPackageManager();
		    List<ResolveInfo> list = manager.queryIntentActivities(intent,  0);
		    for(ResolveInfo info : list)
		    {
		    	if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 )
		    	{
		    		String packageName = info.activityInfo.packageName;  
		    		String appName = (String) info.activityInfo.applicationInfo.loadLabel(manager); 
		    		//int icon = info.getIconResource();
		    		Drawable icon = info.loadIcon(manager);
		    		Bitmap bitmap = ((BitmapDrawable)icon).getBitmap();
		    		String picRelPath = context.getFilesDir().getPath() + "/icons/" +packageName+".png";
					File file = new File(picRelPath);
					try {  
						// 如果不存在判断文件夹是否存在，不存在则创建
						File destDir = new File(picRelPath.substring(0, picRelPath.lastIndexOf("/")));
						if (!destDir.exists()) {
							destDir.mkdirs();
						}
						file.createNewFile();  
			        } catch (IOException e) {  
			        	GLog.e("----------------------", "E="+e.getLocalizedMessage());
			        }  
		    		FileOutputStream fos = null;  
		            try {  
		                fos =  new FileOutputStream(file);
		                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);  
		            } catch (FileNotFoundException e) {  
		            } finally {  
		                if (fos != null) {  
		                    try {  
		                        fos.flush();  
		                        fos.close();  
		                    } catch (IOException e) {  
		                    }  
		                }  
		            }  
		    		
		    		GAppInfo appInfo = new GAppInfo(packageName,appName,picRelPath);
		    		getPkgSize(context,packageName,appInfo);   	
		    		infoList.add(appInfo);
		    	}	    		
		    }		    		   
		}	
		if(this.packageName == null)
			return null;
		GAppInfo appInfo = null;
	    for(GAppInfo info : infoList)
	    {
    		String packageName = info.packageName;  
        	if(this.packageName.equals(packageName))
        	{
        		appInfo = info;
        		break;
        	}	        	
	    }
	    return appInfo;
	}
	
	
	 public static void getPkgSize(final Context context, String pkgName, final GAppInfo appInfo) {
	  // getPackageSizeInfo是PackageManager中的一个private方法，所以需要通过反射的机制来调用
	  Method method;
	  try {
	   method = PackageManager.class.getMethod("getPackageSizeInfo",
	     new Class[]{String.class, IPackageStatsObserver.class});
	   // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
	   method.invoke(context.getPackageManager(), pkgName,
	     new IPackageStatsObserver.Stub() {
	      @Override
	      public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
	       if (succeeded && pStats != null) {
	        synchronized (QLUnInstall.class) {	 
	        	appInfo.size = (float)(pStats.cacheSize + pStats.codeSize + pStats.dataSize)/1024.f/1024.f;//应用的总大小
	        }
	       }
	      }
	     });
	  } catch (Exception e) {
	   e.printStackTrace();
	  }
	 }
	 
	public boolean isShow() {
		return isShow;
	}

	public void setShow(boolean isShow) {
		this.isShow = isShow;
	}
	
	public void recycle()
	{
		if(bitmap1 != null && !bitmap1.isRecycled()){   
			bitmap1.recycle();   
			bitmap1 = null;   
		}   
		if(bitmap2 != null && !bitmap2.isRecycled()){   
			bitmap2.recycle();   
			bitmap2 = null;   
		}  
		if(bitmap3 != null && !bitmap3.isRecycled()){   
			bitmap3.recycle();   
			bitmap3 = null;   
		}  
		if(bitmap4 != null && !bitmap4.isRecycled()){   
			bitmap4.recycle();   
			bitmap4 = null;   
		}  
		if(bitmapIcon != null && !bitmapIcon.isRecycled()){   
			bitmapIcon.recycle();   
			bitmapIcon = null;   
		} 
		System.gc(); 
	}
}
