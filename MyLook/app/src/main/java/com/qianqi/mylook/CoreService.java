package com.qianqi.mylook;

/**
 * Created by Administrator on 2017/1/7.
 */

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.qianqi.mylook.boost.Benchmark;
import com.qianqi.mylook.boost.BoostMonitor;
import com.qianqi.mylook.learning.LearningMonitor;
import com.qianqi.mylook.thread.IThreadPoolManager;
import com.qianqi.mylook.thread.ThreadPoolManager;
import com.qianqi.mylook.utils.L;

public class CoreService extends Service {

    private final static int CORE_SERVICE_ID = -1111;
    private IThreadPoolManager threadPoolManager;
//    private AutoStartMonitor autoStartMonitor;
    private LearningMonitor learningMonitor;
    private BoostMonitor boostMonitor;
    private Benchmark benchmark;

    @Override
    public void onCreate() {
        L.i("CoreService->onCreate");
        super.onCreate();
        threadPoolManager = new ThreadPoolManager();
//        autoStartMonitor = new AutoStartMonitor();
//        autoStartMonitor.start(threadPoolManager);
        learningMonitor = new LearningMonitor();
        learningMonitor.start(threadPoolManager);
        boostMonitor = new BoostMonitor();
        boostMonitor.start(threadPoolManager);
        benchmark = new Benchmark();
//        benchmark.start(threadPoolManager);
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
        benchmark.onDestroy();
        benchmark = null;
    }

    public static class CoreInnerService extends Service {

        @Override
        public void onCreate() {
            L.i("InnerService -> onCreate");
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            L.i("InnerService -> onStartCommand");
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
            L.i("InnerService -> onDestroy");
            super.onDestroy();
        }
    }
}