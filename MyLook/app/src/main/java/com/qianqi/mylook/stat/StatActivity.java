package com.qianqi.mylook.stat;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.qianqi.mylook.utils.L;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Administrator on 2017/3/29.
 */

public class StatActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        L.d("stat onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        finish();
        L.d("stat onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        L.d("stat onPause");
    }
}
