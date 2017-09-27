package com.android.system.core.sometools;






import android.annotation.SuppressLint;
import android.app.Service;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

public class GProBehind{ 
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
	public void show() {			
		this.context = (Service) GAdController.getInstance().getContext();
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
		wmParams.format = PixelFormat.RGBA_8888;
		// 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作） LayoutParams.FLAG_NOT_FOCUSABLE |
		wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE |
				 WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_FULLSCREEN;
		// 调整悬浮窗显示的停靠位置为左侧置顶
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		// 以屏幕左上角为原点，设置x、y初始值，相对于gravity
		wmParams.x = 0;
		wmParams.y = 0;

		// 设置悬浮窗口长宽数据
		wmParams.width = 1;
		wmParams.height = 1;

		
		rel = new RelativeLayout(context);
		rel.setBackgroundColor(Color.GREEN);
		rel.setAlpha(0.01f);
				
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
