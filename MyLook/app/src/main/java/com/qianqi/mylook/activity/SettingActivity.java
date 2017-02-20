package com.qianqi.mylook.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.qianqi.mylook.R;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.presenter.AppConfigPresenter;
import com.qianqi.mylook.view.TopTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import qiu.niorgai.StatusBarCompat;

public class SettingActivity extends BaseActivity{

    @BindView(R.id.not_limit_selected)
    ImageView notLimitSelectedView;
    @BindView(R.id.smart_selected)
    ImageView smartSelectedView;
    @BindView(R.id.titleBar)
    TopTitleBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_config);
//        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#0eb3ca"));
        StatusBarCompat.translucentStatusBar(this);
        ButterKnife.bind(this);
        titleBar.setLeftVisible(true);
        titleBar.setImmersive(true,true,R.color.bar_bg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
