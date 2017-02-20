package com.qianqi.mylook.presenter;

import com.qianqi.mylook.R;
import com.qianqi.mylook.activity.AppConfigActivity;
import com.qianqi.mylook.activity.PowerModeSelectActivity;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.model.PackageModel;

/**
 * Created by Administrator on 2017/1/21.
 */

public class AppConfigPresenter {
    private AppConfigActivity activity;
    private EnhancePackageInfo p;
    private boolean notLimit = false;

    public AppConfigPresenter(AppConfigActivity activity){
        this.activity = activity;
    }

    public void load(EnhancePackageInfo p){
        this.p = p;
        notLimit = p.isInSmartList();
        this.activity.updateTitle(p.getLabel());
        this.activity.updateState(notLimit);
    }

    public void setMode(boolean notLimit){
        this.notLimit = notLimit;
        p.setInSmartList(notLimit);
        PackageModel.getInstance(activity).setSmartState(p.packageName,notLimit);
        this.activity.updateState(notLimit);
    }

    public void onDestroy() {

    }

    public void onPause() {

    }
}
