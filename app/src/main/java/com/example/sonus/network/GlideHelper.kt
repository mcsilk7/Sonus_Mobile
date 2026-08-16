package com.example.sonus.network

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders

object GlideHelper {
    fun getAuthenticatedUrl(context: Context, url: String?): Any? {
        if (url == null) return null
        
        // Only add authentication header if the URL is for our backend
        val isBackendUrl = url.startsWith(RetrofitClient.BASE_URL)
        
        val sessionManager = SessionManager(context)
        val token = sessionManager.getToken()
        
        return if (isBackendUrl && token != null) {
            GlideUrl(
                url,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            )
        } else {
            url
        }
    }
}
