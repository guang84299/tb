package com.qinglu.ad;

import com.guang.client.GCommon;
import com.guang.client.controller.GAvazuController;
import com.guang.client.controller.GOnewayController;
import com.guang.client.mode.GSMOffer;
import com.guang.client.tools.GTools;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;

public class QLBrowserSpotActivity extends Activity{
	private QLBrowserSpotActivity activity;
	private RelativeLayout layout;
	private Bitmap bitmap;
	
	private String adSource;
	
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN );
		

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		layout = new RelativeLayout(this);
		layout.setLayoutParams(layoutParams);
		this.setContentView(layout);
		
		GSMOffer obj = null;
		boolean type = getIntent().getBooleanExtra("type", false);
		if(type)
		{
			obj = GOnewayController.getInstance().getSpotOffer();
			adSource = "OneWay";
		}
		else
		{
			obj = GAvazuController.getInstance().getOffer();
			adSource = "avazu";
		}
        String picPath = obj.getLink();
        final String target = obj.getTarget();
        
        //图片
        ImageView image = new ImageView(this);
        bitmap = BitmapFactory.decodeFile(this.getFilesDir().getPath()+"/"+ picPath);
        image.setImageBitmap(bitmap);
        
        RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);		
        imageLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		QLSize ss = GTools.getScreenSize(this);
		imageLayoutParams.width = (int) (ss.width*0.8f);
		imageLayoutParams.height = (int) (ss.height*0.8f);
		
		image.setScaleType(ScaleType.CENTER_CROP);

		layout.addView(image, imageLayoutParams);	
		
		//关闭按钮
		RelativeLayout.LayoutParams closeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		int marginX = (int) (ss.width*0.1f)-GTools.dip2px(10);
		int marginY = (int) (ss.height*0.1f)-GTools.dip2px(10);
		closeLayoutParams.setMargins(0, marginY, marginX, 0);
		closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		
		ImageView close = new ImageView(this);
		close.setImageResource((Integer)GTools.getResourceId("qew_browser_close", "drawable"));
		layout.addView(close, closeLayoutParams);	
		
		//关闭事件
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.finish();
				
			}
		});
		
		//广告点击事件
		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(target);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                GTools.uploadStatistics(GCommon.CLICK,GCommon.BROWSER_SPOT,adSource);
                activity.finish();	
			}
		});
		
		show();
		
		GTools.uploadStatistics(GCommon.SHOW,GCommon.BROWSER_SPOT,adSource);
	}
	
	private void show()
	{
		AnimationSet animationSet = new AnimationSet(true);
		AlphaAnimation animation = new AlphaAnimation(0, 1);
		animation.setDuration(500);
		animationSet.addAnimation(animation);
		layout.startAnimation(animationSet);
	}
	
	@Override
	protected void onDestroy() {
		recycle();
		super.onDestroy();
	}
	
	public void recycle()
	{
		if(bitmap != null && !bitmap.isRecycled()){   
			bitmap.recycle();   
			bitmap = null;   
		}   
 
		System.gc(); 
	}
}
