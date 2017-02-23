package com.qianqi.mylook.presenter;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.PreferenceHelper;
import com.qianqi.mylook.activity.MainActivity;
import com.qianqi.mylook.model.PackageModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Administrator on 2017/1/20.
 */

public class MainPresenter{

    private MainActivity activity;
    private BatteryHelper batteryHelper;
    private int curPowerMode;

    public MainPresenter(MainActivity activity){
        EventBus.getDefault().register(this);
        this.activity = activity;
        batteryHelper = new BatteryHelper();
        batteryHelper.registerReceiver(activity);
    }

    public void load(){
        onBatteryChanged(new BusTag(BusTag.TAG_BATTERY_CHANGED));
        if(BatteryHelper.isValid(batteryHelper.getCapacity())){
            activity.updateCapacity(batteryHelper.getCapacity());
        }
        curPowerMode = PackageModel.getInstance(activity).getPowerMode();
        activity.updatePowerMode(curPowerMode);
    }

    public void onDestroy(){
        EventBus.getDefault().unregister(this);
        batteryHelper.unregisterReceiver(activity);
        batteryHelper = null;
    }

    public int getTemperature(){
        return batteryHelper.getTemperature();
    }

    public int getCapacity(){
        return batteryHelper.getCapacity();
    }

    @Subscribe(
            threadMode = ThreadMode.MAIN
    )
    public void onBatteryChanged(BusTag event) {
        if(event.tag.equals(BusTag.TAG_BATTERY_CHANGED)){
            if(BatteryHelper.isValid(batteryHelper.getTemperature())){
                activity.updateTemperature(batteryHelper.getTemperature());
            }
            if(BatteryHelper.isValid(batteryHelper.getRemaining())){
                activity.updateRemaining(batteryHelper.getRemaining());
            }
        }
    }

    @Subscribe(
            threadMode = ThreadMode.MAIN
    )
    public void onPowerModeUpdate(BusTag event){
        if(event.tag.equals(BusTag.TAG_POWER_MODE_UPDATE)){
            curPowerMode = PackageModel.getInstance(MainApplication.getInstance()).getPowerMode();;
            this.activity.updatePowerMode(curPowerMode);
        }
    }

    public int getRemaining() {
        return batteryHelper.getRemaining();
    }
}
