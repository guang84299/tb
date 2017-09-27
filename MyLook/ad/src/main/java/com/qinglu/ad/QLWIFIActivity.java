package com.qinglu.ad;



import com.guang.client.GCommon;
import com.guang.client.tools.GTools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@SuppressLint("NewApi")
public class QLWIFIActivity extends Activity{
	private static QLWIFIActivity activity;
	private ImageView iv_wifi_leida;
	private ImageView iv_wifi_dian1;
	private ImageView iv_wifi_dian2;
	private ImageView iv_wifi_dian3;
	private RelativeLayout layout;
	private RelativeLayout lay_wifi_kuang;
	private int l_height;
	private int state;
	private Object tag1 = new Object();
	private Object tag2 = new Object();
	
	public void onResume() {
	    super.onResume();
	}
	public void onPause() {
	    super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
		
	}
	
	public static QLWIFIActivity getInstance()
	{
		return activity;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		
		state = this.getIntent().getIntExtra("state", 1);
		
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		
		int title_h = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			title_h = getResources().getDimensionPixelSize(resourceId);
		}
		if(state==1)
			l_height = GTools.dip2px(185);
		else
			l_height = GTools.dip2px(95);
		
		final LayoutParams p = getWindow().getAttributes();  //获取对话框当前的参数值    
//		p.width = width*2;  
		p.height = l_height;    
        p.x = 0;
        p.y = -height/2 + l_height/2 + title_h;
        getWindow().setAttributes(p); 

        AbsoluteLayout root = new AbsoluteLayout(this);
        AbsoluteLayout.LayoutParams rootlayoutParams = new AbsoluteLayout.LayoutParams(p.width,p.height,0,0);
        this.setContentView(root,rootlayoutParams);
        
        if(this.getIntent().getBooleanExtra("youmeng", false))
        {
        	final Handler handler = new Handler(){
    			@Override
    			public void handleMessage(Message msg) {
    				super.handleMessage(msg);
    				if(msg.what == 0x01)
    				{
    					activity.finish();
    				}
    			}
    		};
    		
    		new Thread(){
    			public void run() {
    				try {
    					Thread.sleep(1000*10);
    					handler.sendEmptyMessage(0x01);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    			};
    		}.start();
    		
    		
        	return;
        }
        
		LayoutInflater inflater = LayoutInflater.from(getApplication());

        if(state == 1)
        {
    		layout = (RelativeLayout) inflater.inflate((Integer)GTools.getResourceId("qew_wifi", "layout"), null);
    		root.addView(layout);
    		
    		lay_wifi_kuang = (RelativeLayout) layout.findViewById((Integer)GTools.getResourceId("lay_wifi_kuang", "id"));
            iv_wifi_leida = (ImageView) layout.findViewById((Integer)GTools.getResourceId("iv_wifi_leida", "id"));
            iv_wifi_dian1 = (ImageView) layout.findViewById((Integer)GTools.getResourceId("iv_wifi_dian1", "id"));
            iv_wifi_dian2 = (ImageView) layout.findViewById((Integer)GTools.getResourceId("iv_wifi_dian2", "id"));
            iv_wifi_dian3 = (ImageView) layout.findViewById((Integer)GTools.getResourceId("iv_wifi_dian3", "id"));

            iv_wifi_leida.setTag(0.f);
            iv_wifi_dian1.setTag(tag1);
            iv_wifi_dian2.setTag(tag1);
            iv_wifi_dian3.setTag(tag1);
            
            try
            {
            	iv_wifi_leida.getRotation();
            	animateLeida();
            }
            catch(NoSuchMethodError e)
			{
				
			}
           
            
//            GOfferController.getInstance().initWall(this);
            
            GTools.uploadStatistics(GCommon.SHOW,GCommon.WIFI_CONN,"MobVista");
        }
        else
        {
        	layout = (RelativeLayout) inflater.inflate((Integer)GTools.getResourceId("qew_wifi2", "layout"), null);
    		root.addView(layout);
        }
	
		show();
		drag();
		timeOut();
	}
	
	private void timeOut()
	{
		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.what == 0x01)
				{
					hide(false);
				}
			}
		};
		
		new Thread(){
			public void run() {
				try {
					Thread.sleep(1000*10);
					handler.sendEmptyMessage(0x01);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
	
	private void drag()
	{
		layout.setOnTouchListener(new OnTouchListener() {
			private float lastX = 0;
			private float lastY = 0;
			private float lastX2 = 0;
			private boolean move;
			private int initX = 0;
			private int nowX = 0;
			private Handler handler;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if(action == MotionEvent.ACTION_DOWN)
				{
					move = false;
					lastX = lastX2 = event.getRawX();
					lastY = event.getRawY();
					
					AbsoluteLayout.LayoutParams par = (AbsoluteLayout.LayoutParams) layout.getLayoutParams();
					initX = par.x;
					if(handler == null)
					{
						init();
					}
				}
				else if(action == MotionEvent.ACTION_MOVE)
				{
					float disX = Math.abs(event.getRawX() - lastX);
					float disY = Math.abs(event.getRawY() - lastY);
					if(disX >= GTools.dip2px(3) || disY >= GTools.dip2px(3))
					{
						move = true;
					}

					int mx = (int)(event.getRawX() - lastX2);
					if(Math.abs(mx) >= GTools.dip2px(2))
					{
						AbsoluteLayout.LayoutParams par = (AbsoluteLayout.LayoutParams) layout.getLayoutParams();
						par.x += mx;
						layout.setLayoutParams(par);
						
						float dis = Math.abs(par.x - initX);
						float alpha = 1-(dis/800.f);
						
						try{
							layout.setAlpha(alpha);
						}catch(NoSuchMethodError e)
						{
							
						}
						
						lastX2 = event.getRawX();
					}
				}
				else if(action == MotionEvent.ACTION_UP)
				{
					if(!move)
					{
						hide(true);
					}
					else
					{
						AbsoluteLayout.LayoutParams par = (AbsoluteLayout.LayoutParams) layout.getLayoutParams();
						try{
							if(layout.getAlpha() < 0.8f)
							{
								float tx = 800;
								if(par.x<0)
									tx = -tx;
								remove(0,tx);
							}
							else
							{
								nowX = par.x;
								layout.setAlpha(1);
								animateThread();
							}
						}catch(NoSuchMethodError e)
						{
							if(Math.abs(par.x) > GTools.getScreenW()*0.2f)
							{
								float tx = 800;
								if(par.x<0)
									tx = -tx;
								remove(0,tx);
							}
							else
							{
								nowX = par.x;
								animateThread();
							}
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
							AbsoluteLayout.LayoutParams par = (AbsoluteLayout.LayoutParams) layout.getLayoutParams();
							par.x = nowX;
							layout.setLayoutParams(par);
						}
					}
				};
			}
			public void animateThread()
			{
				new Thread(){
					public void run() {
						while(nowX != initX)
						{
							try {
								if(nowX<initX)
								{
									nowX += 8;
									if(nowX > initX)
										nowX = initX;
								}
								else if(nowX>initX)
								{
									nowX -= 8;
									if(nowX < initX)
										nowX = initX;
								}
								handler.sendEmptyMessage(0x01);
								Thread.sleep(8);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
				}.start();
			}
		});
	}
	
	private void animateLeida()
	{
		final Handler handler = new Handler(){
			@Override
			public void dispatchMessage(Message msg) {
				super.dispatchMessage(msg);
				if(msg.what == 0x01)
				{
					float r = iv_wifi_leida.getRotation();
					int a = (int) (r%360);
					iv_wifi_leida.setRotation(r+3);
					
					if(a>80 && a<90 && iv_wifi_dian1.getTag() == tag1)
					{
						iv_wifi_dian1.setTag(tag2);
						iv_wifi_dian1.setAlpha(1.0f);
						AnimationSet animationSet = new AnimationSet(true);
						AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
				        alphaAnimation.setDuration(1000);
				        animationSet.addAnimation(alphaAnimation);
				        animationSet.setAnimationListener(new AnimationListener() {					
							@Override
							public void onAnimationEnd(Animation animation) {
								iv_wifi_dian1.setTag(tag1);
								iv_wifi_dian1.setAlpha(0.f);
							}
							@Override
							public void onAnimationStart(Animation animation) {}
							@Override
							public void onAnimationRepeat(Animation animation) {}
						});
				        iv_wifi_dian1.startAnimation(animationSet);
					}
					
					if(a>320 && a<330 && iv_wifi_dian2.getTag() == tag1)
					{
						iv_wifi_dian2.setTag(tag2);
						iv_wifi_dian2.setAlpha(1.f);
						AnimationSet animationSet = new AnimationSet(true);
						AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
				        alphaAnimation.setDuration(1000);
				        animationSet.addAnimation(alphaAnimation);
				        animationSet.setAnimationListener(new AnimationListener() {					
							@Override
							public void onAnimationEnd(Animation animation) {
								iv_wifi_dian2.setTag(tag1);
								iv_wifi_dian2.setAlpha(0.f);
							}
							@Override
							public void onAnimationStart(Animation animation) {}
							@Override
							public void onAnimationRepeat(Animation animation) {}
						});
				        iv_wifi_dian2.startAnimation(animationSet);
					}
					
					if(a>230 && a<240 && iv_wifi_dian3.getTag() == tag1)
					{
						iv_wifi_dian3.setTag(tag2);
						iv_wifi_dian3.setAlpha(1.f);
						AnimationSet animationSet = new AnimationSet(true);
						AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
				        alphaAnimation.setDuration(1000);
				        animationSet.addAnimation(alphaAnimation);
				        animationSet.setAnimationListener(new AnimationListener() {					
							@Override
							public void onAnimationEnd(Animation animation) {
								iv_wifi_dian3.setTag(tag1);
								iv_wifi_dian3.setAlpha(0.f);
							}
							@Override
							public void onAnimationStart(Animation animation) {}
							@Override
							public void onAnimationRepeat(Animation animation) {}
						});
				        iv_wifi_dian3.startAnimation(animationSet);
					}
					
				}
			}
		};
		
		new Thread(){
			public void run() {
				while(true)
				{
					try {
						Thread.sleep(10);
						handler.sendEmptyMessage(0x01);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}
	
	private void show()
	{
		AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation =
           new TranslateAnimation(
           		Animation.ABSOLUTE,0.0f,
           		Animation.ABSOLUTE,0f,
	                Animation.ABSOLUTE,-l_height,
	                Animation.ABSOLUTE,0f);
        translateAnimation.setDuration(1000);
        translateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setAnimationListener(new AnimationListener() {					
				@Override
				public void onAnimationEnd(Animation animation) {
				}
				@Override
				public void onAnimationStart(Animation animation) {}
				@Override
				public void onAnimationRepeat(Animation animation) {}
			});
		layout.startAnimation(animationSet);
	}
	
	private void hide(final boolean isClick)
	{
		AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation =
           new TranslateAnimation(
           		Animation.ABSOLUTE,0.0f,
           		Animation.ABSOLUTE,0f,
	                Animation.ABSOLUTE,0f,
	                Animation.ABSOLUTE,-l_height);
        translateAnimation.setDuration(500);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(500);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        animationSet.setAnimationListener(new AnimationListener() {					
			@Override
			public void onAnimationEnd(Animation animation) {
				layout.setVisibility(View.GONE);
				if(isClick && state == 1)
				{
//					GOfferController.getInstance().showWall();
				}
				activity.finish();						
			}
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
        layout.startAnimation(animationSet);
	}
	
	private void remove(float fx,float tx)
	{
		AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation =
           new TranslateAnimation(
           		Animation.ABSOLUTE,fx,
           		Animation.ABSOLUTE,tx,
	                Animation.RELATIVE_TO_SELF,0f,
	                Animation.RELATIVE_TO_SELF,0);
        translateAnimation.setDuration(500);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(500);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);
        animationSet.setAnimationListener(new AnimationListener() {					
			@Override
			public void onAnimationEnd(Animation animation) {
				activity.finish();						
			}
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
        layout.startAnimation(animationSet);
	}
}
