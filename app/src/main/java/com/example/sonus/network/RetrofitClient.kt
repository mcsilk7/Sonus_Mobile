package com.example.sonus.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val BASE_URL = "http://192.168.1.77:8080/"

    fun getFullUrl(path: String?): String? {
        if (path == null) return null
        if (path.startsWith("http")) return path
        return BASE_URL.removeSuffix("/") + "/" + path.removePrefix("/")
    }

    private lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        sessionManager = SessionManager(context)
    }

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(sessionManager))
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val playlistApi: PlaylistApi by lazy { retrofit.create(PlaylistApi::class.java) }
    val searchApi: SearchApi by lazy { retrofit.create(SearchApi::class.java) }
    val favoriteApi: FavoriteApi by lazy { retrofit.create(FavoriteApi::class.java) }
    val albumApi: AlbumApi by lazy { retrofit.create(AlbumApi::class.java) }
}
