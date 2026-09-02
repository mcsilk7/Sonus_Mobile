package com.example.sonus

import android.content.Context
import com.example.sonus.NetworkHelper

class AndroidNetworkMonitor(private val context: Context) : PlatformNetworkMonitor {
    override fun isNetworkAvailable(): Boolean {
        return NetworkHelper.isNetworkAvailable(context)
    }
}
