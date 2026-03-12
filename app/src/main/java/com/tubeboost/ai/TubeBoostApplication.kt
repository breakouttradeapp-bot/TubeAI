package com.tubeboost.ai

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tubeboost.ai.utils.AdManager

class TubeBoostApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        } catch (e: Exception) {
            // Firebase not configured yet - won't crash the app
        }

        // Initialize AdMob
        initializeAdMob()
    }

    private fun initializeAdMob() {
        try {
            // Set test device IDs for testing
            val testDeviceIds = listOf(
                "EMULATOR_DEVICE_ID",
                "YOUR_TEST_DEVICE_ID_HERE"
            )

            val requestConfiguration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()

            MobileAds.setRequestConfiguration(requestConfiguration)

            // Initialize MobileAds SDK
            MobileAds.initialize(this) { initializationStatus ->
                // Log initialization status per adapter
                val statusMap = initializationStatus.adapterStatusMap
                for ((adapter, status) in statusMap) {
                    android.util.Log.d(
                        "TubeBoostAI",
                        "Adapter: $adapter Status: ${status.initializationState}"
                    )
                }

                // Pre-load ads after initialization
                AdManager.preloadAds(this@TubeBoostApplication)
            }
        } catch (e: Exception) {
            // AdMob initialization failure should not crash the app
            android.util.Log.e("TubeBoostAI", "AdMob init error: ${e.message}")
        }
    }
}
