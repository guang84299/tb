package com.qianqi.mylook.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.qianqi.mylook.R;
import com.qianqi.mylook.adapter.SmartConfigAdapter;
import com.qianqi.mylook.adapter.SmartConfigSection;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.presenter.SmartConfigPresenter;
import com.qianqi.mylook.view.TopTitleBar;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import qiu.niorgai.StatusBarCompat;

public class SmartModeConfigActivity extends BaseActivity{

    @InjectView(R.id.install_tab)
    TextView installTabView;
    @InjectView(R.id.install_indicator)
    ImageView installIndicatorView;
    @InjectView(R.id.system_tab)
    TextView systemTabView;
    @InjectView(R.id.system_indicator)
    ImageView systemIndicatorView;
    @InjectView(R.id.list)
    RecyclerView recyclerView;
    @InjectView(R.id.titleBar)
    TopTitleBar titleBar;
    private SmartConfigAdapter adapter = null;
    private LinearLayoutManager linearLayoutManager;
    private SmartConfigPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_mode_config);
//        StatusBarCompat.setStatusBarColor(this, Color.parseColor("#0eb3ca"));
        StatusBarCompat.translucentStatusBar(this);
        ButterKnife.inject(this);
        titleBar.setLeftVisible(true);
        titleBar.setTitle(R.string.custom_smart_mode);
        titleBar.setImmersive(true,true,R.color.bar_bg);
        presenter = new SmartConfigPresenter(this);

        adapter = new SmartConfigAdapter(this);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {

            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                SmartConfigSection section = (SmartConfigSection) adapter.getItem(position);
                if(!section.isHeader){
                    SmartModeConfigActivity.this.onItemClick(section.t);
                }
            }

            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
//                Toast.makeText(SectionUseActivity.this, "onItemChildClick" + position, Toast.LENGTH_LONG).show();
                SmartConfigSection section = (SmartConfigSection) adapter.getItem(position);
                if(!section.isHeader){
                    SmartModeConfigActivity.this.onItemClick(section.t);
                }
            }


        });
        recyclerView.setAdapter(adapter);
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

    @OnClick(R.id.install_tab) void onSelectInstall(){
        installTabView.setBackgroundColor(Color.parseColor("#ffffff"));
        installTabView.setTextColor(Color.parseColor("#585858"));
        installIndicatorView.setVisibility(View.VISIBLE);
        systemTabView.setBackgroundColor(Color.parseColor("#f7f7f7"));
        systemTabView.setTextColor(Color.parseColor("#888888"));
        systemIndicatorView.setVisibility(View.INVISIBLE);
        presenter.selectInstall();
    }

    @OnClick(R.id.system_tab) void onSelectSystem(){
        installTabView.setBackgroundColor(Color.parseColor("#f7f7f7"));
        installTabView.setTextColor(Color.parseColor("#888888"));
        installIndicatorView.setVisibility(View.INVISIBLE);
        systemTabView.setBackgroundColor(Color.parseColor("#ffffff"));
        systemTabView.setTextColor(Color.parseColor("#585858"));
        systemIndicatorView.setVisibility(View.VISIBLE);
        presenter.selectSystem();
    }

    public void updateData(List<EnhancePackageInfo> data) {
        adapter.setData(data);
        adapter.notifyDataSetChanged();
    }

    public void onItemClick(EnhancePackageInfo packageInfo){
        Intent intent = new Intent();
        intent.setClass(this,AppConfigActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppConfigActivity.EXTRA_PACKAGE_INFO,packageInfo);
        this.startActivity(intent);
    }
}
