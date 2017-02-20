package com.qianqi.mylook.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qianqi.mylook.R;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.presenter.MainPresenter;
import com.qianqi.mylook.presenter.ModeSelectPresenter;
import com.qianqi.mylook.view.TopTitleBar;
import com.qianqi.mylook.view.WaveLoadingView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import qiu.niorgai.StatusBarCompat;

public class PowerModeSelectActivity extends BaseActivity{

    @BindView(R.id.game_selected)
    ImageView gameSelectedView;
    @BindView(R.id.smart_selected)
    ImageView smartSelectedView;
    @BindView(R.id.performance_selected)
    ImageView performanceSelectedView;
    @BindView(R.id.titleBar)
    TopTitleBar titleBar;
    private ModeSelectPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_mode_select);
//        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#0eb3ca"));
        StatusBarCompat.translucentStatusBar(this);
        ButterKnife.bind(this);
//        titleBar.setLeftText("");
        titleBar.setLeftVisible(true);
        titleBar.setTitle(R.string.power_saving_mode);
        titleBar.setImmersive(true,true,R.color.bar_bg);
        presenter = new ModeSelectPresenter(this);
        presenter.load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
        presenter = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @OnClick(R.id.game_mode) void onGameMode(){
        presenter.setMode(PackageModel.POWER_MODE_GAME);
        finish();
    }

    @OnClick(R.id.smart_mode) void onSmartMode(){
        presenter.setMode(PackageModel.POWER_MODE_SMART);
        finish();
    }

    @OnClick(R.id.performance_mode) void onPerformanceMode(){
        presenter.setMode(PackageModel.POWER_MODE_PERFORMANCE);
        finish();
    }

    public void updatePowerMode(int mode) {
        gameSelectedView.setVisibility(View.INVISIBLE);
        smartSelectedView.setVisibility(View.INVISIBLE);
        performanceSelectedView.setVisibility(View.INVISIBLE);
        if(mode == PackageModel.POWER_MODE_GAME){
            gameSelectedView.setVisibility(View.VISIBLE);
        }
        else if(mode == PackageModel.POWER_MODE_SMART){
            smartSelectedView.setVisibility(View.VISIBLE);
        }
        else if(mode == PackageModel.POWER_MODE_PERFORMANCE){
            performanceSelectedView.setVisibility(View.VISIBLE);
        }
    }

}
