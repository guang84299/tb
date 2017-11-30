package com.qinglu.ad;




//import com.data.callback.AdShowListener;
import com.guang.client.GCommon;
import com.guang.client.tools.GLog;
import com.guang.client.tools.GTools;
import com.qinglu.ad.view.AVLoadingIndicatorView;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

//import pa.path.PNativeAd;

public class QLAppSpotActivity extends Activity{
	private static QLAppSpotActivity activity;
	private RelativeLayout layout;

	private long spotAdPositionId;
	private String appName;
	private String adId;

//	private PNativeAd pNativeAd;

	private int num = 0;
	private List<String> loads = new ArrayList<String>();
	private List<String> bgColors = new ArrayList<String>();
	private List<String> loadColors = new ArrayList<String>();
	private AVLoadingIndicatorView vl;
	
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
	
	public static QLAppSpotActivity getInstance()
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


		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		layout = new RelativeLayout(this);
		layout.setLayoutParams(layoutParams);
		this.setContentView(layout);

		this.spotAdPositionId = getIntent().getLongExtra("adPositionId",0);
		this.appName = getIntent().getStringExtra("appName");
		this.adId = getIntent().getStringExtra("adId");


//		initLoads();
//
//		int loadNum = GTools.getSharedPreferences().getInt(appName+"load",-1);
//		if(loadNum == -1)
//		{
//			loadNum = GTools.getSharedPreferences().getInt("loadNum",-1);
//			loadNum += 1;
//			if(loadNum >= 10)
//				loadNum = 0;
//
//			GTools.saveSharedData("loadNum",loadNum);
//			GTools.saveSharedData(appName+"load",loadNum);
//		}
//
//		layout.setBackgroundColor(Color.parseColor(bgColors.get(loadNum)));
//
//		vl = new AVLoadingIndicatorView(this);
//		RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(
//				LinearLayout.LayoutParams.WRAP_CONTENT,
//				LinearLayout.LayoutParams.WRAP_CONTENT);
//		layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
//		vl.setIndicatorColor(Color.parseColor(loadColors.get(loadNum)));
//		layout.addView(vl,layoutParams2);
//		vl.setIndicator(loads.get(loadNum));

//		layout.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//
//				layout.removeView(vl);
//
//				num++;
//				if(num >= loads.size())
//					num = 0;
//				vl = new AVLoadingIndicatorView(activity);
//				RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(
//						LinearLayout.LayoutParams.WRAP_CONTENT,
//						LinearLayout.LayoutParams.WRAP_CONTENT);
//				layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
//				layout.addView(vl,layoutParams2);
//				vl.setIndicator(loads.get(num));
//			}
//		});

		showAppSpot();

	}

	private void initLoads()
	{
		loads.add("BallClipRotatePulseIndicator");
		loads.add("BallGridPulseIndicator");
		loads.add("BallPulseIndicator");
		loads.add("BallRotateIndicator");
//		loads.add("BallSpinFadeLoaderIndicator");
		loads.add("BallTrianglePathIndicator");
		loads.add("LineScaleIndicator");
		loads.add("PacmanIndicator");
		loads.add("SemiCircleSpinIndicator");
		loads.add("SquareSpinIndicator");
		loads.add("TriangleSkewSpinIndicator");

		bgColors.add("#25aafa");
		bgColors.add("#fd9500");
		bgColors.add("#ffc20c");
		bgColors.add("#7bb811");
		bgColors.add("#4e4e4e");
		bgColors.add("#02b8cd");
		bgColors.add("#e85170");
		bgColors.add("#8a229c");
		bgColors.add("#ffffff");
		bgColors.add("#d3f675");

		loadColors.add("#c7eaff");
		loadColors.add("#ffffff");
		loadColors.add("#ff7e04");
		loadColors.add("#ecff54");
		loadColors.add("#25aafa");
		loadColors.add("#abfeff");
		loadColors.add("#ffbbc9");
		loadColors.add("#ffadf4");
		loadColors.add("#d6d6d6");
		loadColors.add("#8bc34a");
	}

	public void showAppSpot()
	{
//		pNativeAd = new PNativeAd(getApplicationContext());
//		pNativeAd.setListener(new AdShowListener() {
//			public void onShowPageFailed(String arg0) {
//				//展示页面失败
//				hide();
//				GLog.e("--------------", "onShowPageFailed");
//			}
//			public void onShowPage() {
//				//展示页面的回调
//				GTools.uploadStatistics(GCommon.SHOW,GCommon.APP_SPOT,"parbattech");
//				hide();
//				GLog.e("--------------", "onShowPage");
//			}
//			public void onLoadSuccessed() {
//				//加载数据成功
//				if(!GTools.isAppInBackground(appName))
//				{
//					pNativeAd.showPage();//展示页面
//					GLog.e("--------------", "app spot success!");
//				}
//				else
//				{
//					hide();
//					GLog.e("--------------", "isAppInBackground="+appName);
//				}
//			}
//			public void onLoadFailed(String arg0) {
//				//加载数据失败
//				hide();
//				GLog.e("-------------","onAdFailedToLoad ="+arg0);
//			}
//			public void onClosePage() {
//				//关闭页面
//				hide();
//				GLog.e("--------------", "onAdClosed");
//			}
//			public void onClickPage() {
//				//点击页面
//				GTools.uploadStatistics(GCommon.CLICK,GCommon.APP_SPOT,"parbattech");
//				hide();
//				GLog.e("--------------", "onClickPage");
//			}
//		});
//		pNativeAd.loadData();
		GTools.uploadStatistics(GCommon.REQUEST,GCommon.APP_SPOT,"parbattech");

		int num = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_APP_SPOT_NUM+spotAdPositionId, 0);
		GTools.saveSharedData(GCommon.SHARED_KEY_APP_SPOT_NUM+spotAdPositionId, num+1);
		GTools.saveSharedData(GCommon.SHARED_KEY_APP_SPOT_TIME+spotAdPositionId,GTools.getCurrTime());


		final Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(msg.what == 0x01)
				{
					hide();
				}
			}
		};

		new Thread(){
			public void run() {
				try {
					Thread.sleep(1000*40);
					handler.sendEmptyMessage(0x01);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
	
	public static void hide()
	{
		if(activity!=null)
		{
			activity.finish();
			activity = null;
		}
	}
	


}
