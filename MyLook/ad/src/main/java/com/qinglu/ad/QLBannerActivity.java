package com.qinglu.ad;




import com.guang.client.GCommon;
import com.guang.client.controller.GParbattechController;
import com.guang.client.tools.GTools;
import com.infomobi.IAd;
import com.infomobi.IAdItem;
import com.infomobi.IAdService;
import com.infomobi.ICallback;
import com.infomobi.INativeAd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@SuppressWarnings("deprecation")
public class QLBannerActivity extends Activity{
	private QLBannerActivity context;
	private RelativeLayout view;
	private int l_height;

	private String appName;
	private long bannerAdPositionId;
	private String adId;
	private static boolean _show = false;
	
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

	public static boolean isShow()
	{
		return _show;
	}
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		_show = true;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
//		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		
		int title_h = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			title_h = getResources().getDimensionPixelSize(resourceId);
		}
		
		l_height = GTools.dip2px(50);
		
		final LayoutParams p = getWindow().getAttributes();  //获取对话框当前的参数值    
//		p.width = width*2;  
		p.height = l_height;    
        p.x = 0;
        p.y = -height/2 + l_height/2 + title_h;
        getWindow().setAttributes(p); 
        
        AbsoluteLayout root = new AbsoluteLayout(this);
        AbsoluteLayout.LayoutParams rootlayoutParams = new AbsoluteLayout.LayoutParams(p.width,p.height,0,0);
// 		rootlayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        view = new RelativeLayout(this);
 		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, l_height);
 		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
 		view.setLayoutParams(layoutParams);
 		
 		root.addView(view);

		this.bannerAdPositionId = getIntent().getLongExtra("adPositionId",0);
		this.appName = getIntent().getStringExtra("appName");
		this.adId = getIntent().getStringExtra("adId");

		IAdService adService = GParbattechController.getInstance().getAdService();
		if(adService != null)
		{
			final RelativeLayout container = new RelativeLayout(this);
			RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, l_height);
			layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
			container.setLayoutParams(layoutParams2);
			view.addView(container);

			ImageView img = new ImageView(this);
			img.setScaleType(ImageView.ScaleType.CENTER_CROP);
			RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, l_height);
			layoutParams3.addRule(RelativeLayout.CENTER_IN_PARENT);
			img.setLayoutParams(layoutParams3);
			container.addView(img);


			INativeAd mAdView = adService.getNativeAd("native.ad1", 320, 50, 1, null);

			IAdItem item = mAdView.getAdItem(0);
			item.bind(new String[]{IAdItem.IMAGE, IAdItem.CALL_TO_ACTION},
					new View[]{img,container});
			container.setVisibility(View.GONE);

			mAdView.setOnLoadLisenter(new ICallback(){
				@Override
				public void call(int resultCode) {
					if (resultCode == IAd.OK){
						container.setVisibility(View.VISIBLE);
						GTools.uploadStatistics(GCommon.REQUEST,GCommon.BANNER,"GMobi");
					}
				} });
			mAdView.load();

		}


//		AdView mAdView = new AdView(this);
//		RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//		layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
//		view.addView(mAdView,layoutParams2);
//
//		mAdView.setAdSize(AdSize.BANNER);
//		mAdView.setAdUnitId(this.adId);
//		mAdView.setAdListener(new AdListener() {
//			@Override
//			public void onAdClosed() {
//				super.onAdClosed();
//				GLog.e("-------------","onAdClosed");
//			}
//
//			@Override
//			public void onAdFailedToLoad(int i) {
//				super.onAdFailedToLoad(i);
//				hide(false);
//				GLog.e("-------------","onAdFailedToLoad code="+i + "  adid="+adId);
//			}
//
//			@Override
//			public void onAdLeftApplication() {
//				super.onAdLeftApplication();
//				GTools.uploadStatistics(GCommon.CLICK,GCommon.BANNER,"AdMob");
//				hide(false);
//			}
//
//			@Override
//			public void onAdOpened() {
//				super.onAdOpened();
//			}
//
//			@Override
//			public void onAdLoaded() {
//				super.onAdLoaded();
//				if(!GTools.isAppInBackground(appName))
//				{
//					show();
//					GTools.uploadStatistics(GCommon.SHOW,GCommon.BANNER,"AdMob");
//
//					GLog.e("--------------", "banner success");
//				}
//				else
//					hide(false);
//			}
//		});
//		AdRequest adRequest = new AdRequest.Builder().build();
//		mAdView.loadAd(adRequest);

 		this.setContentView(root,rootlayoutParams);

		int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_BANNER_NUM+bannerAdPositionId, 0);
		GTools.saveSharedData(GCommon.SHARED_KEY_BANNER_NUM+bannerAdPositionId, num+1);
		GTools.saveSharedData(GCommon.SHARED_KEY_BANNER_TIME+bannerAdPositionId,GTools.getCurrTime());




		
		view.setOnTouchListener(new OnTouchListener() {
			private float lastX = 0;
			private float lastY = 0;
			private float lastX2 = 0;
			private boolean move;
			private int initX = 0;
			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if(action == MotionEvent.ACTION_DOWN)
				{
					move = false;
					lastX = lastX2 = event.getRawX();
					lastY = event.getRawY();
					
					AbsoluteLayout.LayoutParams par = (AbsoluteLayout.LayoutParams) view.getLayoutParams();
					initX = par.x;
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
						AbsoluteLayout.LayoutParams par = (AbsoluteLayout.LayoutParams) view.getLayoutParams();
						par.x += mx;
						view.setLayoutParams(par);
						
						float dis = Math.abs(par.x - initX);
						float alpha = 1-(dis/800.f);
						
						try{
							view.setAlpha(alpha);
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
						AbsoluteLayout.LayoutParams par = (AbsoluteLayout.LayoutParams) view.getLayoutParams();
						try{
							if(view.getAlpha() <= 0.8f)
							{
								float tx = 800;
								if(par.x<0)
									tx = -tx;
								remove(0,tx);
							}
							else
							{
								par.x = initX;
								view.setLayoutParams(par);
								view.setAlpha(1);
							}
						}catch(NoSuchMethodError e)
						{
							if(par.x > 0.2f*GTools.getScreenW())
							{
								float tx = 800;
								if(par.x<0)
									tx = -tx;
								remove(0,tx);
							}
							else
							{
								par.x = initX;
								view.setLayoutParams(par);
							}
						}
						
					}
				}
				return true;
			}
		});

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
        view.startAnimation(animationSet);


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
					Thread.sleep(1000*13);
					handler.sendEmptyMessage(0x01);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();

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
				view.setVisibility(View.GONE);
				context.finish();
				_show = false;
			}
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
       view.startAnimation(animationSet);
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
				context.finish();
				_show = false;
			}
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
       view.startAnimation(animationSet);
	}

}
