# On Tha Set - R8 / ProGuard rules for release builds.

# The National Run Calendar map (assets/us_map.html) calls Kotlin through a WebView
# JavaScript bridge. R8 must not rename or remove these methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# Keep line numbers so Play Console crash reports are readable (mapping.txt de-obfuscates names).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# OkHttp, Coil, Play Billing, Play Services and Compose ship their own consumer rules.
# OkHttp optional platform classes:
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
