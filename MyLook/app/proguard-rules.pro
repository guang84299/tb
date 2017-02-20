# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Administrator\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#umeng begin
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keep public class com.qianqi.mylook.R$*{
    public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#umeng end

-dontwarn rx.internal.util.**
-keep class rx.** { *; }
-keep class com.hwangjr.rxbus.** { *; }
-keep class xiaofei.library.** { *; }
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }
-keep class com.qianqi.mylook.thread.** { *; }
-keep class com.qianqi.mylook.client.IMasterServer { *; }
-keep class com.qianqi.mylook.client.ProcessHelper { *; }
-keep class com.qianqi.mylook.client.callback.MonitorCallback { *; }
#-keep class com.wang.avi.** { *; }
#-keep class com.wang.avi.indicators.** { *; }