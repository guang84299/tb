package com.qianqi.mylook.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qianqi.mylook.R;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.presenter.AppConfigPresenter;
import com.qianqi.mylook.presenter.ModeSelectPresenter;
import com.qianqi.mylook.view.TopTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import qiu.niorgai.StatusBarCompat;

public class AppConfigActivity extends BaseActivity{

    public static final String EXTRA_PACKAGE_INFO = "package_info";

    @BindView(R.id.not_limit_selected)
    ImageView notLimitSelectedView;
    @BindView(R.id.smart_selected)
    ImageView smartSelectedView;
    @BindView(R.id.titleBar)
    TopTitleBar titleBar;
    private AppConfigPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);
//        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#0eb3ca"));
        StatusBarCompat.translucentStatusBar(this);
        ButterKnife.bind(this);
        titleBar.setLeftVisible(true);
        titleBar.setImmersive(true,true,R.color.bar_bg);
        presenter = new AppConfigPresenter(this);
        if(getIntent() != null){
            EnhancePackageInfo p = getIntent().getParcelableExtra(EXTRA_PACKAGE_INFO);
            presenter.load(p);
        }
        else{
            finish();
        }
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

    @OnClick(R.id.not_limit) void onSelectNotLimit(){
        presenter.setMode(true);
        finish();
    }

    @OnClick(R.id.smart) void onSelectSmart(){
        presenter.setMode(false);
        finish();
    }

    public void updateTitle(String title){
        titleBar.setTitle(title);
    }

    public void updateState(boolean notLimit) {
        if(notLimit){
            notLimitSelectedView.setVisibility(View.VISIBLE);
            smartSelectedView.setVisibility(View.INVISIBLE);
        }
        else {
            notLimitSelectedView.setVisibility(View.INVISIBLE);
            smartSelectedView.setVisibility(View.VISIBLE);
        }
    }

}
