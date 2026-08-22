package com.example.sonus

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkHelper {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        // Log detected transports for debugging
        if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) android.util.Log.d("SonusNet", "Detected: WIFI")
        if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) android.util.Log.d("SonusNet", "Detected: CELLULAR")

        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
               activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
