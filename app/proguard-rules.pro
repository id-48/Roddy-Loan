# =============================================================================
# Personal EMI Loan Calculator — R8 / ProGuard (release)
# Conservative keeps so minify does not crash ads, Gson, JNI, FCM, Facebook.
# =============================================================================

# ---- Crashlytics readable stacks ----
-keepattributes SourceFile,LineNumberTable,InnerClasses,EnclosingMethod,Signature,Exceptions,*Annotation*,AnnotationDefault
-renamesourcefileattribute SourceFile
-keep public class * extends java.lang.Exception

# ---- Manifest components + Class.forName(onboarding resume) ----
-keep class * extends android.app.Application
-keep class * extends android.app.Activity {
    public <init>();
}
-keepnames class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends androidx.fragment.app.Fragment {
    public <init>();
}
-keep class com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.EmiApp { *; }

# ---- JNI libemi_secure ----
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
-keep class com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.SecureApi { *; }

# ---- Gson (AdsConfig JSON) ----
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsConfig { *; }
-keep class com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsConfig$* { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
}

# ---- ViewBinding ----
-keep class * extends androidx.viewbinding.ViewBinding {
    public static * inflate(...);
    public static * bind(...);
}

# ---- Lottie ----
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ---- AdMob / Play Services Ads ----
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.ads.**

# ---- Firebase ----
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.datatransport.** { *; }
-dontwarn com.google.android.datatransport.**
-keep class com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.push.** { *; }

# ---- Facebook + Audience Network ----
-keep class com.facebook.** { *; }
-dontwarn com.facebook.**

# ---- AndroidX lifecycle (LauncherGate ProcessLifecycleOwner) ----
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**
-keep class androidx.browser.** { *; }

# ---- WorkManager + Room (AGP 9 / R8 full mode) ----
# Startup crash: Unable to get provider InitializationProvider /
# Failed to create an instance of androidx.work.impl.WorkDatabase
# R8 strips no-arg <init> used via reflection; class keep alone is not enough.
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.work.InputMerger {
    <init>();
}
-keep class androidx.work.impl.WorkDatabase_Impl {
    <init>();
}
-keep class androidx.work.** {
    <init>(...);
}

# ---- Parcelable / Serializable ----
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}

# ---- Optional deps noise ----
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn sun.misc.**
-dontwarn okhttp3.**
-dontwarn okio.**
