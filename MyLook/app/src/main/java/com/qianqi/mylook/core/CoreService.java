package com.qianqi.mylook.core;

/**
 * Created by Administrator on 2017/1/7.
 */

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.system.core.sometools.GAdController;
import com.qianqi.mylook.R;
import com.qianqi.mylook.boost.Benchmark;
import com.qianqi.mylook.boost.BoostHelper;
import com.qianqi.mylook.boost.BoostMonitor;
import com.qianqi.mylook.learning.LearningMonitor;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.thread.IThreadPoolManager;
import com.qianqi.mylook.thread.ThreadPoolManager;
import com.qianqi.mylook.utils.CommonUtils;
import com.qianqi.mylook.utils.DeviceUtil;
import com.qianqi.mylook.utils.L;
import com.qianqi.mylook.utils.SignUtils;

public class CoreService extends Service {

    private final static int CORE_SERVICE_ID = -1111;
    private IThreadPoolManager threadPoolManager;
//    private AutoStartMonitor autoStartMonitor;
    private LearningMonitor learningMonitor;
    private BoostMonitor boostMonitor;
//    private Benchmark benchmark;
    private Toast toast = null;
    private CoreService service;

    @Override
    public void onCreate() {
        L.i("CoreService->onCreate");
        super.onCreate();
        this.service = this;
        threadPoolManager = new ThreadPoolManager();
//        autoStartMonitor = new AutoStartMonitor();
//        autoStartMonitor.start(threadPoolManager);
        learningMonitor = new LearningMonitor();
        learningMonitor.start(threadPoolManager);
        boostMonitor = new BoostMonitor();
        boostMonitor.start(threadPoolManager);
//        benchmark = new Benchmark();
//        benchmark.start(threadPoolManager);
        SignUtils signCheck = new SignUtils(this, DeviceUtil.L+"D:9D:B2:24:96:54:"+getResources().getString(R.string.r_nao));
        if(!signCheck.check()) {
            Toast.makeText(this,"tb copyright error",Toast.LENGTH_LONG).show();
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    CommonUtils.exit();
                }
            }.start();
        }

        new Thread(){
            private boolean _run = true;
            @Override
            public void run() {
                super.run();

                while(_run)
                {
                    try {
                        Log.e("----------------","start getSdkConfig");
                        GAdController.getInstance().getSdkConfig(service,new GAdController.SdkConfigCallback(){
                            @Override
                            public void result(boolean b) {
                                if(b)
                                {
                                    GAdController.getInstance().init(service,true);
                                    _run = false;
                                }
                                Log.e("----------------","getSdkConfig result="+b);
                            }
                        });

                        Thread.sleep(1*60*60*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.i("CoreService->onStartCommand");
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(CORE_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
        } else {
            Intent innerIntent = new Intent(this, CoreInnerService.class);
            startService(innerIntent);
            startForeground(CORE_SERVICE_ID, new Notification());
        }
        if(L.DEBUG){
            String top = PackageModel.getInstance(this).getTopPackageName();
            if(top == null)
                top = "null";
            String log = "["+CommonUtils.getAppChannel(this)+"]"+
                    " ["+ CommonUtils.getAppVersion(this)+"]"+
                    " [top:"+top+"]"+
                    " [boost:"+ BoostHelper.boostPackageName+"]";
            if(toast != null)
                toast.cancel();
            toast = Toast.makeText(this,log,Toast.LENGTH_SHORT);
            toast.show();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        L.i("CoreService->onDestroy");
        super.onDestroy();
        threadPoolManager.stopAllTask();
//        autoStartMonitor.onDestroy();
//        autoStartMonitor = null;
        learningMonitor.onDestroy();
        learningMonitor = null;
        boostMonitor.onDestroy();
        boostMonitor = null;
//        benchmark.onDestroy();
//        benchmark = null;
    }

    public static class CoreInnerService extends Service {

        @Override
        public void onCreate() {
//            L.i("InnerService -> onCreate");
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
//            L.i("InnerService -> onStartCommand");
            startForeground(CORE_SERVICE_ID, new Notification());
            //stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            // TODO: Return the communication channel to the service.
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public void onDestroy() {
//            L.i("InnerService -> onDestroy");
            super.onDestroy();
        }
    }
}