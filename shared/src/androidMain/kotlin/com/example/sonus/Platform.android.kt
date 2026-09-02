package com.example.sonus

import java.text.SimpleDateFormat
import java.util.*

actual fun getPlatformName(): String = "Android"

class AndroidDateFormatter : PlatformDateFormatter {
    private val httpDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("GMT")
    }
    
    override fun formatHttpDate(timestamp: Long): String {
        return httpDateFormat.format(Date(timestamp))
    }
}
