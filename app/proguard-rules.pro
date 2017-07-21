# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
#-dontoptimize
#-dontpreverify

# If you want to enable optimization, you should include the
# following:

#
# Note that you cannot just include these flags in your own
# configuration file; if you are including this file, optimization
# will be turned off. You'll need to either edit this file, or
# duplicate the contents of this file and remove the include of this
# file from your project's proguard.config path property.

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.support.v4.app.DialogFragment



-keep class android.support.v7.** { *; }
# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn com.google.ads.**
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keep public class com.google.ads.** { public protected *; }

-keepattributes Signature
-keepattributes *Annotation*
-ignorewarnings
-keep class * {
    public private *;
}
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**


-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
