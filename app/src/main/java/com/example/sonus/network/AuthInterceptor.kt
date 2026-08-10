package com.example.sonus.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        
        sessionManager.getToken()?.let {
            Log.d("SonusAuth", "Adding Token to ${request.url}")
            requestBuilder.addHeader("Authorization", "Bearer $it")
        } ?: Log.w("SonusAuth", "No token found in SessionManager for ${request.url}")
        
        return chain.proceed(requestBuilder.build())
    }
}
