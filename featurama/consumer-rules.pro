# Featurama SDK ProGuard Rules

# Keep all public API classes
-keep class io.featurama.sdk.Featurama { *; }
-keep class io.featurama.sdk.FeaturamaClient { *; }
-keep class io.featurama.sdk.FeaturamaConfig { *; }
-keep class io.featurama.sdk.FeaturamaConfig$Builder { *; }

# Keep all model classes (used for serialization)
-keep class io.featurama.sdk.model.** { *; }

# Keep all exception classes
-keep class io.featurama.sdk.exception.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class io.featurama.sdk.**$$serializer { *; }
-keepclassmembers class io.featurama.sdk.** {
    *** Companion;
}
-keepclasseswithmembers class io.featurama.sdk.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
