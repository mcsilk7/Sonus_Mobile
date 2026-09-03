package network

import com.example.sonus.PlatformNetworkMonitor
import com.example.sonus.db.MusicDao
import com.example.sonus.network.SonusApiService
import data.DesktopSessionManager
import kotlinx.coroutines.*
import ui.DesktopDI

object DesktopSyncManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    fun startSyncCycle(
        dao: MusicDao,
        apiService: SonusApiService,
        networkMonitor: PlatformNetworkMonitor,
        sessionManager: DesktopSessionManager
    ) {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                if (networkMonitor.isNetworkAvailable()) {
                    processSyncQueue(dao, apiService, sessionManager)
                }
                delay(30_000) // Sprawdzaj co 30 sekund
            }
        }
    }

    private suspend fun processSyncQueue(
        dao: MusicDao,
        apiService: SonusApiService,
        sessionManager: DesktopSessionManager
    ) {
        val pendingActions = dao.getPendingSyncActions()
        if (pendingActions.isEmpty()) return

        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        println("SONUS_SYNC: Processing ${pendingActions.size} pending actions...")

        for (action in pendingActions) {
            try {
                val success = when (action.actionType) {
                    "TOGGLE_FAVORITE" -> {
                        action.songId?.let { apiService.toggleFavorite(userId, it).isNotEmpty() } ?: true
                    }
                    "ADD_ALBUM" -> {
                        action.albumId?.let { apiService.addAlbumToLibrary(it, userId).isNotEmpty() } ?: true
                    }
                    "REMOVE_ALBUM" -> {
                        action.albumId?.let { apiService.removeAlbumFromLibrary(it, userId).isNotEmpty() } ?: true
                    }
                    "ADD_SONG_TO_PLAYLIST" -> {
                        val pId = action.playlistId
                        val sId = action.songId
                        if (pId != null && sId != null) {
                            apiService.addSongToPlaylist(pId, sId).isNotEmpty()
                        } else true
                    }
                    "REMOVE_SONG_FROM_PLAYLIST" -> {
                        val pId = action.playlistId
                        val sId = action.songId
                        if (pId != null && sId != null) {
                            apiService.removeSongFromPlaylist(pId, sId).isNotEmpty()
                        } else true
                    }
                    else -> true
                }

                if (success) {
                    dao.deleteSyncAction(action)
                    println("SONUS_SYNC: Action ${action.actionType} for ${action.songId} synced successfully.")
                }
            } catch (e: Exception) {
                println("SONUS_SYNC: Failed to sync action ${action.actionType}: ${e.message}")
            }
        }
    }
    
    fun triggerImmediateSync() {
        scope.launch {
            val container = DesktopDI.container
            processSyncQueue(
                container.dao,
                container.apiService,
                DesktopDI.sessionManager
            )
        }
    }
}
