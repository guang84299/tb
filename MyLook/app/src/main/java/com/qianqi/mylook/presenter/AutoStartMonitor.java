//package com.qianqi.mylook.presenter;
//
//import android.os.Message;
//import android.util.Xml;
//
//import com.qianqi.mylook.BusTag;
//import com.qianqi.mylook.MainApplication;
//import com.qianqi.mylook.bean.EnhancePackageInfo;
//import com.qianqi.mylook.model.PackageModel;
//import com.qianqi.mylook.thread.ThreadTask;
//import com.qianqi.mylook.client.MasterClient;
//import com.qianqi.mylook.utils.L;
//
//import org.xmlpull.v1.XmlSerializer;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.StringWriter;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Administrator on 2017/1/3.
// */
//
//public class AutoStartMonitor extends ThreadTask {
//
//    private static File RULES_DIR = new File("/data/system/ifw/");
//
//    public AutoStartMonitor(){
//        super(AutoStartMonitor.class.getSimpleName());
//        RxBus.get().register(this);
//        initRulesDir();
//    }
//
//    private void initRulesDir(){
//        Class<?> threadClazz = null;
//        try {
//            threadClazz = Class.forName("android.os.Environment");
//            Method method = threadClazz.getMethod("getSystemSecureDirectory");
//            File f = (File) method.invoke(null);
//            RULES_DIR = new File(f, "ifw");
//            return;
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//        if(!RULES_DIR.exists()){
//            RULES_DIR = new File("/data/secure/system/ifw/");
//        }
//    }
//
//    private void checkFW(){
//        if(handler == null){
//            return;
//        }
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                L.i("start check fw");
////                List<ActivityManager.RunningAppProcessInfo> processList = MasterClient.getInstance().getProcessList();
////                List<String> runningPackages = null;
////                if(processList != null){
////                    runningPackages = new ArrayList<String>(processList.size());
////                    for(ActivityManager.RunningAppProcessInfo process:processList){
////                        String[] packageList = process.pkgList;
////                        for(String packageName:packageList){
////                            togglePackageFW(packageName,false);
////                            runningPackages.add(packageName);
////                        }
////                    }
////                }
//                boolean hasInitPackageList = PackageModel.getInstance(MainApplication.getInstance()).hasInit();
//                if(hasInitPackageList){
//                    List<EnhancePackageInfo> packageList =
//                            PackageModel.getInstance(MainApplication.getInstance()).getPackageList(null);
//                    clearFWDir(packageList);
//                    for(EnhancePackageInfo p:packageList){
//                        String packageName = p.packageName;
//                        if(!p.isRunning){
//                            if(p.allowAutoStart){
//                                togglePackageFW(packageName,false);
//                            }
//                            else{
//                                togglePackageFW(packageName,true);
//                            }
//                        }
//                        else{
//                            togglePackageFW(packageName,false);
//                        }
//                    }
//                }
//                L.i("finish check fw");
//            }
//        });
//    }
//
//    private void clearFWDir(List<EnhancePackageInfo> packageList){
//        ArrayList<String> packageNames = new ArrayList<>(packageList.size());
//        for(EnhancePackageInfo p:packageList){
//            packageNames.add(p.packageName+".xml");
//        }
//        MasterClient.getInstance().clearDir(RULES_DIR.getAbsolutePath(),packageNames);
//    }
//
//    /*不允许自启动的应用不在运行时开启防火墙，其他情况关闭防火墙*/
//    private void togglePackageFW(String packageName,boolean enable){
//        File f = new File(RULES_DIR,packageName+".xml");
//        if(enable){
//            if(!MasterClient.getInstance().isFileExist(f.getAbsolutePath())){
//                L.d("enable fw:"+packageName);
//                writeRule(packageName);
//            }
//        }
//        else{
//            if(MasterClient.getInstance().isFileExist(f.getAbsolutePath())){
//                L.d("disable fw:"+packageName);
//                MasterClient.getInstance().deleteFile(f.getAbsolutePath());
//            }
//        }
//    }
//
//    private void writeRule(String packageName){
//        File f = new File(RULES_DIR,packageName+".xml");
//        StringWriter writer=new StringWriter();
//        try {
//            // we create a XmlSerializer in order to write xml data
//            XmlSerializer serializer = Xml.newSerializer();
//            // we set the FileOutputStream as output for the serializer,
//            // using UTF-8 encoding
//            serializer.setOutput(writer);
//            // <?xml version=”1.0″ encoding=”UTF-8″>
//            // Write <?xml declaration with encoding (if encoding not
//            // null) and standalone flag (if stan dalone not null)
//            // This method can only be called just after setOutput.
//            serializer.startDocument("UTF-8", null);
//            // start a tag called "root"
//            serializer.startTag(null, "rules");
//            serializer.startTag(null, "service");
//            serializer.attribute(null, "block", "true");
//            serializer.attribute(null, "log", "false");
//            serializer.startTag(null, "component-filter");
//            serializer.attribute(null, "name", packageName+"/*");
//            serializer.endTag(null, "component-filter");
//            serializer.startTag(null, "not");
//            serializer.startTag(null, "sender-package");
//            serializer.attribute(null, "name", packageName);
//            serializer.endTag(null, "sender-package");
//            serializer.endTag(null, "not");
//            serializer.endTag(null, "service");
//            serializer.startTag(null, "broadcast");
//            serializer.attribute(null, "block", "true");
//            serializer.attribute(null, "log", "false");
//            serializer.startTag(null, "component-filter");
//            serializer.attribute(null, "name", packageName+"/*");
//            serializer.endTag(null, "component-filter");
//            serializer.startTag(null, "not");
//            serializer.startTag(null, "sender-package");
//            serializer.attribute(null, "name", packageName);
//            serializer.endTag(null, "sender-package");
//            serializer.endTag(null, "not");
//            serializer.endTag(null, "broadcast");
//            serializer.endTag(null, "rules");
//            serializer.endDocument();
//            // write xml data into the FileOutputStream
//            serializer.flush();
//            // finally we close the file stream
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//        String content = writer.toString();
//        MasterClient.getInstance().writeFile(f.getAbsolutePath(),content);
//    }
//
//    @Subscribe(
//            thread = EventThread.IMMEDIATE,
//            tags = {@Tag(BusTag.TAG_PACKAGE_UPDATE)}
//    )
//    public void onPackageUpdate(ArrayList<EnhancePackageInfo> packageList){
////        checkFW();
//    }
//
//    @Override
//    protected void onLooperPrepared() {
//        super.onLooperPrepared();
////        checkFW();
//    }
//
//    protected void handleMessage(Message msg) {
//    }
//
//    public void onDestroy(){
//        RxBus.get().unregister(this);
//        this.cancel();
//    }
//
//    public void monitorLogcat(){
////        new Thread(){
////            @Override
////            public void run() {
////                Process mLogcatProc = null;
////                BufferedReader reader = null;
////                try {
////                    //获取logcat日志信息
////                    mLogcatProc = Runtime.getRuntime().exec(new String[] { "logcat","Mytest:I *:S" });
////                    reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()));
////                    String line;
////                    while ((line = reader.readLine()) != null) {
////                        if (line.indexOf("this is a test") > 0) {
////                            //logcat打印信息在这里可以监听到
////                            // 使用looper 把给界面一个显示
////                            Looper.prepare();
////                            Toast.makeText(this, "监听到log信息", Toast.LENGTH_SHORT).show();
////                            Looper.loop();
////                        }
////                    }
////                } catch (Exception e) {
////                    e.printStackTrace();
////                }
////            }
////        }.start();
//    }
//}
