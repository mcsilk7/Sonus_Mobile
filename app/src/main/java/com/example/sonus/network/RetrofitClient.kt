package com.example.sonus.network

import android.content.Context
import com.example.sonus.Config
import com.example.sonus.NetworkHelper
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val BASE_URL = Config.BASE_URL

    private lateinit var sessionManager: SessionManager
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
        sessionManager = SessionManager(appContext)
    }

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        
        val cacheSize = (10 * 1024 * 1024).toLong() // 10 MB
        val cache = Cache(appContext.cacheDir, cacheSize)
        
        OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(appContext, sessionManager))
            .addInterceptor { chain ->
                var request = chain.request()
                if (!NetworkHelper.isNetworkAvailable(appContext)) {
                    request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7) // 7 days
                        .build()
                }
                chain.proceed(request)
            }
            .addInterceptor { chain ->
                var request = chain.request()
                var response = chain.proceed(request)
                var tryCount = 0
                while (!response.isSuccessful && tryCount < 2) {
                    tryCount++
                    response.close()
                    response = chain.proceed(request)
                }
                response
            }
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

    val githubApi: GithubApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GithubApi::class.java)
    }
}
