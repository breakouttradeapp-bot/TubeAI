package com.tubeboost.ai.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utility class for network connectivity checks
 */
object NetworkUtils {

    /**
     * Checks if the device has an active internet connection
     * Compatible with API 24+
     */
    fun isInternetAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as? ConnectivityManager ?: return false

            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                else -> false
            }
        } catch (e: Exception) {
            // If we can't determine, assume available to let the API call fail gracefully
            false
        }
    }

    /**
     * Returns a human-readable connection type for debugging
     */
    fun getConnectionType(context: Context): String {
        return try {
            val connectivityManager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as? ConnectivityManager ?: return "Unknown"

            val network = connectivityManager.activeNetwork ?: return "No Connection"
            val capabilities = connectivityManager.getNetworkCapabilities(network)
                ?: return "No Connection"

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
