package com.example.sonus

import com.example.sonus.db.MusicDao
import com.example.sonus.network.KtorSonusClient
import com.example.sonus.network.SonusApiService
import com.example.sonus.repository.MusicRepository

class DependencyContainer(
    val dao: MusicDao,
    val networkMonitor: PlatformNetworkMonitor,
    val dateFormatter: PlatformDateFormatter,
    val getToken: () -> String?
) {
    val apiService: SonusApiService by lazy {
        KtorSonusClient(baseUrl = Config.BASE_URL, getToken = getToken)
    }

    val repository: MusicRepository by lazy {
        MusicRepository(dao, apiService, networkMonitor, dateFormatter)
    }
}
