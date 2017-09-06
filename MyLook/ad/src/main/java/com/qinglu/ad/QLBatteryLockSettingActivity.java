package com.qinglu.ad;

import com.guang.client.GCommon;
import com.guang.client.tools.GTools;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class QLBatteryLockSettingActivity extends Activity{
	private Activity activity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		activity = this;
		
		LayoutInflater inflater = LayoutInflater.from(getApplication());
		View view_setting = inflater.inflate((Integer) GTools.getResourceId(
				"qew_battery_lock_setting", "layout"), null);
		
		RelativeLayout.LayoutParams layoutGrayParams = new RelativeLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		layoutGrayParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		this.setContentView(view_setting,layoutGrayParams);

		ImageView iv_return = (ImageView) view_setting.findViewById((Integer)GTools.getResourceId("iv_return", "id"));
		final RadioButton rb_set_0 = (RadioButton) view_setting.findViewById((Integer)GTools.getResourceId("rb_set_0", "id"));
		final RadioButton rb_set_1 = (RadioButton) view_setting.findViewById((Integer)GTools.getResourceId("rb_set_1", "id"));
		final RadioButton rb_set_2 = (RadioButton) view_setting.findViewById((Integer)GTools.getResourceId("rb_set_2", "id"));
		final RadioButton rb_set_3 = (RadioButton) view_setting.findViewById((Integer)GTools.getResourceId("rb_set_3", "id"));
		final RadioButton rb_set_4 = (RadioButton) view_setting.findViewById((Integer)GTools.getResourceId("rb_set_4", "id"));
		rb_set_1.setTag(2);
		rb_set_2.setTag(3);
		rb_set_3.setTag(4);
		rb_set_4.setTag(5);
		rb_set_0.setTag(1);
		
		iv_return.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				activity.finish();
			}
		});
		
		OnClickListener listener = new OnClickListener() 
		{
			@Override
			public void onClick(View v) {
				rb_set_1.setChecked(false);
				rb_set_2.setChecked(false);
				rb_set_3.setChecked(false);
				rb_set_4.setChecked(false);
				rb_set_0.setChecked(false);
				
				RadioButton btn = (RadioButton) v;	
				boolean b = btn.isChecked();
				btn.setChecked(!b);
				
				if(!b)
				{
					int type = (Integer) btn.getTag();
					GTools.saveSharedData(GCommon.SHARED_KEY_LOCK_SAVE_TYPE, type);
					GTools.saveSharedData(GCommon.SHARED_KEY_LOCK_SAVE_TIME, GTools.getCurrTime());
				}				
				activity.finish();
			}
			
		};
		rb_set_1.setOnClickListener(listener);
		rb_set_2.setOnClickListener(listener);
		rb_set_3.setOnClickListener(listener);
		rb_set_4.setOnClickListener(listener);
		rb_set_0.setOnClickListener(listener);	
		
		int type = GTools.getSharedPreferences().getInt(GCommon.SHARED_KEY_LOCK_SAVE_TYPE, 1);	
		if(type == 2)
		{
			rb_set_1.setChecked(true);
		}
		else if(type == 3)
		{
			rb_set_2.setChecked(true);
		}
		else if(type == 4)
		{
			rb_set_3.setChecked(true);
		}
		else if(type == 5)
		{
			rb_set_4.setChecked(true);
		}
		else if(type == 1)
		{
			rb_set_0.setChecked(true);
		}
				
	}
	
	
}
