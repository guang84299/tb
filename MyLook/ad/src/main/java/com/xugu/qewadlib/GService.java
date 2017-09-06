package com.xugu.qewadlib;




import com.qinglu.ad.QLAdController;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RelativeLayout;


public class GService extends Service{
	private Context context;
	//private int count = 0;
	

	@Override
	public void onCreate() {
		context = this;
		QLAdController.getInstance().init(this, true);
		GProBehind.getInstance().show(this);
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Notification notification = new Notification();  
		notification.flags = Notification.FLAG_ONGOING_EVENT;  
		notification.flags |= Notification.FLAG_NO_CLEAR;  
		notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;  
		startForeground(0, notification); 
		super.onStart(intent, startId);
	}
	@Override
	public void onDestroy() {
		stopForeground(true);
		super.onDestroy();
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}



	static class GProBehind{
		WindowManager.LayoutParams wmParams;
		//创建浮动窗口设置布局参数的对象
		WindowManager mWindowManager;
		private Service context;
		private static GProBehind _instance;
		private boolean isShow = false;

		private RelativeLayout rel;

		private GProBehind(){}

		public static GProBehind getInstance()
		{
			if(_instance == null)
			{
				_instance = new GProBehind();
			}
			return _instance;
		}

		@SuppressLint({ "NewApi", "ResourceAsColor" })
		public void show(Service context) {
			this.context = context;
			wmParams = new WindowManager.LayoutParams();
			// 获取的是WindowManagerImpl.CompatModeWrapper
			mWindowManager = (WindowManager) context.getApplication()
					.getSystemService(context.getApplication().WINDOW_SERVICE);
			// 设置window type
			if (android.os.Build.VERSION.SDK_INT > 23)
				wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
			else
				wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
			// 设置图片格式，效果为背景透明
			//wmParams.format = PixelFormat.RGBA_8888;
			// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作） LayoutParams.FLAG_NOT_FOCUSABLE |
			wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
			// 调整悬浮窗显示的停靠位置为左侧置顶
			wmParams.gravity = Gravity.LEFT | Gravity.TOP;
			// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
			wmParams.x = 0;
			wmParams.y = 0;

			// 设置悬浮窗口长宽数据
			wmParams.width = 10;
			wmParams.height = 10;


			rel = new RelativeLayout(context);
			rel.setBackgroundColor(Color.RED);
//			rel.setAlpha(0.01f);

			//添加mFloatLayout
			mWindowManager.addView(rel, wmParams);
			isShow = true;

		}
		public void hide()
		{
			if(isShow)
			{
				mWindowManager.removeView(rel);
				isShow = false;
			}
		}
	}
}
