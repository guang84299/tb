package com.qianqi.mylook.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.R;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.presenter.MainPresenter;
import com.qianqi.mylook.view.TopTitleBar;
import com.qianqi.mylook.view.WaveLoadingView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import qiu.niorgai.StatusBarCompat;

public class MainActivity extends BaseActivity{

    @BindView(R.id.waveLoadingView)
    WaveLoadingView waveView;
    @BindView(R.id.usage_time)
    TextView usageView;
    @BindView(R.id.temperature)
    TextView temperatureView;
    @BindView(R.id.battery_capacity)
    TextView capacityView;
    @BindView(R.id.cur_power_mode)
    TextView curModeView;
    @BindView(R.id.config)
    LinearLayout configLayout;
    @BindView(R.id.mode_name)
    TextView modeNameView;
    @BindView(R.id.mode_description)
    TextView modeDescriptionView;
    @BindView(R.id.config_action)
    ImageView configActionView;
    private MainPresenter presenter;
    private int capacity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.qianqi.mylook.R.layout.activity_main);
//        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#0eb3ca"));
        StatusBarCompat.translucentStatusBar(this);
        ButterKnife.bind(this);
        presenter = new MainPresenter(this);
        presenter.load();
//        waveView.setShapeType(WaveLoadingView.ShapeType.CIRCLE);
//        waveView.setTopTitle("Top Title");
//        waveView.setCenterTitleColor(Color.GRAY);
//        waveView.setBottomTitleSize(18);
//        waveView.setProgressValue(80);
//        waveView.setBorderWidth(10);
//        waveView.setAmplitudeRatio(60);
//        waveView.setWaveColor(Color.GRAY);
//        waveView.setBorderColor(Color.GRAY);
//        waveView.setTopTitleStrokeColor(Color.BLUE);
//        waveView.setTopTitleStrokeWidth(3);
        waveView.setAnimDuration(2500);
//        waveView.pauseAnimation();
//        waveView.resumeAnimation();
//        waveView.cancelAnimation();
        waveView.startAnimation();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        presenter = null;
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        waveView.resumeAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        waveView.pauseAnimation();
    }

    //    @OnClick(R.id.auto_start_button) void onAutoStart(){
//        Intent intent = new Intent();
//        intent.setClass(this,AutoStartActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startActivity(intent);
//    }

    @OnClick(R.id.mode) void onSelectMode(){
        Intent intent = new Intent();
        intent.setClass(this,PowerModeSelectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @OnClick(R.id.config) void onConfigMode(){
        Intent intent = new Intent();
        intent.setClass(this,SmartModeConfigActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @OnClick(R.id.setting) void onSetting(){
        Intent intent = new Intent();
        intent.setClass(this,EggshellActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void updateTemperature(int temperature) {
        temperatureView.setText(temperature+getString(R.string.degree_celsius));
    }

    public void updateRemaining(int remaining) {
        usageView.setText(getUsageTime(remaining));
        waveView.setBottomTitle(remaining+"%");
        if(remaining > 95){
            remaining = 95;
        }
        waveView.setProgressValue(remaining);
    }

    public void updatePowerMode(int mode) {
        String modeName = this.getResources().getStringArray(R.array.power_mode_name)[mode];
        String modeDesc = this.getResources().getStringArray(R.array.power_mode_desc)[mode];
        curModeView.setText(modeName);
        modeNameView.setText(modeName);
        modeDescriptionView.setText(modeDesc);
        if(mode == PackageModel.POWER_MODE_SMART){
            configLayout.setClickable(true);
            configLayout.setBackgroundColor(Color.WHITE);
            modeDescriptionView.setTextColor(Color.parseColor("#777777"));
            configActionView.setImageResource(R.drawable.ic_arrow_right);
        }
        else{
            configLayout.setClickable(false);
            configLayout.setBackgroundColor(Color.parseColor("#e7e7e7"));
            modeDescriptionView.setTextColor(Color.parseColor("#979797"));
            configActionView.setImageResource(R.drawable.ic_selected);
        }
    }

    public void updateCapacity(int capacity) {
        this.capacity = capacity;
        capacityView.setText(capacity+getString(R.string.battery_current_unit));
    }

    @Subscribe(
            threadMode = ThreadMode.MAIN
    )
    public void onPowerModeUpdate(BusTag event){
        if(event.tag.equals(BusTag.TAG_CURRENT)){
            usageView.setText((CharSequence) event.data);
        }
    }

    public String getUsageTime(int remaining){
        float usageTime = 1.0f*this.capacity*remaining/100/100;
        int hour = (int) Math.floor(usageTime);
        int minute = (int) ((usageTime-hour)*60);
        if(hour < 0)hour = 0;
        if(minute < 0)minute = 0;
        String s = "";
        if(hour > 0)
            s += hour + getString(R.string.hour);
        if(minute > 0)
            s += minute + getString(R.string.minute);
        return s;
    }
}
