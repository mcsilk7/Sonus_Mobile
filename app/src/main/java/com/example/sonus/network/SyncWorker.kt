package com.example.sonus.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sonus.db.SonusDatabase

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = SonusDatabase.getDatabase(applicationContext)
        val dao = database.musicDao()
        
        val pendingActions = dao.getPendingSyncActions()
        if (pendingActions.isEmpty()) return Result.success()
        
        val sessionManager = SessionManager(applicationContext)
        val userId = sessionManager.getUserId()
        if (userId == -1L) return Result.failure()

        var allSuccessful = true
        for (action in pendingActions) {
            try {
                val success = when (action.actionType) {
                    "TOGGLE_FAVORITE" -> RetrofitClient.favoriteApi.toggleFavorite(userId, action.songId).isSuccessful
                    "ADD_ALBUM" -> action.albumId?.let { RetrofitClient.albumApi.addAlbumToLibrary(it, userId).isSuccessful } ?: true
                    "REMOVE_ALBUM" -> action.albumId?.let { RetrofitClient.albumApi.removeAlbumFromLibrary(it, userId).isSuccessful } ?: true
                    else -> true
                }
                
                if (success) {
                    dao.deleteSyncAction(action)
                } else {
                    allSuccessful = false
                }
            } catch (e: Exception) {
                allSuccessful = false
            }
        }
        
        return if (allSuccessful) Result.success() else Result.retry()
    }
}
