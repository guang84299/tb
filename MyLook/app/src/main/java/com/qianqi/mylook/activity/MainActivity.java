package com.qianqi.mylook.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.R;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.presenter.MainPresenter;
import com.qianqi.mylook.utils.CommonUtils;
import com.qianqi.mylook.utils.L;
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

    @BindView(R.id.title)
    TextView titleView;
    @BindView(R.id.waveLoadingView)
    WaveLoadingView waveView;
    @BindView(R.id.usage_time)
    TextView usageView;
    @BindView(R.id.temperature)
    TextView temperatureView;
//    @BindView(R.id.battery_capacity)
//    TextView capacityView;
    @BindView(R.id.cur_power_mode)
    TextView curModeView;
    @BindView(R.id.config)
    LinearLayout configLayout;
    @BindView(R.id.mode_name)
    TextView modeNameView;
//    @BindView(R.id.mode_description)
//    TextView modeDescriptionView;
    @BindView(R.id.config_action)
    ImageView configActionView;
    private MainPresenter presenter;
    private int capacity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.qianqi.mylook.R.layout.activity_main);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.bar_bg));
//        StatusBarCompat.translucentStatusBar(this);
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
        waveView.setAnimDuration(3500);
//        waveView.pauseAnimation();
//        waveView.resumeAnimation();
//        waveView.cancelAnimation();
        waveView.startAnimation();
        EventBus.getDefault().register(this);

        requestPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        presenter = null;
        EventBus.getDefault().unregister(this);
        CommonUtils.exit();
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
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        waveView.setCenterTitle(remaining+"%");
        if(remaining > 95){
            remaining = 95;
        }
        waveView.setProgressValue(remaining);
    }

    public void updatePowerMode(int mode) {
        String modeName = this.getResources().getStringArray(R.array.power_mode_name)[mode];
//        String modeDesc = this.getResources().getStringArray(R.array.power_mode_desc)[mode];
        curModeView.setText(modeName);
//        modeNameView.setText(modeName);
//        modeDescriptionView.setText(modeDesc);
        if(mode == PackageModel.POWER_MODE_SMART){
            configLayout.setVisibility(View.VISIBLE);
            configLayout.setClickable(true);
            configLayout.setBackgroundColor(Color.WHITE);
//            modeDescriptionView.setTextColor(Color.parseColor("#777777"));
            configActionView.setImageResource(R.drawable.ic_arrow_right);
        }
        else{
            configLayout.setVisibility(View.INVISIBLE);
//            configLayout.setClickable(false);
//            configLayout.setBackgroundColor(Color.parseColor("#e7e7e7"));
//            modeDescriptionView.setTextColor(Color.parseColor("#979797"));
//            configActionView.setImageResource(R.drawable.ic_selected);
        }
    }

    public void updateCapacity(int capacity) {
        this.capacity = capacity;
//        capacityView.setText(capacity+getString(R.string.battery_current_unit));
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

    public final static int REQUEST_READ_PHONE_STATE = 1;
    public final static int REQUEST_READ_CALL_LOG = 2;
    public final static int REQUEST_ACCESS_COARSE_LOCATION = 3;
    public final static int REQUEST_ACCESS_FINE_LOCATION = 4;
    public final static int REQUEST_READ_EXTERNAL_STORAGE = 5;
    public final static int REQUEST_WRITE_EXTERNAL_STORAGE = 6;
    private void requestPermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_READ_CALL_LOG);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            default:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this,"成功获取权限",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this,"获取权限失败",Toast.LENGTH_SHORT).show();
                }
                requestPermission();
                break;
        }

    }
}
