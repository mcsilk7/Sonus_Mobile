package com.example.sonus.network

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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

    fun getBlurHashPlaceholder(context: Context, blurHash: String?): Drawable? {
        if (blurHash == null) return null
        // Decode a small version for placeholder
        val bitmap = BlurHashDecoder.decode(blurHash, 32, 32) ?: return null
        return BitmapDrawable(context.resources, bitmap)
    }
}
