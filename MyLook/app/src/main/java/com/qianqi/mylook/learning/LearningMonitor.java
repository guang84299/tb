package com.qianqi.mylook.learning;

import android.os.Message;
import android.view.accessibility.AccessibilityEvent;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/3.
 *
 * 每次灭屏后2分钟检查是否要进行训练，亮屏则取消检查，整体训练最小间隔时间为1天，运行中应用的训练最小间隔时间为2小时
 */

public class LearningMonitor extends ThreadTask {

    public static final long PREDICT_RUNNING_INTERVAL = 2*60*60*1000;
    public static final long PREDICT_TOTAL_INTERVAL = 24*60*60*1000;
    public static final long PREDICT_CHECK_INTERVAL = 2*60*1000;

    private static final int MSG_START = 0;
    private static final int MSG_TIME_COUNT = 1;
    private static final int MSG_NETWORK_CHANGED = 2;
    private static final int MSG_TASK_CHANGED = 3;
    private static final int MSG_PLUG_CHANGED = 4;
    private static final int MSG_PACKAGE_CHANGED = 5;
    private static final int MSG_FIT = 6;

    private final long TIME_COUNT = 10*60*1000;
    private UsageManager manager;
    private long lastFitTime = 0;

    public LearningMonitor() {
        super(LearningMonitor.class.getSimpleName());
        EventBus.getDefault().register(this);
//        L.d("create learning monitor");
    }

    public void onDestroy(){
        EventBus.getDefault().unregister(this);
        this.cancel();
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler.sendEmptyMessage(MSG_START);
        handler.sendEmptyMessage(MSG_FIT);
//        handler.sendEmptyMessageDelayed(MSG_TIME_COUNT,TIME_COUNT);
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what){
            case MSG_START:
                manager = new UsageManager();
                break;
            case MSG_PACKAGE_CHANGED:
                if(lastFitTime == 0){
                    checkFit();
                }
                if(manager == null)
                    return;
                manager.onPackageChanged();
                break;
            case MSG_NETWORK_CHANGED:
                if(manager == null)
                    return;
                manager.onNetworkChanged();
                break;
            case MSG_TIME_COUNT:
                handler.sendEmptyMessageDelayed(MSG_TIME_COUNT,TIME_COUNT);
                if(manager == null)
                    return;
                manager.onTimeChanged();
                break;
            case MSG_TASK_CHANGED:
                if(manager == null)
                    return;
                manager.onTaskChanged();
                break;
            case MSG_FIT:
                checkFit();
                break;
        }
    }

    private void checkFit(){
        if(!PackageModel.getInstance(MainApplication.getInstance()).hasInit())
            return;
        long time = System.currentTimeMillis();
        long interval = time-lastFitTime;
//                L.d("fit interval:"+interval);
        EventBus.getDefault().post(new BusTag(BusTag.TAG_FLUSH_LEARNING_DATA));
        if(interval > PREDICT_TOTAL_INTERVAL){
            lastFitTime = time;
            PackageFilter filter = new PackageFilter.Builder().persistent(false).qianqi(false).build();
//        PackageFilter filter = new PackageFilter.Builder().hasActivity(true).persistent(false).build();
            List<EnhancePackageInfo> pkgList = PackageModel.getInstance(MainApplication.getInstance()).getPackageList(filter);
            UsagePredictor.getInstance().updateFit(pkgList);
        }
        else if(interval > PREDICT_RUNNING_INTERVAL){
            lastFitTime = time;
            PackageFilter filter = new PackageFilter.Builder().running(true).persistent(false).qianqi(false).build();
            List<EnhancePackageInfo> runningPackageList = PackageModel.getInstance(MainApplication.getInstance()).getPackageList(filter);
            UsagePredictor.getInstance().updateFit(runningPackageList);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void onTopTaskChanged(BusTag event){
        if(event.tag.equals(BusTag.TAG_TOP_TASK_CHANGED)){
            handler.sendEmptyMessage(MSG_TASK_CHANGED);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void onPackageChanged(BusTag event){
        if(event.tag.equals(BusTag.TAG_PACKAGE_UPDATE)) {
            handler.sendEmptyMessage(MSG_PACKAGE_CHANGED);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void onNetworkChanged(BusTag event){
        if(event.tag.equals(BusTag.TAG_NETWORK_CHANGED)) {
            handler.sendEmptyMessage(MSG_NETWORK_CHANGED);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void onFlushData(BusTag event){
        if(event.tag.equals(BusTag.TAG_FLUSH_LEARNING_DATA)) {
            if (manager == null)
                return;
            manager.onFlushData();
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void screenOn(BusTag event) {
        if(event.tag.equals(BusTag.TAG_SCREEN_ON)) {
            if(handler == null)
                return;
//        L.d("remove fit");
            handler.removeMessages(MSG_FIT);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void screenOff(BusTag event) {
        if(event.tag.equals(BusTag.TAG_SCREEN_OFF)) {
            if(handler == null)
                return;
//        L.d("post fit");
            handler.sendEmptyMessageDelayed(MSG_FIT,PREDICT_CHECK_INTERVAL);
        }
    }

    @Subscribe(
            threadMode = ThreadMode.POSTING
    )
    public void userPresent(BusTag event) {
        if(event.tag.equals(BusTag.TAG_USER_PRESENT)) {
            if(handler == null)
                return;
//        L.d("remove fit");
            handler.removeMessages(MSG_FIT);
        }
    }
}
