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
# TensorFlow Lite
-dontwarn org.tensorflow.lite.gpu.**
-dontwarn org.tensorflow.lite.support.**

# Google Sign-In / Credential Manager / Identity
-keep class com.google.android.gms.auth.api.identity.** { *; }
-keep interface com.google.android.gms.auth.api.identity.** { *; }
-keepnames class com.google.android.gms.auth.api.identity.** { *; }
-keep public class com.google.android.gms.auth.api.identity.** { public *; }
-keepclassmembers class com.google.android.gms.auth.api.identity.** {
    <fields>;
    <methods>;
    public static final android.os.Parcelable$Creator *;
}
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.gms.common.api.** { *; }
-keep class com.google.android.gms.internal.auth-api.** { *; }
-keep class com.google.android.gms.internal.auth-api-phone.** { *; }


# Firebase
-keep class com.google.firebase.** { *; }

# For Parcelables used in Intents/Bundles
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# General Google Play Services
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**
