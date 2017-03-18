package com.qianqi.mylook.boost;

import android.os.Message;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.bean.EnhancePackageInfo;
import com.qianqi.mylook.model.PackageFilter;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.thread.ThreadTask;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Administrator on 2017/1/3.
 */

public class BoostMonitor extends ThreadTask {

    private static final int MSG_CHECK_MEM = 0;
    private static final int MSG_CHECK_AUTOSTART = 1;

    private final long CHECK_MEM_INTERVAL_STANDBY = 60*60*1000;
    private final long CHECK_MEM_INTERVAL_NORMAL = 15*1000;
    private final long CHECK_MEM_INTERVAL_HIGH = 5*1000;
    private MemHelper memHelper;
    private BoostHelper boostHelper;

    public BoostMonitor() {
        super(BoostMonitor.class.getSimpleName());
        EventBus.getDefault().register(this);
        memHelper = new MemHelper();
        boostHelper = new BoostHelper();
//        L.d("create boost monitor");
    }

    public void onDestroy(){
        EventBus.getDefault().unregister(this);
        this.cancel();
        memHelper.onDestroy();
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        if(boostHelper.getMode() != PackageModel.POWER_MODE_PERFORMANCE)
            handler.sendEmptyMessage(MSG_CHECK_MEM);
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what){
            case MSG_CHECK_MEM:
                EventBus.getDefault().post(new BusTag(BusTag.TAG_REQUEST_PROCESS_UPDATE));
                if(memHelper.timeToBoost()){
                    if(boostHelper.boost()){
                        handler.sendEmptyMessageDelayed(MSG_CHECK_MEM,CHECK_MEM_INTERVAL_HIGH);
                        break;
                    }
                }
                handler.sendEmptyMessageDelayed(MSG_CHECK_MEM,CHECK_MEM_INTERVAL_NORMAL);
                break;
            case MSG_CHECK_AUTOSTART:
                PackageFilter filter = new PackageFilter.Builder().persistent(false).qianqi(false).build();
                List<EnhancePackageInfo> packageInfoList = PackageModel.getInstance(MainApplication.getInstance()).getPackageList(filter);
                if(packageInfoList == null){
                    return;
                }
                for(EnhancePackageInfo p:packageInfoList){
                    if(!p.isRunning && p.isStopping()){
                        p.setStopping(false);
                    }
                    if(p.isRunning && !p.isStopping() && !p.allowAutoStart){
                        PackageModel.getInstance(MainApplication.getInstance()).setAutoStart(p.packageName,true);
                        L.d("enable autostart:"+p.packageName);
                    }
                }
                break;
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void onModeChanged(BusTag event){
        if(event.tag.equals(BusTag.TAG_POWER_MODE_UPDATE)){
            int mode = PackageModel.getInstance(MainApplication.getInstance()).getPowerMode();
            if(mode == PackageModel.POWER_MODE_PERFORMANCE){
                handler.removeMessages(MSG_CHECK_MEM);
            }
            else{
                handler.removeMessages(MSG_CHECK_MEM);
                handler.sendEmptyMessage(MSG_CHECK_MEM);
            }
            boostHelper.updateMode(mode);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void onProcessUpdate(BusTag event){
        if(event.tag.equals(BusTag.TAG_PACKAGE_PROCESS_UPDATE)){
            if(boostHelper.getMode() == PackageModel.POWER_MODE_PERFORMANCE)
                return;
            handler.sendEmptyMessage(MSG_CHECK_AUTOSTART);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.BACKGROUND
    )
    public void onMasterConnected(BusTag event){
        if(event.tag.equals(BusTag.TAG_MASTER_CONNECTED)){
            if(memHelper != null){
                memHelper.updateMinFreeMem();
            }
        }
    }

}
