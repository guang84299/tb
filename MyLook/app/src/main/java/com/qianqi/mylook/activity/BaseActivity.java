package com.qianqi.mylook.activity;

import android.support.v7.app.AppCompatActivity;

import com.qianqi.mylook.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Administrator on 2017/2/16.
 */

public class BaseActivity extends AppCompatActivity {

    private static int START_COUNT = 0;

    @Override
    protected void onStart() {
        super.onStart();
        START_COUNT++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        START_COUNT--;
        if(START_COUNT <= 0){
            CommonUtils.exit();
        }
    }
}
