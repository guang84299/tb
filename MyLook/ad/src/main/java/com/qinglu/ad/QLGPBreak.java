package com.qinglu.ad;









import com.guang.client.GCommon;
import com.guang.client.controller.GAPPNextController;
import com.guang.client.controller.GAffiliateController;
import com.guang.client.controller.GGpController;
import com.guang.client.controller.GMIController;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

@SuppressLint("SetJavaScriptEnabled")
public class QLGPBreak{ 
    WindowManager.LayoutParams wmParams;  
    //创建浮动窗口设置布局参数的对象  
    WindowManager mWindowManager;
    private Service context;
    private static QLGPBreak _instance;
	private boolean isShow = false;
		
	private String urls;
	private String target;
	private WebView webView;
	private RelativeLayout view;

	private String type;

	private QLGPBreak(){}
	
	public static QLGPBreak getInstance()
	{
		if(_instance == null)
		{
			_instance = new QLGPBreak();
		}
		return _instance;
	}
	
	@SuppressLint("NewApi")
	public void show(final String type,String packageName) {
		this.type = type;
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
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN;
		// 调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
		wmParams.x = 0;
		wmParams.y = 0;

		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		wmParams.height = 1;

		webView = new WebView(context);
		webView.setAlpha(0.f);
		//添加mFloatLayout  
        mWindowManager.addView(webView, wmParams);  
		isShow = true;
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		webView.setWebViewClient(new WebViewClient(){
			 @Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				 Log.e("--------------","url="+url);
				 if(url != null && (url.startsWith("market:") || url.contains("play.google.com/store/apps/details")))
				 {
					 target = url;
					 if(!"off".equals(type) && !"offbrush".equals(type) && !"self_gp".equals(type) && !"affi_gp".equals(type))
					 	show2();
					 else
					 {
						 hide();
					 }
					 return false;
				 }
				 else
				 {
					 view.loadUrl(url);
					return true;
				 }
				
			}
		 });

		if("self_gp".equals(type))
			urls = GGpController.getInstance().getGpOffer().getTrackUrl();
		else if("affi_gp".equals(type))
			urls = GAffiliateController.getInstance().getGpOffer().getTrackUrl();
		else if("mi".equals(type))
			urls = GMIController.getInstance().getGpOffer().getUrlApp();
		else if("off".equals(type))
			urls = GMIController.getInstance().getGpOffer().getUrlApp();
		else if("offbrush".equals(type))
			urls = GMIController.getInstance().getGpOffOffer().getUrlApp();
		else
			urls = GAPPNextController.getInstance().getGpOffer().getUrlApp();


		target = null;
		if("self_gp".equals(type))
		{
			target = GGpController.getInstance().getGpOffer().getGpUrl();
			openGP(target);
		}
		else if("affi_gp".equals(type))
		{
			target = GAffiliateController.getInstance().getGpOffer().getGpUrl();
			openGP(target);
		}


		if("self_gp".equals(type))
		{
			GTools.uploadStatistics(GCommon.SHOW,GCommon.GP_BREAK,GGpController.getInstance().getGpOffer().getId());
		}
		else if("affi_gp".equals(type))
		{
			GTools.uploadStatistics(GCommon.SHOW,GCommon.GP_BREAK,GAffiliateController.getInstance().getGpOffer().getId());
		}
		else if("mi".equals(type))
			GTools.uploadStatistics(GCommon.SHOW,GCommon.GP_BREAK,"mi");
		else if("off".equals(type))
		{
			String offerType = GMIController.getInstance().getGpOffOffer().getOfferType();
			if("pingStart".equals(offerType))
				GTools.uploadStatistics(GCommon.SHOW,GCommon.OFF_GP_BREAK,"pingoff");
			else
				GTools.uploadStatistics(GCommon.SHOW,GCommon.OFF_GP_BREAK,"mioff");
		}
		else if("offbrush".equals(type))
		{

		}
		else
			GTools.uploadStatistics(GCommon.SHOW,GCommon.GP_BREAK,"appNext");

		Log.e("--------------","urls="+urls);
		if(urls != null && !"".equals(urls))
		{
			webView.loadUrl(urls);
		}
		else
		{
			hide();
		}
		
	}
	
	@SuppressLint("NewApi")
	private void show2()
	{
		 hide();
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
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_FULLSCREEN;
		// 调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
		wmParams.x = 0;
		wmParams.y = 0;

		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;

		view = new RelativeLayout(context);
	 	RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
	 	layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
	 	view.setLayoutParams(layoutParams);
	 	view.setAlpha(0.f);
		//添加mFloatLayout  
        mWindowManager.addView(view, wmParams);  
		isShow = true;
		
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(target != null)
				{
					Uri uri = Uri.parse(target);

					PackageManager packageMgr = context.getPackageManager();
					Intent intent = packageMgr.getLaunchIntentForPackage("com.android.vending");
					if(intent == null)
						intent = new Intent(Intent.ACTION_VIEW, uri);
					else
						intent.setAction(Intent.ACTION_VIEW);


					intent.addCategory(Intent.CATEGORY_DEFAULT);
					intent.setData(uri);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 			 intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		             context.startActivity(intent);
		             
		             int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_GP_BREAK_NUM, 0);
					 GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_NUM, num+1);
					 GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_TIME,GTools.getCurrTime());	
				}
				hide2();
			}
		});
	}

	private void openGP(String target)
	{
		Uri uri = Uri.parse(target);

		PackageManager packageMgr = context.getPackageManager();
		Intent intent = packageMgr.getLaunchIntentForPackage("com.android.vending");
		if(intent == null)
			intent = new Intent(Intent.ACTION_VIEW, uri);
		else
			intent.setAction(Intent.ACTION_VIEW);


		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		context.startActivity(intent);

		int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_GP_BREAK_NUM, 0);
		GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_NUM, num+1);
		GTools.saveSharedData(GCommon.SHARED_KEY_GP_BREAK_TIME,GTools.getCurrTime());
	}
	
	public void hide()
	{
		if(isShow)
		{
			mWindowManager.removeView(webView);
			isShow = false;
		}		
	}
	public void hide2()
	{
		if(isShow)
		{
			mWindowManager.removeView(view);
			isShow = false;
		}		
	}
	
}
