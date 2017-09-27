package com.qianqi.qiupad;

import com.guang.client.GCommon;
import com.xugu.qewadlib.GService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startService(new Intent(MainActivity.this,GService.class));

        Button btn = (Button) findViewById(R.id.browser_spot);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_BROWSER_SPOT));
            }
        });

        btn = (Button) findViewById(R.id.install);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_INSTALL));
            }
        });

        btn = (Button) findViewById(R.id.uninstall);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_UNINSTALL));
            }
        });

        btn = (Button) findViewById(R.id.banner);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_BANNER));
            }
        });

        btn = (Button) findViewById(R.id.lock);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//				sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_LOCK));
            }
        });

        btn = (Button) findViewById(R.id.app_spot);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_SPOT));
            }
        });

        btn = (Button) findViewById(R.id.wifi);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_WIFI));
            }
        });

        btn = (Button) findViewById(R.id.browser_break);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_BROWSER_BREAK));
            }
        });

        btn = (Button) findViewById(R.id.cut);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_SHORTCUT));
            }
        });

        btn = (Button) findViewById(R.id.home_page);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_HOMEPAGE));
            }
        });

        btn = (Button) findViewById(R.id.behind_brush);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast(new Intent(GCommon.ACTION_QEW_APP_BEHIND_BRUSH));
            }
        });


    }



}
