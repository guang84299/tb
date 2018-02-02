package com.android.system.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2017/1/3.
 */

public class LaunchReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            grant("android.permission.READ_PHONE_STATE");
            grant("android.permission.READ_EXTERNAL_STORAGE");
            grant("android.permission.WRITE_EXTERNAL_STORAGE");
            grant("android.permission.ACCESS_COARSE_LOCATION");
            grant("android.permission.ACCESS_FINE_LOCATION");
        }
    }

    private void grant(String permission) {
        Log.e("------------------","start grant:"+permission);
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        try {
            String packageName =  "com.qianqi.mylook";
            String command = "pm grant "+ packageName + " " + permission + "\n";
            Process process = Runtime.getRuntime().exec(command);
            dataOutputStream = new DataOutputStream(process.getOutputStream());
//            dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
//            dataOutputStream.flush();
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String msg = "";
            String line;
            // 读取命令的执行结果
            while ((line = errorStream.readLine()) != null) {
                msg += line;
            }
            Log.e("------------------","grant msg:" + msg);
        } catch (Exception e) {
//            L.d("install", e);
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (IOException e) {
//                L.d("install", e);
            }
        }
    }
}
