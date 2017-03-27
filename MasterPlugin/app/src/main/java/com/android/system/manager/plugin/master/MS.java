package com.android.system.manager.plugin.master;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/3.
 */

public interface MS {

    /*start*/
    void a(Context context);

    /*b*/
    void b();

    /*forceStopPackage*/
    boolean c(String packageName);

//    boolean monitorAppProcess(final MonitorCallback callback);

    /*getProcesses*/
    List<String> d();

    /*setWriteApps*/
    void e(List<String> writeApps);

    /*getRunningTasks*/
    String f();

    /*enableAccessibilityService*/
    void g(String s);

    /*clearDir*/
    void h(String dirPath,ArrayList<String> keyList);

    /*isFileExist*/
    boolean i(String path);

    /*deleteFile*/
    void j(String path);

    /*writeFile*/
    void k(String path,String content);

    /*getAudioFocus*/
    String l();

    void m(boolean bool);

    long n();

    void o(boolean bool);

    boolean p();
}
