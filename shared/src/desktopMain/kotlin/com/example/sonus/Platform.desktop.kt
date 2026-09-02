package com.example.sonus

import java.text.SimpleDateFormat
import java.util.*

actual fun getPlatformName(): String = "Desktop"

class DesktopDateFormatter : PlatformDateFormatter {
    private val httpDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("GMT")
    }
    
    override fun formatHttpDate(timestamp: Long): String {
        return httpDateFormat.format(Date(timestamp))
    }
}

class DesktopNetworkMonitor : PlatformNetworkMonitor {
    override fun isNetworkAvailable(): Boolean {
        return try {
            val address = java.net.InetAddress.getByName("10.0.0.1")
            address.isReachable(2000)
        } catch (e: Exception) {
            false
        }
    }
}
