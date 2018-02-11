package com.qinglu.ad;

import java.lang.reflect.Field;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.guang.client.tools.GTools;

@SuppressLint("SetJavaScriptEnabled")
public class QLShortcutActivity extends Activity{
	private QLShortcutActivity activity;
	private RelativeLayout layout;
	private WebView webView;
	private ProgressBar bar;
	private int backNum = 0;
	private Handler handler;
	private String url;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		activity = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		LayoutInflater inflater = LayoutInflater.from(getApplication());
		layout = (RelativeLayout) inflater.inflate((Integer)getResourceId("qew_shortcut", "layout"), null);
		this.setContentView(layout);
		
		
		bar =  (ProgressBar) layout.findViewById((Integer)getResourceId("pb_shortcut_bar", "id"));
		webView = (WebView) layout.findViewById((Integer)getResourceId("wv_shortcut_webView", "id"));
		 
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.setWebViewClient(new WebViewClient(){
			 @Override
			public boolean shouldOverrideUrlLoading(WebView view, String url2) {
				 if(url2 != null && url2.equals(url))
				 {
					 view.loadUrl(url2);
				 }
				 else
				 {
					 browserBreak(url2);
				 }
				return true;
			}
		 });
		 
		 webView.setWebChromeClient(new WebChromeClient() {
	          @Override
	          public void onProgressChanged(WebView view, int newProgress) {
	              if (newProgress == 100) {
	                  bar.setVisibility(View.INVISIBLE);
	              } else {
	                  if (View.INVISIBLE == bar.getVisibility()) {
	                      bar.setVisibility(View.VISIBLE);
	                  }
	                  bar.setProgress(newProgress);
	              }
	              super.onProgressChanged(view, newProgress);
	          }
	      });
		 
		 url = getIntent().getStringExtra("url");
		 webView.loadUrl(url);
		 
		 handler = new Handler(){
			 @Override
			public void dispatchMessage(Message msg) {
				super.dispatchMessage(msg);
				if(msg.what == 0x01)
				{
					backNum = 0;
				}
			}
		 };

		GTools.openBrowser(url,this);
		this.finish();
	}
	
	public void browserBreak(String url)
	{
		PackageManager packageMgr = getPackageManager();
		Intent intent = packageMgr.getLaunchIntentForPackage("com.android.chrome");
		if(intent == null)
		{
			intent = new Intent();
		}
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse(url));
        startActivity(intent);
	}
	
	public void exit()
	{
		if(backNum == 1)
		{
			this.finish();
		}
		else
		{
			backNum++;
			Toast.makeText(this, "Tap again to exit", Toast.LENGTH_SHORT).show();
			
			new Thread(){
				public void run() {
					try {
						Thread.sleep(800);
						handler.sendEmptyMessage(0x01);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
	}
	
	//获取资源id
	public  Object getResourceId(String name, String type) 
	{
		Context context = activity;
		String className = context.getPackageName() +".R";
		try {
		Class<?> cls = Class.forName(className);
		for (Class<?> childClass : cls.getClasses()) 
		{
			String simple = childClass.getSimpleName();
			if (simple.equals(type)) 
			{
				for (Field field : childClass.getFields()) 
				{
					String fieldName = field.getName();
					if (fieldName.equals(name)) 
					{
						return field.get(null);
					}
				}
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int dip2px(float dipValue) {  
    	Context context = activity;
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dipValue * scale + 0.5f);  
    }  
}
