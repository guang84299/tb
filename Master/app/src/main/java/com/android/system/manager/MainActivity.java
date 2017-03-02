package com.android.system.manager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.system.manager.utils.FileUtils;

import java.io.File;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainApplication.startSService();
//        TextView logView = (TextView) findViewById(R.id.log);
//        File dir = this.getFilesDir();
//        File logFile = new File(dir,"block");
//        List<String> logs = FileUtils.readFile(logFile);
//        for(String s:logs){
//            logView.append(s+"\n");
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
