package com.spiralgang.srirachaarmy.devutility

import android.app.Application
import android.os.Build
import android.webkit.WebView
// import dalvik.system.VMRuntime  // Temporarily disabled
// import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * SrirachaArmy DevUtility Application
 * Central application class for the comprehensive Android IDE
 */
// @HiltAndroidApp  // Temporarily disabled
class SrirachaArmyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("SrirachaArmy DevUtility v${BuildConfig.SRIRACHA_ARMY_VERSION} starting...")
        
        // Memory optimization for Samsung Galaxy S9+
        if (Build.MODEL.contains("SM-G96")) {
            // VMRuntime.getRuntime().setTargetHeapUtilization(0.75f)  // Temporarily disabled
            Timber.d("Samsung Galaxy S9+ detected - memory optimizations available")
        }
        
        // Enable hardware acceleration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WebView.setDataDirectorySuffix("deepseek_ide")
            Timber.d("WebView data directory configured for DeepSeek IDE")
        }
        
        Timber.d("SrirachaArmy Application initialized successfully")
    }
}