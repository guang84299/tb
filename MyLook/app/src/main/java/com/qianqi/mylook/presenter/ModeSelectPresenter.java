package com.qianqi.mylook.presenter;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.PreferenceHelper;
import com.qianqi.mylook.activity.PowerModeSelectActivity;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.model.PackageModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/1/21.
 */

public class ModeSelectPresenter {
    private PowerModeSelectActivity activity;
    private int curPowerMode;

    public ModeSelectPresenter(PowerModeSelectActivity activity){
        this.activity = activity;
        EventBus.getDefault().register(this);
    }

    public void load(){
        curPowerMode = PackageModel.getInstance(activity).getPowerMode();
        this.activity.updatePowerMode(curPowerMode);
    }

    public void setMode(int mode){
        PackageModel.getInstance(activity).setPowerMode(mode);
    }

    @Subscribe(
            threadMode = ThreadMode.MAIN
    )
    public void onPowerModeUpdate(BusTag event){
        if(event.tag.equals(BusTag.TAG_POWER_MODE_UPDATE)){
            curPowerMode = (int) event.data;
            this.activity.updatePowerMode(curPowerMode);
        }
    }

    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    public void onPause() {

    }
}
