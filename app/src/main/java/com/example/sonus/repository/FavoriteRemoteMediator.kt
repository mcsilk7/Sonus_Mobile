package com.example.sonus.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.sonus.db.RemoteKey
import com.example.sonus.db.SongEntity
import com.example.sonus.db.SonusDatabase
import com.example.sonus.network.RetrofitClient
import com.example.sonus.network.SongDTO

@OptIn(ExperimentalPagingApi::class)
class FavoriteRemoteMediator(
    private val database: SonusDatabase,
    private val userId: Long
) : RemoteMediator<Int, SongEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, SongEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKey = database.musicDao().getRemoteKey("favorites")
                if (remoteKey?.nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                remoteKey.nextKey
            }
        }

        try {
            val response = RetrofitClient.favoriteApi.getFavorites(
                userId = userId,
                page = page,
                size = state.config.pageSize
            )

            if (response.isSuccessful) {
                val body = response.body() ?: emptyList()
                val songs = body.mapNotNull { it.song ?: it.songDto } // simplified
                val endOfPaginationReached = songs.isEmpty()

                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        database.musicDao().deleteRemoteKey("favorites")
                        // In real Spotify, we might not clear all if we want to keep some offline,
                        // but Paging 3 REFRESH usually means reset.
                    }
                    
                    val nextKey = if (endOfPaginationReached) null else page + 1
                    database.musicDao().insertRemoteKeys(listOf(RemoteKey("favorites", nextKey, null)))
                    database.musicDao().insertSongs(songs.map { it.toEntity() })
                }
                return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            } else {
                return MediatorResult.Error(Exception("API_ERR: ${response.code()}"))
            }
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private fun SongDTO.toEntity() = SongEntity(id, title, artist, duration, coverPath, blurHash, filePath, albumId, isFavorite = true, isInPlaylist = isInPlaylist)
}
