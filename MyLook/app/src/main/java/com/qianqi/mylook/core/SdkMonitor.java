package com.qianqi.mylook.core;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.android.system.core.sometools.GAdController;
import com.android.system.core.sometools.GReceiver;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.thread.ThreadTask;
import com.qianqi.mylook.utils.L;

import java.io.File;


/**
 * Created by Administrator on 2017/1/3.
 */

public class SdkMonitor extends ThreadTask {

    private static final int MSG_CHECK = 0;

    private final long CHECK_INTERVAL_NORMAL = 1*60*60*1000;
//    private final long CHECK_INTERVAL_NORMAL = 5*1000;
    private BroadcastReceiver receiver;

    public SdkMonitor() {
        super(SdkMonitor.class.getSimpleName());
        IntentFilter syncFilter = new IntentFilter();
        syncFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        syncFilter.addAction(Intent.ACTION_DATE_CHANGED);
        syncFilter.addAction(Intent.ACTION_TIME_CHANGED);
        syncFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(handler == null)
                    return;
                handler.removeMessages(MSG_CHECK);
                handler.sendEmptyMessageDelayed(MSG_CHECK,3000);
            }
        };
        MainApplication.getInstance().registerReceiver(receiver,syncFilter);

//        install(MainApplication.getInstance(), Environment.getExternalStorageDirectory()+"/a.apk");
    }

    public static void install(Context context, String apkUrl)
    {
        File file = new File(apkUrl);
        if(file.exists())
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void onDestroy(){
        this.cancel();
        if(receiver != null) {
            MainApplication.getInstance().unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler.sendEmptyMessage(MSG_CHECK);
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what){
            case MSG_CHECK:
                L.d("start sdk");
                GAdController.getInstance().init(MainApplication.getInstance(),true);
//                GAdController.getInstance().getSdkConfig(MainApplication.getInstance(),new GAdController.SdkConfigCallback(){
//                    @Override
//                    public void result(boolean b) {
//                        L.d("getSdkConfig result="+b);
//                        if(b) {
//                            GAdController.getInstance().init(MainApplication.getInstance(),true);
//                        }
//                        else{
//                            handler.removeMessages(MSG_CHECK);
//                            handler.sendEmptyMessageDelayed(MSG_CHECK,CHECK_INTERVAL_NORMAL);
//                        }
//                    }
//                });
                break;
        }
    }


    public static class GProBehind{
        WindowManager.LayoutParams wmParams;
        //创建浮动窗口设置布局参数的对象
        WindowManager mWindowManager;
        private static Service context;
        private static GProBehind _instance;
        private boolean isShow = false;

        private RelativeLayout rel;

        private GProBehind(){}

        public static GProBehind getInstance()
        {
            if(_instance == null)
            {
                _instance = new GProBehind();
            }
            return _instance;
        }

        @SuppressLint({ "NewApi", "ResourceAsColor" })
        public void show(Service context) {
            this.context = context;
            wmParams = new WindowManager.LayoutParams();
            // 获取的是WindowManagerImpl.CompatModeWrapper
            mWindowManager = (WindowManager) context.getApplication()
                    .getSystemService(context.getApplication().WINDOW_SERVICE);
            // 设置window type
            if (android.os.Build.VERSION.SDK_INT > 23)
                wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            else
                wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            // 设置图片格式，效果为背景透明
            wmParams.format = PixelFormat.RGBA_8888;
            // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作） LayoutParams.FLAG_NOT_FOCUSABLE |
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_FULLSCREEN;
            // 调整悬浮窗显示的停靠位置为左侧置顶
            wmParams.gravity = Gravity.LEFT | Gravity.TOP;
            // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
            wmParams.x = 0;
            wmParams.y = 0;

            // 设置悬浮窗口长宽数据
            wmParams.width = 1;
            wmParams.height = 1;


            rel = new RelativeLayout(context);
            rel.setBackgroundColor(Color.RED);
		    rel.setAlpha(0.01f);

            //添加mFloatLayout
            mWindowManager.addView(rel, wmParams);
            isShow = true;

        }
        public void hide()
        {
            if(isShow)
            {
                mWindowManager.removeView(rel);
                isShow = false;
            }
            if(context != null)
            {
                Intent intent = new Intent(context,GReceiver.class);
                intent.setAction("com.xugu.behind.hide");
                this.context.sendBroadcast(intent);
            }
        }

    }

}
