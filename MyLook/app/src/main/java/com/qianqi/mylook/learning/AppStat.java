package com.qianqi.mylook.learning;

import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.PreferenceHelper;
import com.qianqi.mylook.utils.L;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/1/16.
 */

public class AppStat {

    /*
    * 输入：时间片，是否周六，是否周日，网络状态，最近启动间隔(单位是时间片)//待补：当前位置，耳机状态，是否休息日
    * 输出：是否启动
    * 预测：启动概率
    * */
    public static final int MAX_CACHE_RECORDS = 5;
    public static final float START_TIME_INTERVAL = 10*60*1000f;
    private String packageName;
    private List<RecordItem> records = new ArrayList<>(MAX_CACHE_RECORDS);
    private long lastStartTime = -1;

    public AppStat(String packageName){
        this.packageName = packageName;
        lastStartTime = PreferenceHelper.getInstance().start().getLong(packageName,lastStartTime);
    }

    public String getPackageName() {
        return packageName;
    }

    public float[] getInput(TimeInfo timeInfo, NetworkInfo networkInfo){
        float outputInterval = 0;
        long curTime = timeInfo.getTime();
        if(lastStartTime > 0 && lastStartTime < curTime){
            long interval = curTime - lastStartTime;
            if(interval <= START_TIME_INTERVAL){
                outputInterval = 1;
            }
            else{
                outputInterval = START_TIME_INTERVAL/interval;
            }
        }
        float[] input = new float[7];
        timeInfo.fill(input,0);
        networkInfo.fill(input,3);
        input[6] = outputInterval;
        return input;
    }

    public void addRecord(TimeInfo timeInfo, NetworkInfo networkInfo, String top) {
        long curTime = timeInfo.getTime();
        float[] input = getInput(timeInfo,networkInfo);
        boolean isTop = top.equals(packageName);
        if(isTop){
            lastStartTime = curTime;
            PreferenceHelper.getInstance().start().edit().putLong(packageName,curTime).apply();
        }
        int y = isTop?1:0;
//        L.d(packageName+":"+ Arrays.toString(input) +"#"+y);
        RecordItem record = new RecordItem(packageName,timeInfo.getDate(),input,y);
        records.add(record);
        checkSave();
    }

    private void checkSave(){
        if(records.size() < MAX_CACHE_RECORDS)
            return;
        flush();
    }

    public void flush() {
        for(RecordItem record:records){
            UsageCache.write(MainApplication.getInstance(),record);
        }
        records.clear();
    }
}
