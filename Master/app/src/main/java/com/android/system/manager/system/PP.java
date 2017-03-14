package com.android.system.manager.system;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.text.TextUtils;

import com.android.server.firewall.IntentFirewall;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/3.
 * PP
 */


public class PP extends IntentFirewall {

    private IntentFirewall o = null;
    private M m = null;
    private ArrayList<String> systemBroadcast = new ArrayList<>();

    public PP(AMSInterface ams, Handler handler) {
        super(ams, handler);
    }

    /*b*/
    public void b(IntentFirewall o){
        this.o = o;
    }

    /*setManager*/
    public void a(M m){
        this.m = m;
    }

    @Override
    public boolean checkStartActivity(Intent intent, int callerUid, int callerPid, String resolvedType, ApplicationInfo resolvedApp) {
        //L.d("checkStartActivity:"+resolvedApp.packageName+","+callerPid);
        return o.checkStartActivity(intent, callerUid, callerPid, resolvedType, resolvedApp);
    }

    @Override
    public boolean checkService(ComponentName resolvedService, Intent intent, int callerUid, int callerPid, String resolvedType, ApplicationInfo resolvedApp) {
        if(resolvedApp != null){
            String n = resolvedApp.packageName;
            //L.d("check service:to "+packageName+",from "+callerPid);
            if(!c(n)){
                if(!i(n)){
//                    L.d("[block] service:"+n);
//                    this.m.logBlock(packageName);
                    return false;
                }
            }
        }
        return o.checkService(resolvedService, intent, callerUid, callerPid, resolvedType, resolvedApp);
    }

    @Override
    public boolean checkBroadcast(Intent intent, int callerUid, int callerPid, String resolvedType, int receivingUid) {
        //L.d("check broadcast:to "+this.master.getPackageForUid(receivingUid)+","+(intent.getAction()==null?"null":intent.getAction())+",from "+callerPid);
//        if(callerUid <= 1000){
//            if(!systemBroadcast.contains(intent.getAction())){
//                systemBroadcast.add(intent.getAction());
//                this.m.logBlock("system broadcast:"+intent.getAction());
//            }
//
//        }
//        boolean excludeStopped = (intent.getFlags()&(FLAG_EXCLUDE_STOPPED_PACKAGES|FLAG_INCLUDE_STOPPED_PACKAGES))
//                == FLAG_EXCLUDE_STOPPED_PACKAGES;
//        if(!excludeStopped){
////            L.d("[good] broadcast:"+intent.getAction());
//            this.m.logBlock("good broadcast:"+intent.getAction());
//        }
        if(!c(receivingUid)){
            if(!u(receivingUid)){
//                L.d("[block] broadcast:"+receivingUid);
//                this.m.logBlock(this.m.getPackageForUid(receivingUid));
                return false;
            }
        }
        return o.checkBroadcast(intent, callerUid, callerPid, resolvedType, receivingUid);
    }

    /*c*/
    private boolean c(String p){
        if(TextUtils.isEmpty(p)){
            return true;
        }
        return this.m.c(p);
    }

    private boolean c(int u){
        return this.m.c(u);
    }

    /*isPackageRunning*/
    private boolean i(String p){
        List<ActivityManager.RunningAppProcessInfo> list = this.m.r();
        for(ActivityManager.RunningAppProcessInfo processInfo : list){
            if(processInfo.pkgList != null){
                for(String pkg:processInfo.pkgList){
                    if(pkg.equals(p)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*isUidRunning*/
    private boolean u(int u){
        List<ActivityManager.RunningAppProcessInfo> list = this.m.r();
        for(ActivityManager.RunningAppProcessInfo processInfo : list){
            if(processInfo.uid == u){
                return true;
            }
        }
        return false;
    }

    //    public void onMakeDex(){
//        try {
//            DexMaker dexMaker = new DexMaker();
//            // Generate a HelloWorld class.
//            TypeId<?> intentFirewall = TypeId.get("LIntentFirewall;");
//            TypeId<?> processFirewall = TypeId.get("LProcessFirewall;");
//            dexMaker.declare(processFirewall, "HelloWorld.generated", Modifier.PUBLIC, intentFirewall);
//            generateHelloMethod(dexMaker, helloWorld);
//            // Create the dex file and load it.
//            File outputDir = new File(Environment.getExternalStorageDirectory() + File.separator + "dexmaker");
//            if (!outputDir.exists())outputDir.mkdir();
//            ClassLoader loader = dexMaker.generateAndLoad(this.getClassLoader(), outputDir);
//            Class<?> helloWorldClass = loader.loadClass("HelloWorld");
//            // Execute our newly-generated code in-process.
//            helloWorldClass.getMethod("hello").invoke(null);
//        } catch (Exception e) {
//            Log.e("MainActivity","[onMakeDex]",e);
//        }
//    }
//
//    private static void generateHelloMethod(DexMaker dexMaker, TypeId<?> declaringType) {
//        // Lookup some types we'll need along the way.
//        TypeId<SystemProcess> systemType = TypeId.get(SystemProcess.class);
//        TypeId<PrintStream> printStreamType = TypeId.get(PrintStream.class);
//
//        // Identify the 'hello()' method on declaringType.
//        MethodId hello = declaringType.getMethod(TypeId.VOID, "hello");
//
//        // Declare that method on the dexMaker. Use the returned Code instance
//        // as a builder that we can append instructions to.
//        Code code = dexMaker.declare(hello, Modifier.STATIC | Modifier.PUBLIC);
//
//        // Declare all the locals we'll need up front. The API requires this.
//        Local<Integer> a = code.newLocal(TypeId.INT);
//        Local<Integer> b = code.newLocal(TypeId.INT);
//        Local<Integer> c = code.newLocal(TypeId.INT);
//        Local<String> s = code.newLocal(TypeId.STRING);
//        Local<PrintStream> localSystemOut = code.newLocal(printStreamType);
//
//        // int a = 0xabcd;
//        code.loadConstant(a, 0xabcd);
//
//        // int b = 0xaaaa;
//        code.loadConstant(b, 0xaaaa);
//
//        // int c = a - b;
//        code.op(BinaryOp.SUBTRACT, c, a, b);
//
//        // String s = Integer.toHexString(c);
//        MethodId<Integer, String> toHexString
//                = TypeId.get(Integer.class).getMethod(TypeId.STRING, "toHexString", TypeId.INT);
//        code.invokeStatic(toHexString, s, c);
//
//        // System.out.println(s);
//        FieldId<System, PrintStream> systemOutField = systemType.getField(printStreamType, "out");
//        code.sget(systemOutField, localSystemOut);
//        MethodId<PrintStream, Void> printlnMethod = printStreamType.getMethod(
//                TypeId.VOID, "println", TypeId.STRING);
//        code.invokeVirtual(printlnMethod, null, localSystemOut, s);
//
//        // return;
//        code.returnVoid();
//    }
}
