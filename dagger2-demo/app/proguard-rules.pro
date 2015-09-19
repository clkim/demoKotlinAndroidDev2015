# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/android-sdk-macosx/tools/proguard/proguard-android.txt
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

# NOTE: Nothing to configure for Dagger!

# Support
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

# Retrofit (with OkHttp)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keep class sun.misc.Unsafe { *; }
-keep class net.gouline.dagger2demo.model.** { *; }

-dontwarn okio.**

# NoSuchFieldException: producerIndex
#  https://github.com/ReactiveX/RxJava/issues/3097
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

# Kotlin
-dontwarn kotlin.**
# add keep class to try to avoid :app:packageDebug FAILED
#  Execution failed for task':app:packageDebug'. > Unable to compute hash of /Users/clkim/AndroidstudioProjects/android-samples/dagger2-demo/app/build/intermediates/classes-proguard/debug/classes.jar
#  seen with Run | Run 'app'
#  but can't be sure really needed because with M13 Kotlin, a Proguard bug surfaced https://youtrack.jetbrains.com/issue/KT-9184
#  and we intermittently see :app:proguardDebug  Exception while processing task  java.lang.StringIndexOutOfBoundsException: String index out of range: 37
-keep class kotlin.** { *; }

# Butter Knife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewInjector { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}
