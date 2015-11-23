#
# This ProGuard configuration file illustrates how to process a program
# library, such that it remains usable as a library.
# Usage:
#     java -jar proguard.jar @library.pro
#

# Specify the input jars, output jars, and library jars.
# In this case, the input jar is the program library that we want to process.

-include 'mobileCore_ext.pro'

-injars  ./bin/mobilecore.jar
-outjars ./mobilecore.jar

# Keep parameter names
-keepparameternames

# Save the obfuscation mapping to a file, so we can de-obfuscate any stack
# traces later on. Keep a fixed source file attribute and all line number
# tables to get line numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.

-printmapping out.map

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

#-keepnames class * implements android.os.Parcelable {
#    public static final ** CREATOR;
#}

-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod

# Preserve all annotations.
-dontwarn android.view.Window
-dontwarn android.view.WindowManager$LayoutParams

-keepattributes *Annotation*

# include flurry jar
#-libraryjars FlurryAgent.jar

# Preserve all public classes, and their public and protected fields and
# methods.

-keep class com.ironsource.mobilcore.NativeAdsAdapter {
	public *;
}


-keep class com.ironsource.mobilcore.CallbackResponse** {
    *** onConfirmation(...);
}

-keep class com.ironsource.mobilcore.UserProperties** {
  public *;
}

-keep class com.ironsource.mobilcore.OnReadyListener {
  public *;
}

-keep class com.ironsource.mobilcore.AdUnitEventListener$EVENT_TYPE {
  public *;
}

-keep class com.ironsource.mobilcore.AdUnitEventListener {
  public *;
}

-keepclassmembers class com.ironsource.mobilcore.MobileCore {
	public *;
    private static void setMediationParams(java.lang.String);
}

# Preserve AD_UNIT_TRIGGER with all public methods and objects
-keep class com.ironsource.mobilcore.MobileCore$AD_UNIT_TRIGGER{
    public *;
}

-keepnames class com.ironsource.mobilcore.MobileCore {
	*** isInterstitialReady(...);
	*** setMediationParams(...);
}

-keepnames class * extends android.app.IntentService
{

}

-keepnames class * extends android.app.Service
{

}

-keepnames class * extends android.app.Activity
{

}


-keepclassmembers class * implements com.ironsource.mobilcore.MCWebChromeClientWithJSAccess$IJSBridge {

	public *;
}

#-keepclassmembers class com.ironsource.mobilcore.Utils {
#    public *;
#}

# Preserve all enums
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Preserve specific enum names
-keep public enum com.ironsource.mobilcore.MobileCore$** {
	**[] $VALUES;
	public *;
}
-keep public enum com.ironsource.mobilcore.CallbackResponse$** {
        **[] $VALUES;
        public *;
}


# Stickeez JS Bridge 
#-keepclassmembers class com.ironsource.mobilcore.StickeezManager$JSReportingBridge {
#    public *;
#}

-keep class * extends android.content.BroadcastReceiver {

}

# Preserve all .class method names.

#-keepclassmembernames class * {
#    java.lang.Class class$(java.lang.String);
#    java.lang.Class class$(java.lang.String, boolean);
#}

# Preserve all native method names and the names of their classes.

#-keepclasseswithmembernames class * {
 #   native <methods>;
#}

# Preserve the special static methods that are required in all enumeration
# classes.

-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your library doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Your library may contain more items that need to be preserved; 
# typically classes that are dynamically created using Class.forName:

# -keep public class mypackage.MyClass
# -keep public interface mypackage.MyInterface
# -keep public class * implements mypackage.MyInterface


# Fix: Conversion to Dalvik format failed with error 1 (Based on this: http://viktorbresan.blogspot.co.il/2012/10/conversion-to-dalvik-format-failed-with.html)
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
