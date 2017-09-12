package com.android.system.core.sometools;





import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class GService extends Service{
	private final static int CORE_SERVICE_ID = -1111;
	private Context context;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	 @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (Build.VERSION.SDK_INT < 18) {
//            startForeground(CORE_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
//        } else {
//            Intent innerIntent = new Intent(this, CoreInnerService.class);
//            startService(innerIntent);
//            startForeground(CORE_SERVICE_ID, new Notification());
//        }
		Log.e("-------------------","onStart  ...");
		context = this;
		GAdController.getInstance().setContext(context);
        return START_STICKY;
    }
	 
	

	@Override
	public void onCreate() {
		context = this;
		Log.e("-------------------","onCreate  ...");
//		GAdController.getInstance().init(context, false);
		GAdController.getInstance().setContext(context);
		
		GAdController.getInstance().init(context);
		
//		GProBehind.getInstance().show();
				
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
//		stopForeground(true);
		super.onDestroy();
		
	}
	
	public static class CoreInnerService extends Service {

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
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
            super.onDestroy();
        }
    }
	
}
