package com.qianqi.mylook.boost;

import android.os.Message;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.learning.UsageManager;
import com.qianqi.mylook.thread.ThreadTask;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/2/13.
 */

public class Benchmark extends ThreadTask {

    public static final int MSG_UPDATE_CURRENT = 0;

    public Benchmark() {
        super(Benchmark.class.getSimpleName());
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler.sendEmptyMessage(MSG_UPDATE_CURRENT);
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_CURRENT:
                String s = getCurrent();
                L.d(s);
                EventBus.getDefault().post(new BusTag(BusTag.TAG_CURRENT,s));
                handler.sendEmptyMessage(MSG_UPDATE_CURRENT);
                break;
        }
    }

    /**
     * 获取当前电流
     */
    private String getCurrent() {
        String result = "null";
        try {
            Class systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getDeclaredMethod("get", String.class);
            String platName = (String) get.invoke(null, "ro.hardware");
            if (platName.startsWith("mt") || platName.startsWith("MT")) {
                String filePath = "/sys/class/power_supply/battery/device/FG_Battery_CurrentConsumption";
                // MTK平台该值不区分充放电，都为负数，要想实现充放电电流增加广播监听充电状态即可
                result = "" + Math.round(getMeanCurrentVal(filePath, 10, 50) / 10.0f) + "mA";
//                result += ", 电压为：" + readFile("/sys/class/power_supply/battery/batt_vol", 0) + "mV";
            } else if (platName.startsWith("qcom")) {
                String filePath ="/sys/class/power_supply/battery/current_now";
                int current = Math.round(getMeanCurrentVal(filePath, 10, 50) / 10.0f);
                int voltage = readFile("/sys/class/power_supply/battery/voltage_now", 0) / 1000;
                // 高通平台该值小于0时电池处于放电状态，大于0时处于充电状态
                if (current < 0) {
                    result = "充电电流为：" + (-current) + "mA, 电压为：" + voltage + "mV";
                } else {
                    result = "放电电流为：" + current + "mA, 电压为：" + voltage + "mV";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取平均电流值
     * 获取 filePath 文件 totalCount 次数的平均值，每次采样间隔 intervalMs 时间
     */
    private float getMeanCurrentVal(String filePath, int totalCount, int intervalMs) {
        float meanVal = 0.0f;
        if (totalCount <= 0) {
            return 0.0f;
        }
        for (int i = 0; i < totalCount; i++) {
            try {
                float f = Float.valueOf(readFile(filePath, 0));
                meanVal += f / totalCount;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (intervalMs <= 0) {
                continue;
            }
            try {
                Thread.sleep(intervalMs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return meanVal;
    }

    private int readFile(String path, int defaultValue) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    path));
            String s = bufferedReader.readLine();
//            L.d("read:"+s);
            long i = Long.parseLong(s);
            bufferedReader.close();
            return (int) (i/1000000);
        } catch (Exception localException) {
            L.d("read",localException);
        }
        return defaultValue;
    }

    public void onDestroy() {
        this.cancel();
    }
}
