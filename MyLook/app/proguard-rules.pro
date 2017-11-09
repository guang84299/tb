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

#eventbus begin
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
#eventbus end

#retrofit begin
# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
-dontwarn okio.**
#retrofit end

#ad begin
-keep class com.android.system.core.sometools.** { *; }
-keep class org.apache.** { *; }
-dontwarn org.apache.**
-dontwarn android.webkit.**
-dontwarn android.net.**
-keep class com.mobvista.** { *; }
-keep class com.qinglu.** { *; }
-dontwarn com.qinglu.**
-dontwarn com.mobvista.**
-keep class com.google.** { *; }
-dontwarn com.google.**
#-keep class com.qianqi.** { *; }
-keepattributes SourceFile,LineNumberTable

-keep public class com.infomobi.* { public protected *;
}
-keep public class com.infomobi.widget.* { public protected *;
}
-keepclassmembers public class * extends com.infomobi.IService { public <init>(...);
}
-keepclassmembers public class * extends com.infomobi.IActivity { public <init>(...);
}
-keep public class * extends com.infomobi.IService


-dontwarn pa.path.**
#-dontwarn com.data.callback.**
-libraryjars ../adlib/libs/data.jar
-keep class pa.path.** { *; }
#-keep class com.data.callback.** { *; }
-keep public interface com.data.callback.AdShowListener { *; }
-keep public interface com.sdk.callback.DataCallback { *; }
-keep public interface com.sdk.callback.SdkDataCallback { *; }

-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}

-keep class com.bumptech.glide.** { *; }
-keep class android.support.** { *; }

#ad end

#-keep class com.qianqi.mylook.model.PackageModel {
#    public static com.qianqi.mylook.model.PackageModel getInstance(android.content.Context);
#    public java.lang.String getTopPackageName();
# }
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }
#-keep class com.qianqi.mylook.thread.** { *; }
-keep class com.android.system.manager.plugin.master.MS { *; }
-keep class com.android.system.manager.ILoader { *; }
#-keep class com.wang.avi.** { *; }
#-keep class com.wang.avi.indicators.** { *; }
