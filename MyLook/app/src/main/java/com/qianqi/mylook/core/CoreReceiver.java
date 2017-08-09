package com.qianqi.mylook.core;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;

import com.qianqi.mylook.BusTag;
import com.qianqi.mylook.MainApplication;
import com.qianqi.mylook.activity.GPermissionActivity;
import com.qianqi.mylook.activity.MainActivity;
import com.qianqi.mylook.model.PackageModel;
import com.qianqi.mylook.stat.StatActivity;
import com.qianqi.mylook.utils.L;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/12/26.
 */

public class CoreReceiver extends BroadcastReceiver {
    public static int defMode = PackageModel.DEFAULT_POWER_MODE;
    public static boolean isCts = false;
    public static boolean isShowAlert = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        L.d("CoreReceiver:onReceive");
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED)
        {
            showAlert();
            return;
        }
        checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG);
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            showAlert();
            return;
        }

        checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            showAlert();
            return;
        }

        checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            showAlert();
            return;
        }

        checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            showAlert();
            return;
        }

        checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
            showAlert();
            return;
        }

        if(intent.getAction().equals("android.hardware.usb.action.USB_STATE"))
        {
            boolean connected = intent.getExtras().getBoolean("connected");
            if(connected)
            {
                boolean enableAdb = (Settings.Secure.getInt(MainApplication.getInstance().getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
                if(enableAdb && !isCts)
                {
                    isCts = true;
                    defMode = PackageModel.getInstance(MainApplication.getInstance()).getPowerMode();
                    if(defMode != PackageModel.POWER_MODE_PERFORMANCE)
                        PackageModel.getInstance(MainApplication.getInstance()).setPowerMode(PackageModel.POWER_MODE_PERFORMANCE);
//                    Toast.makeText(MainApplication.getInstance(),"cts 模式打开 关闭功能",Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                isCts = false;
                if(PackageModel.getInstance(MainApplication.getInstance()).getPowerMode() != defMode)
                    PackageModel.getInstance(MainApplication.getInstance()).setPowerMode(defMode);
//                Toast.makeText(MainApplication.getInstance(),"cts 模式关闭 恢复功能",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            MainApplication.getInstance().startCoreService();
        }

        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            EventBus.getDefault().post(new BusTag(BusTag.TAG_NETWORK_CHANGED));
        }
    }

    private void showAlert()
    {
        if(isShowAlert)
            return;
        isShowAlert = true;
        int num = MainApplication.getInstance().getSharedPreferences("permissnum", Activity.MODE_PRIVATE).getInt("num",0);
        if(num<20)
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainApplication.getInstance());
            dialogBuilder.setTitle("Error!");
            dialogBuilder.setMessage("TurboBattery has no permissions and can not run.");
            dialogBuilder.setCancelable(false);
            dialogBuilder.setPositiveButton("I know", new DialogInterface.OnClickListener() {//添加确定按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                    isShowAlert = false;
                }
            });
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        }
        else
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainApplication.getInstance());
            dialogBuilder.setTitle("Error!");
            dialogBuilder.setMessage("TurboBattery has no permissions and can not run.");
            dialogBuilder.setCancelable(false);
            dialogBuilder.setPositiveButton("Set", new DialogInterface.OnClickListener() {//添加确定按钮
                @Override
                public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                    isShowAlert = false;
                    openPermissionActivity();
//                    Intent in = new Intent();
//                    in.setAction("com.qianqi.openpermission");
//                    MainApplication.getInstance().sendBroadcast(in);
                }
            });
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        }
        MainApplication.getInstance().getSharedPreferences("permissnum", Activity.MODE_PRIVATE).edit().putInt("num",num+1).commit();
    }

    private void openPermissionActivity()
    {
        Intent intent = new Intent();
        intent.setClass(MainApplication.getInstance(),GPermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MainApplication.getInstance().startActivity(intent);
    }
}
