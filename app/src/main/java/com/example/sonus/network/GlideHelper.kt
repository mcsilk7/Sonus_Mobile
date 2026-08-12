package com.example.sonus.network

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders

object GlideHelper {
    fun getAuthenticatedUrl(context: Context, url: String?): Any? {
        if (url == null) return null
        
        val sessionManager = SessionManager(context)
        val token = sessionManager.getToken()
        
        return if (token != null) {
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
