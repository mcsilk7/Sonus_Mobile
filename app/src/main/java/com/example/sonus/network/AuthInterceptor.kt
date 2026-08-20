package com.example.sonus.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sonus.MainActivity
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context,
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        
        sessionManager.getToken()?.let {
            Log.d("SonusAuth", "Adding Token to ${request.url}")
            requestBuilder.addHeader("Authorization", "Bearer $it")
        } ?: Log.w("SonusAuth", "No token found in SessionManager for ${request.url}")
        
        val response = try {
            chain.proceed(requestBuilder.build())
        } catch (e: Exception) {
            val errorMsg = when (e) {
                is java.net.SocketTimeoutException -> "NET_TIMEOUT"
                is java.net.ConnectException -> "NET_REFUSED"
                is java.net.UnknownHostException -> "DNS_FAIL"
                else -> "NET_ERR: ${e.message}"
            }
            NetworkMonitor.logError(errorMsg)
            throw e
        }
        
        if (!response.isSuccessful) {
            NetworkMonitor.logError("HTTP_ERR_${response.code}: ${request.url.encodedPath}")
        }
        
        if (response.code == 401) {
            Log.e("SonusAuth", "401 Unauthorized - Redirecting to Login")
            sessionManager.clearSession()
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
        
        return response
    }
}
