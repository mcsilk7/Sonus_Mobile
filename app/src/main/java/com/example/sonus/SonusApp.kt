package com.example.sonus

import android.app.Application
import com.example.sonus.network.RetrofitClient

class SonusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
