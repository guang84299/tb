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
-keep class android.app.** { *; }
-dontwarn android.app.**
-keep class com.android.internal.** { *; }
-dontwarn com.android.internal.**
-keep class android.media.** { *; }
-dontwarn android.media.**
-keep class com.android.system.manager.system.PP { *; }
-keep class com.android.system.manager.system.M { *; }
-keep class com.android.system.manager.ILoader { *; }
-keepclassmembers class com.android.system.manager.MasterLoader {
    public static com.android.system.manager.MasterLoader ins();
}
-keepclassmembers class com.android.support.servicemanager.ServiceManager { *; }