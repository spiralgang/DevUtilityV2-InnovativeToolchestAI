# Proguard rules for DevUtility SrirachaArmy IDE

# Keep all SrirachaArmy bot classes
-keep class com.spiralgang.srirachaarmy.devutility.core.** { *; }
-keep class com.spiralgang.srirachaarmy.devutility.ai.** { *; }

# Keep Hilt components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep Retrofit interfaces
-keep interface com.spiralgang.srirachaarmy.devutility.ai.DeepSeekEngine { *; }

# Keep Room entities and DAOs
-keep class com.spiralgang.srirachaarmy.devutility.storage.** { *; }

# Keep serialization classes
-keep class com.spiralgang.srirachaarmy.devutility.**.model.** { *; }

# TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }

# SSH and terminal
-keep class com.jcraft.jsch.** { *; }

# Compose
-keep class androidx.compose.** { *; }