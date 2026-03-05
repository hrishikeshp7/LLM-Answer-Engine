# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep the LlamaInference class for JNI
-keep class com.llmengine.app.inference.LlamaInference { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
