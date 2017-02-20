package com.qianqi.mylook.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.util.DisplayMetrics;

import com.qianqi.mylook.MainApplication;

import java.util.List;

/**
 * Created by Administrator on 2017/1/9.
 */

public class CommonUtils {

    public static int dp2px(float dpValue) {
        final float scale = MainApplication.getInstance().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String getProcessName(Context context){
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process: list) {
            if(process.pid == pid)
            {
                processName = process.processName;
            }
        }
        return processName;
    }

    public static boolean isSameArray(float[] src,float[] dst){
        if(src == null || dst == null)
            return false;
        if(src.length == dst.length){
            for(int i = 0;i<src.length;i++){
                if(src[i] != dst[i]){
                    return false;
                }
            }
            return true;
        }
        else return false;
    }
}
