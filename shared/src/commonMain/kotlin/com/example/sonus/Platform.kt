package com.example.sonus

interface PlatformNetworkMonitor {
    fun isNetworkAvailable(): Boolean
}

interface PlatformDateFormatter {
    fun formatHttpDate(timestamp: Long): String
    fun now(): Long
}

expect fun getPlatformName(): String
