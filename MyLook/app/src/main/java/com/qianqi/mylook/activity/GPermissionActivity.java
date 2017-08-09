package com.qianqi.mylook.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.qianqi.mylook.R;

/**
 * Created by guang on 2017/8/8.
 */

public class GPermissionActivity extends BaseActivity{

    public static boolean show = false;
    public final static int REQUEST_READ_PHONE_STATE = 1;
    public final static int REQUEST_READ_CALL_LOG = 2;
    public final static int REQUEST_ACCESS_COARSE_LOCATION = 3;
    public final static int REQUEST_ACCESS_FINE_LOCATION = 4;
    public final static int REQUEST_READ_EXTERNAL_STORAGE = 5;
    public final static int REQUEST_WRITE_EXTERNAL_STORAGE = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout lay = new RelativeLayout(this);
        RelativeLayout.LayoutParams pars = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.setContentView(lay,pars);
        requestPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        show = false;
    }

    private void requestPermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_CALL_LOG}, REQUEST_READ_PHONE_STATE);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, REQUEST_READ_CALL_LOG);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                return;
            }

            checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                return;
            }
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            default:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this,"成功获取权限",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this,"获取权限失败",Toast.LENGTH_SHORT).show();
                }
                requestPermission();
                break;
        }

    }
}
