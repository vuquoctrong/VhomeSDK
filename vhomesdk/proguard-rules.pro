# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#OkHttp3
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class okio.** { *; }
-dontwarn okio.**

-keep class com.viettel.vht.core.utils.**{*;}
-dontwarn com.vht.sdkcore.core.utils.**

-keep class com.viettel.vht.sdk.model.** {*;}
-dontwarn com.viettel.vht.sdk.model.**

-keep class com.viettel.vht.sdk.ui.jftechaddcamera.model.** {*;}
-dontwarn com.viettel.vht.sdk.ui.jftechaddcamera.model.**

-keep class com.viettel.vht.sdk.funtionsdk.** {*;}
-dontwarn com.viettel.vht.sdk.funtionsdk.**

-dontwarn com.sun.jna.**
-keep class com.sun.jna.**{*;}

-keep class com.viettel.vht.sdk.module.** {*;}
-dontwarn com.viettel.vht.sdk.module.**

-keep class com.vht.sdkcore.module.** {*;}
-dontwarn com.vht.sdkcore.module.**

-keep class com.viettel.vht.sdk.network.** {*;}
-dontwarn com.viettel.vht.sdk.network.**

-keep class javax.inject.** { *; }
-keep class dagger.** { *; }
-keep interface dagger.** { *; }
-keep class dagger.hilt.android.internal.generated.root.** { *; }
-keep class dagger.hilt.android.internal.generated.components.** { *; }
-keep class dagger.hilt.android.internal.generated.** { *; }
-keep class dagger.hilt.android.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.GeneratesRootInput class * { *; }

-keep class com.vht.sdkcore.base.ver2.di.CoroutinesModule
-dontwarn com.vht.sdkcore.base.ver2.di.CoroutinesModule
-keep class com.vht.sdkcore.module.** { *; }
-dontwarn com.vht.sdkcore.module.**
-keep class com.viettel.vht.sdk.di.NavigationModule
-dontwarn com.viettel.vht.sdk.di.NavigationModule
-keep class com.viettel.vht.sdk.notification.** { *; }
-dontwarn com.viettel.vht.sdk.notification.**
-keep class com.viettel.vht.sdk.ui.** { *; }
-dontwarn com.viettel.vht.sdk.ui.**


-keepclassmembers class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <init>(...);
}

-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class com.lib.** {*;}
-keep public class com.basic.** {*;}
-keep public class com.manager.**{public<methods>;public<fields>;}
-keep public  class com.video.opengl.GLSurfaceView20 {*;}
-keep public class com.xm.ui.**{public<methods>;protected<methods>;public<fields>;protected<fields>;}
-keep public class com.utils.**{public<methods>;}
-keep public class com.xm.activity.base.XMBasePresenter{public protected *;}
-keep public class com.xm.activity.base.XMBaseActivity{public<methods>;protected <fields>;}
-keep public class com.xm.activity.base.XMBaseFragment{public<methods>;protected <fields>;}
-keep public class com.xm.kotlin.**{public<methods>;protected<methods>;public<fields>;protected<fields>;}
-keep public class com.xm.ui.**{public<methods>;}
-keep public class com.xm.linke.**{public<methods>;}
-keep public class com.**$*{*;}

-keepattributes Signature
-dontwarn com.google.gson.**
-keep class com.google.gson.**{*;}

#eventbus
-keep class org.greenrobot.eventbus.**{*;}
-keepclassmembers class **{
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode {*;}

#zxing.jar
-dontwarn com.google.zxing.**
-keep class com.google.zxing.**{*;}

#lechangeSDK.jar
-dontwarn com.lechange.**
-keep class com.lechange.**{*;}
-dontwarn com.company.**
-keep class com.company.**{*;}

#pulltorefreshlib.jar
-dontwarn com.lechange.pulltorefreshlistview.**
-keep class com.lechange.pulltorefreshlistview.**{*;}

#DHScanner-1.0.7.aar
-dontwarn com.mm.android.**
-keep class com.mm.android.**{*;}

#imageload4dh.jar
-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.**{*;}


#Android PdfViewer
-keep class com.shockwave.**

-keep class com.viettel.vht.sdk.di.**{*;}
-dontwarn com.viettel.vht.sdk.di.**
-keep class com.vht.sdkcore.**{*;}
-dontwarn com.vht.sdkcore.**

