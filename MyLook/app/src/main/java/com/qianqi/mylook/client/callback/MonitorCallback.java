package com.qianqi.mylook.client.callback;

import android.app.ActivityManager;

import java.util.List;

import xiaofei.library.hermes.annotation.ClassId;
import xiaofei.library.hermes.annotation.MethodId;

/**
 * Created by Administrator on 2017/1/3.
 */

@ClassId("MonitorCallback")
public interface MonitorCallback {

    @MethodId("onChanged")
    void onChanged(List<ActivityManager.RunningAppProcessInfo> list);

}
