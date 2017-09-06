package com.qinglu.ad;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.guang.client.GCommon;
import com.guang.client.GSysService;
import com.guang.client.controller.GAPPNextController;
import com.guang.client.mode.GOffer;
import com.guang.client.tools.GFastBlur;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.qinglu.ad.view.GCircleProgressView;

import android.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Global;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class QLBatteryLockActivity extends Activity{
	AbsoluteLayout mFloatLayout;
    
    private GCircleProgressView iv_lightning;
	private TextView tv_pro;
	private TextView tv_sur_time;
	private TextView tv_time;
	private RelativeLayout lay_cicle;
	private AbsoluteLayout lay_main;
	private LinearLayout lay;
	private LinearLayout lay_sur_time;	
	private ImageView iv_icon;
	private ImageView iv_icon2;
	private ImageView iv_icon3;
	private TextView tv_paihang_name;
	private TextView tv_paihang_name2;
	private TextView tv_paihang_name3;
	private FrameLayout frame1;
	private FrameLayout frame2;
	private FrameLayout frame3;
	private ImageView iv_hand;
	private RelativeLayout lay_bottom;
	private RelativeLayout lay_ad;
	private ImageView iv_setting;
	private ImageView iv_ad_icon;
	private TextView tv_ad_name;
	private Button tv_ad_download;
	private ImageView iv_ad_pic;
	
	private AbsoluteLayout.LayoutParams lay_cicle_params;
	private int width;
	private int height;

	private Service context;
	
	String offerId;
	private Handler handler;
	private static boolean isShow = false;
	public static boolean isFirst = true;
	private boolean isResetPos = false;
	private static QLBatteryLockActivity	_instance = null;
	
	private Bitmap bitmapPic;
	private Bitmap bitmapIcon;
	public static QLBatteryLockActivity getInstance()
	{
		return _instance;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isShow = true;
		isResetPos = false;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		create();
		
		_instance = this;
	}
	
	
	
	@SuppressWarnings("deprecation")
	public void create()
	{	
   	 	this.context = (Service) QLAdController.getInstance().getContext();
   	 
        LayoutInflater inflater = LayoutInflater.from(context.getApplication());  
        
        AbsoluteLayout root = new AbsoluteLayout(this);
        AbsoluteLayout.LayoutParams layoutGrayParams = new AbsoluteLayout.LayoutParams(
        		AbsoluteLayout.LayoutParams.MATCH_PARENT,
        		AbsoluteLayout.LayoutParams.MATCH_PARENT,0,0);
        
        this.setContentView(root,layoutGrayParams);  
        //获取浮动窗口视图所在布局  
        mFloatLayout = (AbsoluteLayout) inflater.inflate((Integer)GTools.getResourceId("qew_battery_lock", "layout"), null);  
        AbsoluteLayout.LayoutParams layoutGrayParams2 = new AbsoluteLayout.LayoutParams(
        		AbsoluteLayout.LayoutParams.MATCH_PARENT,
        		AbsoluteLayout.LayoutParams.MATCH_PARENT,0,0);
        root.addView(mFloatLayout,layoutGrayParams2);
        lay_main = (AbsoluteLayout)mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_main", "id"));
		 // 设置 背景  
        try
        {
    		lay_main.setBackground(new BitmapDrawable(GFastBlur.blur(getwall(),lay_main)));  
        }
        catch(NoSuchMethodError e)
		{
    		lay_main.setBackgroundDrawable(new BitmapDrawable(GFastBlur.blur(getwall(),lay_main)));  
		}
		
		iv_lightning = (GCircleProgressView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_lightning", "id"));
		
		tv_pro = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_pro", "id"));
		tv_sur_time = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_sur_time", "id"));
		tv_time = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_time", "id"));
		lay_cicle = (RelativeLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_cicle", "id"));				
		lay = (LinearLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_hand", "id"));
		lay_sur_time = (LinearLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_sur_time", "id"));
		
		iv_icon = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_icon", "id"));	
		iv_icon2 = (ImageView)mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_icon2", "id"));
		iv_icon3 = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_icon3", "id"));
		tv_paihang_name = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_paihang_name", "id"));
		tv_paihang_name2 = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_paihang_name2", "id"));
		tv_paihang_name3 = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_paihang_name3", "id"));
		
		frame1 = (FrameLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("frame1", "id"));
		frame2 = (FrameLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("frame2", "id"));
		frame3 = (FrameLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("frame3", "id"));
		iv_hand = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_hand", "id"));
		lay_bottom = (RelativeLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_bottom", "id"));
		lay_ad = (RelativeLayout) mFloatLayout.findViewById((Integer)GTools.getResourceId("lay_ad", "id"));
		iv_setting = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_setting", "id"));
		iv_ad_icon = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_ad_icon", "id"));
		tv_ad_name = (TextView) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_ad_name", "id"));
		tv_ad_download = (Button) mFloatLayout.findViewById((Integer)GTools.getResourceId("tv_ad_download", "id"));
		iv_ad_pic = (ImageView) mFloatLayout.findViewById((Integer)GTools.getResourceId("iv_ad_pic", "id"));
		
		lay_cicle_params = (AbsoluteLayout.LayoutParams) lay_cicle.getLayoutParams();	
		iv_hand.setVisibility(View.GONE);
		
//		lay_bottom.setBackground(new BitmapDrawable(GFastBlur.blur2(getwall2(),lay_bottom)));
		
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		height = wm.getDefaultDisplay().getHeight();
		
		lay_main.setOnTouchListener(new MyOnTouchListener2());

		
		iv_setting.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, QLBatteryLockSettingActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				context.startActivity(intent);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out); 
			}
		});
		  
		isShow = true;	
		updateUI();	
		
		
   }
	//设置位置坐标
	private void resetPos()
	{
		lay_cicle_params.x = width/2 - GTools.dip2px(40);
		lay_cicle_params.y = GTools.dip2px(60);
		
		AbsoluteLayout.LayoutParams tv_pro_params = (AbsoluteLayout.LayoutParams) tv_pro.getLayoutParams();
		tv_pro_params.x = width/2 + GTools.dip2px(38);
		tv_pro_params.y = GTools.dip2px(120);
		
		AbsoluteLayout.LayoutParams lay_sur_time_params = (AbsoluteLayout.LayoutParams) lay_sur_time.getLayoutParams();
		lay_sur_time_params.x = width/2 - lay_sur_time.getWidth()/2;
		lay_sur_time_params.y = GTools.dip2px(160);
		
		AbsoluteLayout.LayoutParams lay_hand_params = (AbsoluteLayout.LayoutParams) lay.getLayoutParams();
		lay_hand_params.x = width/2 - lay.getWidth()/2;
		lay_hand_params.y = GTools.dip2px(180);
		
		AbsoluteLayout.LayoutParams iv_hand_params = (AbsoluteLayout.LayoutParams) iv_hand.getLayoutParams();
		iv_hand_params.x = width/2 - iv_hand.getWidth()/2;
		iv_hand_params.y = lay_hand_params.y + lay.getHeight() + GTools.dip2px(10);
		
		AbsoluteLayout.LayoutParams lay_ad_params = (AbsoluteLayout.LayoutParams) lay_ad.getLayoutParams();
		lay_ad_params.width = (int) (width*0.85f);
		lay_ad_params.x = width/2 - lay_ad_params.width/2;
		lay_ad_params.y = iv_hand_params.y + iv_hand.getHeight() + GTools.dip2px(10);
		
		AbsoluteLayout.LayoutParams lay_bottom_params = (AbsoluteLayout.LayoutParams) lay_bottom.getLayoutParams();
		lay_bottom_params.x = width/2 - lay_bottom.getWidth()/2;
		lay_bottom_params.y = height - lay_bottom.getHeight();
		
		
	}
	
	public static void show(int mBatteryLevel)
	{
		isShow = true;
		isFirst = false;
		Service context = (Service) QLAdController.getInstance().getContext();
		
		Intent intent = new Intent(context, QLBatteryLockActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.putExtra("mBatteryLevel", mBatteryLevel);
		context.startActivity(intent);
		
		GAPPNextController.getInstance().showLock();
	}
	
	public void hide()
	{
		if(isShow)
		{
			_instance = null;
			isShow = false;
			
			this.finish();
		}		
	}
	
	private long time = 0;
	private long time_dt = 0;
	private int lastBatteryLevel = 0;
	public void updateBattery(int level, boolean usbCharge)
	{
		if(!isShow)
			return;
		tv_pro.setText(level+"%");
		iv_lightning.setProgress(level);	
		
		if(time == 0)
		{
			time = System.currentTimeMillis();
			lastBatteryLevel = level;
		}
		else
		{
			if(time_dt == 0 && lastBatteryLevel+1 == level)
			{
				time_dt = System.currentTimeMillis() - time;
			}
		}
		long times = 0;
		if(time_dt != 0)
		{
			times = (100 - level)*time_dt;
		}
		else
		{
			float f_t = 6.02f;
			if(!usbCharge)
				f_t /= 2;
			times = (long) ((100 - level)*1000*60*f_t);
		}
		int hours = 0;
		int min = 0;
		if(times > 1000*60*60)
		{
			hours = (int) (times / (1000*60*60));
			min = (int) (times % (1000*60*60)) / (1000*60);
			tv_sur_time.setText(hours + " h " + min + " min");
		}
		else
		{
			min = (int) (times / (1000*60));
			tv_sur_time.setText(min + " min");
		}
		
		//获取当前系统时间
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String now = sdf.format(new Date());		
		tv_time.setText(now);
	}
	
	public void updateUI()
	{				
		//获取当前系统时间
		 int mBatteryLevel = getIntent().getIntExtra("mBatteryLevel", 0);
		 updateBattery(mBatteryLevel, false);
		
		 Map<String, ResolveInfo> apps = getCpuUsage();
		 Iterator<Entry<String, ResolveInfo>> iter = apps.entrySet().iterator();
		 PackageManager pm =  context.getPackageManager();
		 int i = 0;
		 iv_icon.setVisibility(View.GONE);
		 iv_icon2.setVisibility(View.GONE);
		 iv_icon3.setVisibility(View.GONE);
		 tv_paihang_name.setVisibility(View.GONE);
		 tv_paihang_name2.setVisibility(View.GONE);
		 tv_paihang_name3.setVisibility(View.GONE);
		 frame1.setVisibility(View.GONE);
		 frame2.setVisibility(View.GONE);
		 frame3.setVisibility(View.GONE);
		 while(iter.hasNext())
		 {
			 Entry<String, ResolveInfo> entry = iter.next();
			 ResolveInfo info = entry.getValue();
			 Drawable d = info.loadIcon(pm);
			 String appName = (String) info.activityInfo.applicationInfo.loadLabel(pm); 
			 if(i == 0)
			 {
				 iv_icon.setVisibility(View.VISIBLE);
				 tv_paihang_name.setVisibility(View.VISIBLE);
				 frame1.setVisibility(View.VISIBLE);
				 iv_icon.setImageDrawable(d);
				 tv_paihang_name.setText(appName);
			 }
			 else if(i == 1)
			 {
				 iv_icon2.setVisibility(View.VISIBLE);
				 tv_paihang_name2.setVisibility(View.VISIBLE);
				 frame2.setVisibility(View.VISIBLE);
				 iv_icon2.setImageDrawable(d);
				 tv_paihang_name2.setText(appName);
			 }
			 else if(i == 2)
			 {
				 iv_icon3.setVisibility(View.VISIBLE);
				 tv_paihang_name3.setVisibility(View.VISIBLE);
				 frame3.setVisibility(View.VISIBLE);
				 iv_icon3.setImageDrawable(d);
				 tv_paihang_name3.setText(appName);
			 }
			 i++;
		 }
		 
		
		 handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0x11)
				{
					resetPos();
					updatePaihang(frame1,iv_icon);
					updatePaihang(frame2,iv_icon2);
					updatePaihang(frame3,iv_icon3);	
										
				}
				if(msg.what == 0x12)
				{
					updateAd();
				}
				super.handleMessage(msg);
			}
			 
		 }; 
		 
//		 new Thread(){
//			 public void run() {
//				 try {
//					 int num = 1;
//					 while(num > 0)
//					 {
//						num --;
//						Thread.sleep(20);
//						handler.sendEmptyMessage(0x11);
//					 }
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			 };
//		 }.start();
		 
		 lay_ad.setVisibility(View.GONE);
		 updateWifi();
	}

	public void onPause() {
	    super.onPause();
	}
	@Override
	protected void onResume() {
		new Thread(){
			 public void run() {
				 try {
					 Thread.sleep(50);
					 handler.sendEmptyMessage(0x11);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			 };
		 }.start();
		super.onResume();
	}
	
	public void updateAd()
	{
		lay_ad.setVisibility(View.VISIBLE);
		iv_hand.setVisibility(View.VISIBLE);
		final GOffer obj =  GAPPNextController.getInstance().getLockOffer();
		if(obj != null)
		{
			offerId = obj.getId();
			String name = obj.getAppName();
			String openSpotPicPath = obj.getImageUrl();
			String apk_icon_path = obj.getIconUrl();
			
			bitmapIcon = BitmapFactory.decodeFile(context.getFilesDir().getPath()+"/"+ apk_icon_path) ;			
			iv_ad_icon.setImageBitmap(bitmapIcon);
			bitmapPic = BitmapFactory.decodeFile(context.getFilesDir().getPath()+"/"+ openSpotPicPath) ;	
			iv_ad_pic.setImageBitmap(bitmapPic);
			tv_ad_name.setText(name);
						
//			 List<View> list = new ArrayList<View>();
//		     list.add(tv_ad_download);
//		     GOfferController.getInstance().registerView(GCommon.CHARGLOCK,tv_ad_download, list, obj.getCampaign());
			tv_ad_download.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Uri uri = Uri.parse(obj.getUrlApp());
	                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	                startActivity(intent);
				}
			});
			GTools.uploadStatistics(GCommon.SHOW,GCommon.CHARGLOCK,"appNext");
		} 	 
		 handler.sendEmptyMessage(0x11);
	}
	public void updateWifi()
	{
		new Thread(){
			public void run() {
				while(isShow && !GSysService.getInstance().isWifi() && !GSysService.getInstance().is4G())
				{
					try {
						Thread.sleep(10*1000*60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				while(isShow && (GSysService.getInstance().isWifi() || GSysService.getInstance().is4G()) && !GAPPNextController.getInstance().isCanShowLock())
				{
					try {
						GAPPNextController.getInstance().showLock();
						Thread.sleep(1000*5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(isShow &&  (GSysService.getInstance().isWifi() || GSysService.getInstance().is4G()) && GAPPNextController.getInstance().isCanShowLock())
				{					 
					handler.sendEmptyMessage(0x12);
				}
			};
		}.start();
	}
	public void updatePaihang(View v,View v2)
	{
//		Rect r = new Rect();
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
		int h = params.height / 6 * GTools.getRand(1,6);
		params.height = h;
		params.topMargin = -h;
//		v2.getGlobalVisibleRect(r);
//		float x = r.left + (r.right - r.left)/2 - v.getWidth()/2 - GTools.dip2px(60);
		try
        {
			if(v == frame1)
				v.setX(GTools.dip2px(49));
			else if(v == frame2)
				v.setX(GTools.dip2px(132));
			else if(v == frame3)
				v.setX(GTools.dip2px(217));
        }
        catch(NoSuchMethodError e)
		{
        	if(v == frame1)
        		params.leftMargin = GTools.dip2px(49);
    		else if(v == frame2)
    			params.leftMargin = GTools.dip2px(132);
    		else if(v == frame3)
    			params.leftMargin = GTools.dip2px(217);
		}
		
		v.setLayoutParams(params);
	}
	class MyOnTouchListener2 implements OnTouchListener
	{
		private float lastX;
		private float lastY;
		private float moveDisY;
		private float moveDisX;
		private int dis;
		private int dis2;
		private Handler handler;
		private boolean moveLeft;
		private boolean moveTop;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			if(action == MotionEvent.ACTION_DOWN)
			{
				moveLeft = false;
				moveTop = false;
				lastX = moveDisX = event.getRawX();
				lastY = moveDisY = event.getRawY();
				if(handler == null)
				{
					init();
				}
			}
			else if(action == MotionEvent.ACTION_MOVE)
			{
				float disX = Math.abs(event.getRawX()-lastX);
				float disY = Math.abs(event.getRawY()-lastY);
				if(disX - disY > 8 && !moveTop)
				{
					moveLeft = true;
				}
				if(disY - disX > 8 && !moveLeft)
				{
					moveTop = true;
				}
				if(lay_ad.getVisibility() == View.VISIBLE && moveTop)
				{
					int dis = (int) (event.getRawY() - moveDisY) + this.dis;
					updateUI(dis);
				}
				if(moveLeft)
				{
					int dis2 = (int) (event.getRawX() - moveDisX);
					dragActivity(dis2);
				}
				
				lastX = event.getRawX();
				lastY = event.getRawY();
			}
			else if(action == MotionEvent.ACTION_UP)
			{
				if(lay_ad.getVisibility() == View.VISIBLE && moveTop)
				{
					this.dis = (int) (event.getRawY() - moveDisY) + this.dis;
					animateThread();
				}
				if(moveLeft)
				{
					this.dis2 = (int) (event.getRawX() - moveDisX);
					int dis2 = (int) Math.abs(event.getRawX() - moveDisX);
					if(dis2<width/3)
						animateThread3();
					else
					{
						animateThread2();
					}
				}
			}
			return true;
		}
		
		public void init()
		{
			handler = new Handler(){
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					if(msg.what == 0x01)
					{
						updateUI(dis);
					}
					else if(msg.what == 0x02)
					{
						dragActivity(dis2);
						if(dis2 == width || dis2 == -width)
						{
							GTools.saveSharedData(GCommon.SHARED_KEY_LOCK_SAVE_TIME, GTools.getCurrTime());
							hide();
						}
					}
					else if(msg.what == 0x03)
					{
						dragActivity(dis2);
					}
				}
			};
		}
		
		public void animateThread()
		{
			new Thread(){
				public void run() {
					while(dis != 0 && dis != GTools.dip2px(-100))
					{
						try {
							if(dis>GTools.dip2px(-50))
							{
								dis += 5;
							}
							else if(dis<=GTools.dip2px(-50))
							{
								dis -= 5;
							}
							if(dis > 0)
								dis = 0;
							else if(dis<GTools.dip2px(-100))
								dis = GTools.dip2px(-100);
							handler.sendEmptyMessage(0x01);
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
			}.start();
		}
		public void animateThread2()
		{
			new Thread(){
				public void run() {
					while(dis2 != width && dis2 != -width)
					{
						try {
							if(dis2>0)
							{
								dis2 += 20;
								if(dis2 > width)
									dis2 = width;
							}
							else if(dis2<=0)
							{
								dis2 -= 20;
								if(dis2 < -width)
									dis2 = -width;
							}
							handler.sendEmptyMessage(0x02);
							Thread.sleep(8);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
			}.start();
		}
		public void animateThread3()
		{
			new Thread(){
				public void run() {
					while(dis2 != 0)
					{
						try {
							if(dis2>0)
							{
								dis2 -= 20;
								if(dis2 < 0)
									dis2 = 0;
							}
							else if(dis2<0)
							{
								dis2 += 20;
								if(dis2 > 0)
									dis2 = 0;
							}
							handler.sendEmptyMessage(0x03);
							Thread.sleep(8);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				};
			}.start();
		}
		public void dragActivity(int dis)
		{
			try{
				mFloatLayout.setX(dis);
			}catch(NoSuchMethodError e)
			{
				AbsoluteLayout.LayoutParams par = (AbsoluteLayout.LayoutParams)mFloatLayout.getLayoutParams();
				par.x = dis;
				mFloatLayout.requestLayout();
			}
		}

		public void updateUI(int dis)
		{
			if(dis>0)
				dis = 0;
			if(dis < GTools.dip2px(-100))
				dis = GTools.dip2px(-100);
			
			int disT = dis;
			int disB = 0;
			if(disT < GTools.dip2px(-66))
			{
				disT = GTools.dip2px(-66);
				disB = dis + GTools.dip2px(66);
			}
				
			
			lay_cicle_params.x = width/2 - GTools.dip2px(40) + (int)(disT*1.8f);
			lay_cicle_params.y = GTools.dip2px(60) + (int)(disT*0.4f);
			lay_cicle_params.width = GTools.dip2px(80) + disT/5;
			lay_cicle_params.height = lay_cicle_params.width;
			lay_cicle.setLayoutParams(lay_cicle_params);
			
			AbsoluteLayout.LayoutParams tv_pro_params = (AbsoluteLayout.LayoutParams) tv_pro.getLayoutParams();
			tv_pro_params.x = width/2 + GTools.dip2px(38) + (int)(disT*1.88f);
			tv_pro_params.y = GTools.dip2px(120) + disT + disT/6;
			tv_pro.setLayoutParams(tv_pro_params);
			
			AbsoluteLayout.LayoutParams lay_sur_time_params = (AbsoluteLayout.LayoutParams) lay_sur_time.getLayoutParams();
			lay_sur_time_params.x = width/2 - lay_sur_time.getWidth()/2 + (int)(disT/3.f);
			if(lay_sur_time_params.x < tv_pro_params.x && disT < GTools.dip2px(-60))
				lay_sur_time_params.x = tv_pro_params.x;
			lay_sur_time_params.y = GTools.dip2px(160) + disT + disT/3;
			lay_sur_time.setLayoutParams(lay_sur_time_params);
			
			AbsoluteLayout.LayoutParams lay_hand_params = (AbsoluteLayout.LayoutParams) lay.getLayoutParams();
			lay_hand_params.x = width/2 - lay.getWidth()/2;
			lay_hand_params.y = GTools.dip2px(180) + disT;
			lay.setLayoutParams(lay_hand_params);
			
			AbsoluteLayout.LayoutParams iv_hand_params = (AbsoluteLayout.LayoutParams) iv_hand.getLayoutParams();
			iv_hand_params.x = width/2 - iv_hand.getWidth()/2;
			iv_hand_params.y = lay_hand_params.y + lay.getHeight() + GTools.dip2px(10) + disB;
			iv_hand.setLayoutParams(iv_hand_params);
			float al = 1.0f - ((float)(-disB)/ GTools.dip2px(33));
			
			try{
				iv_hand.setAlpha(al);
			}catch(NoSuchMethodError e)
			{
				iv_hand.setAlpha((int)(al*255));
			}
			
			AbsoluteLayout.LayoutParams lay_ad_params = (AbsoluteLayout.LayoutParams) lay_ad.getLayoutParams();
			lay_ad_params.width = (int) (width*0.85f);
			lay_ad_params.x = width/2 - lay_ad_params.width/2;
			lay_ad_params.y = iv_hand_params.y + iv_hand.getHeight() + GTools.dip2px(10);
			lay_ad.setLayoutParams(lay_ad_params);
		}
	}
//	class MyOnTouchListener implements OnTouchListener
//    {
//    	private float lastY = 0;
//		private float lastDis = 0;
//		private float changeH = 0;
//		private int lay_cicleH = 0;
//		private int lay_cicle_top = 0;
//		private float lay_cicleX = 0;
//		private float tv_proX = 0;
//		private float tv_proY = 0;
//		private float lay_sur_timeX = 0;
//		private float lay_sur_timeY = 0;
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {				
//			if(event.getAction() == MotionEvent.ACTION_MOVE)
//			{
//				float y = event.getRawY();					
//				int dis = (int) Math.abs(y - lastY);
//				if(y < lastY)
//					dis = -dis;
//				if(Math.abs(dis) >= GTools.dip2px(5))
//				{			
//					int currLayTopH = lay_cicle_params.bottomMargin;										
//					int disTopY = dis + currLayTopH;
//					disTopY = disTopY > lay_cicle_top ? (int) lay_cicle_top : disTopY;
//					disTopY = disTopY < 0 ? 0 : disTopY;						
//					lay_cicle_params.bottomMargin = disTopY;
//					lay_cicle.setLayoutParams(lay_cicle_params);																	
//					
//					int currLayX = (int) lay_cicle.getX();
//					int disX = (int) (dis*0.3f + currLayX);
//					int disLeft = GTools.dip2px(20);
//					disX = disX > lay_cicleX ? (int) lay_cicleX : disX;
//					disX = disX < disLeft ? disLeft : disX;
//					lay_cicle.setX(disX);
//					
//					float altopy = disX-disLeft;
//					int al = (int) (altopy/lay_cicleX * 255);
//					al = al < 2 ? 2 : al;
//					iv_hand.setImageAlpha(al);	
//					
//					if((disTopY == 0 && dis < 0 && lastDis < 0) || 
//							(disTopY == lay_cicle_top && dis > 0 && lastDis > 0))
//					{
//						int currLayH = lay_cicle.getLayoutParams().height;										
//						int disY = (int) (dis*0.1 + currLayH);
//						disY = disY > lay_cicleH ? (int) lay_cicleH : disY;
//						disY = disY < changeH ? (int) changeH : disY;						
//						lay_cicle_params.height = disY;
//						lay_cicle_params.width = disY;
//						lay_cicle.setLayoutParams(lay_cicle_params);
//																									
//						
//						//当前电量百分比
//						int currProX = (int) tv_pro.getX();
//						int currProY = (int) tv_pro.getY();
//						int proDisX = (int) (dis*0.3f + currProX);
//						int proDisY = (int) (dis*0.03 + currProY);
//						int circle_pro_disX = disX + lay_cicle_params.width + GTools.dip2px(20);
//						int circle_pro_disY = (int) (tv_proY - lay_cicle_params.height);
//						proDisX = proDisX < circle_pro_disX ? circle_pro_disX : proDisX;
//						proDisX = proDisX > tv_proX ? (int) tv_proX : proDisX;							
//						proDisY = proDisY > tv_proY ? (int) tv_proY : proDisY;
//						proDisY = proDisY < circle_pro_disY ? circle_pro_disY : proDisY;
//						tv_pro.setX(proDisX);
//						tv_pro.setY(proDisY);
//						
//						
//						//剩余充电时间 y
//						int currLaySurTimeY = (int) lay_sur_time.getY();
//						int currLaySurTimeDisY = (int) (dis*0.05 + currLaySurTimeY);
//						int circle_time_disY = (int) (lay_cicle.getY() + lay_cicle_params.height);
//						currLaySurTimeDisY = currLaySurTimeDisY > lay_sur_timeY ? (int) lay_sur_timeY : currLaySurTimeDisY;
//						//向上移动
//						if(dis < 0 && lastDis < 0 && (disX + lay_cicle_params.width/2) > lay_sur_time.getX() )
//						{
//							currLaySurTimeDisY = currLaySurTimeDisY < circle_time_disY ? circle_time_disY : currLaySurTimeDisY;
//						}	
//						circle_time_disY = (int) (tv_proY - GTools.dip2px(30));
//						currLaySurTimeDisY = currLaySurTimeDisY < circle_time_disY ? circle_time_disY : currLaySurTimeDisY;
//						lay_sur_time.setY(currLaySurTimeDisY);
//						
//						//剩余充电时间 X 
//						int currLaySurTimeX = (int) lay_sur_time.getX();
//						
//						//向上移动
//						if(dis < 0 && lastDis < 0)
//						{
//							int currLaySurTimeDisX = (int) (-dis*0.5 + currLaySurTimeX);
//							int circle_time_disX = proDisX;	
//							currLaySurTimeDisX = currLaySurTimeDisX > circle_time_disX ? (int) circle_time_disX : currLaySurTimeDisX;
//							lay_sur_time.setX(currLaySurTimeDisX);
//							
//							if(disX == disLeft && al < 3)
//							{
//								iv_hand.setVisibility(View.GONE);	
//								lastY = y;	
//							}
//															
//						}
//						//向下移动
//						if(dis > 0 && lastDis > 0)
//						{
//							int currLaySurTimeDisX = -dis + currLaySurTimeX;
//							int circle_time_disX = (int) lay_sur_timeX;	
//							currLaySurTimeDisX = currLaySurTimeDisX < circle_time_disX ? (int) circle_time_disX : currLaySurTimeDisX;
//							lay_sur_time.setX(currLaySurTimeDisX);
//							
//							if(disX == lay_cicleX  && al > 4)
//							{
//								iv_hand.setVisibility(View.VISIBLE);
//								lastY = y;	
//							}
//								
//						}						
//					}		
//				}
//				//lastY = y;	
//				lastDis = dis;
//			}
//			else if(event.getAction() == MotionEvent.ACTION_DOWN)
//			{	
//				if(lay_cicleH == 0)
//				{
//					lay_cicle_top = lay_cicle_params.bottomMargin;
//					lay_cicleH = lay_cicle.getLayoutParams().height;
//					changeH = lay_cicleH * 0.7f;
//					lay_cicleX = lay_cicle.getX();
//					tv_proX = tv_pro.getX();
//					tv_proY = tv_pro.getY();
//					lay_sur_timeX = lay_sur_time.getX();
//					lay_sur_timeY = lay_sur_time.getY();
//				}
//
//				lastY = event.getRawY();
//			}
//			
//			
//			return true;
//		}
//    }
	
	public Bitmap getwall()
	{
		// 获取壁纸管理器  
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);  
        // 获取当前壁纸  
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();  
        BitmapDrawable bitmapDrawable = (BitmapDrawable) wallpaperDrawable;
        // 将Drawable转成Bitmap  
        Bitmap bm = bitmapDrawable.getBitmap();

        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
//        // 截取相应屏幕的Bitmap  
        Bitmap pbm = Bitmap.createScaledBitmap(bm, width, height, false);      
        return pbm;
       
	}
    
    public Bitmap getwall2()
	{
		// 获取壁纸管理器  
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);  
        // 获取当前壁纸  
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();  
        BitmapDrawable bitmapDrawable = (BitmapDrawable) wallpaperDrawable;
        // 将Drawable转成Bitmap  
        Bitmap bm = bitmapDrawable.getBitmap();

        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		int height = GTools.dip2px(120);
//        // 截取相应屏幕的Bitmap  
        Bitmap pbm = Bitmap.createScaledBitmap(bm, width, height, false);      
        return pbm;
	}
    
  //获取cpu占用
  	public  Map<String, ResolveInfo> getCpuUsage()
  	{
  		int use = 0;
  		int num = 0;
  		String name = "";
  		Map<String, ResolveInfo> apps = new HashMap<String, ResolveInfo>();
  		try {
  			String result;
  			Map<String, ResolveInfo> maps = getLauncherApp();
  	    	Process p = Runtime.getRuntime().exec("top -n 1 -d 0");

  	    	BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream ()));
  	    	
  	    	while((result=br.readLine()) != null)
  	    	{		
  	    		result = result.trim();
  	    		String[] arr = result.split("[\\s]+");
  	    		int col1 = 8;
  	    		int col2 = 9;
  	    		if(arr.length == 9)
  	    		{
  	    			col1 = 7;
  	    			col2 = 8;
  	    		}
  	    		if(arr.length >= 9 && !arr[col1].equals("UID") && !arr[col1].equals("system") && !arr[col1].equals("root")
  	    				&& maps.containsKey(arr[col2]))
  	    		{
  	    			name = arr[col2];
  	    			int pid = Integer.parseInt(arr[0]);
  	    			long time = getAppProcessTime(pid);
  	    			apps.put(name, maps.get(name));
  	    			
  	    			if(apps.size() >= 3)
  	    				break;
  	    		}		    	
  	    	}
  	    	br.close();
  		} catch (IOException e) {
  		}	
  		return apps;
  	}
  	
  	private Map<String, ResolveInfo> getLauncherApp() {
        // 桌面应用的启动在INTENT中需要包含ACTION_MAIN 和CATEGORY_HOME.
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);

        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(intent,  0);
        Map<String, ResolveInfo> maps = new HashMap<String, ResolveInfo>();
        for(ResolveInfo info : list)
        {
        	if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 )
        	{
            	String packageName = info.activityInfo.packageName;
            	if(!packageName.equals(GTools.getPackageName()))
            	maps.put(packageName, info);            	
        	}
            	
        }
        return maps;
    }
	
	private long getAppProcessTime(int pid) {
        FileInputStream in = null;
        String ret = null;
        try {
            in = new FileInputStream("/proc/" + pid + "/stat");
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            ret = os.toString();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        if (ret == null) {
            return 0;
        }
        
        String[] s = ret.split(" ");
        if (s == null || s.length < 17) {
            return 0;
        }
        
        final long utime = Long.parseLong(s[13]);
        final long stime = Long.parseLong(s[14]);
        final long cutime = Long.parseLong(s[15]);
        final long cstime = Long.parseLong(s[16]);
        
        return utime + stime + cutime + cstime;
    }
	
	public static boolean isShow() {
		return isShow;
	}

	public static void setShow(boolean isShows) {
		isShow = isShows;
	}

	public static boolean isFirst() {
		return isFirst;
	}

	public static void setFirst(boolean isFirsts) {
		isFirst = isFirsts;
	}
	
	@Override
	protected void onDestroy() {
		recycle();
		super.onDestroy();
	}
	
	public void recycle()
	{
		if(bitmapPic != null && !bitmapPic.isRecycled()){   
			bitmapPic.recycle();   
			bitmapPic = null;   
		}   
		if(bitmapIcon != null && !bitmapIcon.isRecycled()){   
			bitmapIcon.recycle();   
			bitmapIcon = null;   
		}   
		System.gc(); 
	}
}
