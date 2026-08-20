package com.example.sonus.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

//    const val BASE_URL = "http://192.168.1.77:8080/"//localhost
//    const val BASE_URL = "http://192.168.1.59:8080/"//localnetwork
      const val BASE_URL = "http://100.126.233.66:8080/"//tailcsale

    private lateinit var sessionManager: SessionManager
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        sessionManager = SessionManager(appContext)
    }

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS // Lowered from BODY to avoid timeouts on slow VPN
        }
        
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(appContext, sessionManager))
            .connectTimeout(30, TimeUnit.SECONDS) // Reduced to 30s
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
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
    val songApi: SongApi by lazy { retrofit.create(SongApi::class.java) }
}
