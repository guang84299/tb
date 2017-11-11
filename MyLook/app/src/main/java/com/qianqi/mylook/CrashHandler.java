package com.qianqi.mylook;

import android.content.Context;
import android.util.Log;

import com.qianqi.mylook.utils.CommonUtils;
import com.qianqi.mylook.utils.L;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Administrator on 2017/1/22.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler INSTANCE = new CrashHandler();
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
             mDefaultHandler.uncaughtException(thread, ex);
        } else {
            //判断是否清除数据
            int grestartnum = PreferenceHelper.getInstance().start().getInt("grestartnum",1);
            if(grestartnum>=9)
            {
                CommonUtils.cleanApplicationData(mContext);
                PreferenceHelper.getInstance().start().edit().putInt("grestartnum", 1).commit();
            }
            else
            {
                long grestartnumtime = PreferenceHelper.getInstance().start().getLong("grestartnumtime",0l);
                long now = System.currentTimeMillis();
                if(now - grestartnumtime < 1*60*1000)
                {
                    PreferenceHelper.getInstance().start().edit().putInt("grestartnum", grestartnum+1).commit();
                }
                PreferenceHelper.getInstance().start().edit().putLong("grestartnumtime",now).commit();
            }
            CommonUtils.exit();
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return true;
        }
        Log.e("--------CRASH","--------CRASH",ex);

        return true;
    }
}
