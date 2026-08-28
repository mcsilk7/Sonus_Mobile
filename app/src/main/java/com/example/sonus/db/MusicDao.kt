package com.example.sonus.db

import androidx.room.*
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM songs")
    fun getAllSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: Long): SongEntity?

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: Long, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Query("SELECT * FROM albums")
    suspend fun getAllAlbums(): List<AlbumEntity>

    @Query("SELECT * FROM albums")
    fun getAllAlbumsFlow(): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>)

    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylists(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsFlow(): Flow<List<PlaylistWithSongs>>

    @Transaction
    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylistsWithSongs(): List<PlaylistWithSongs>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(crossRef: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)

    @Transaction
    @Query("SELECT songs.* FROM songs INNER JOIN playlist_songs ON songs.id = playlist_songs.songId WHERE playlistId = :playlistId")
    suspend fun getSongsForPlaylist(playlistId: Long): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE isFavorite = 1")
    suspend fun getFavoriteSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE isFavorite = 1")
    fun getFavoriteSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT DISTINCT songId FROM playlist_songs")
    suspend fun getAllSongIdsInPlaylists(): List<Long>

    @Insert
    suspend fun insertSyncAction(action: SyncAction)
    
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getPendingSyncActions(): List<SyncAction>
    
    @Delete
    suspend fun deleteSyncAction(action: SyncAction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncMetadata(metadata: SyncMetadata)

    @Query("SELECT * FROM sync_metadata WHERE resourceKey = :key")
    suspend fun getSyncMetadata(key: String): SyncMetadata?
    
    @Query("DELETE FROM songs WHERE isFavorite = 1 AND id NOT IN (:ids)")
    suspend fun removeObsoleteFavorites(ids: List<Long>)
    
    @Query("DELETE FROM albums WHERE id NOT IN (:ids)")
    suspend fun removeObsoleteAlbums(ids: List<Long>)
    
    @Query("DELETE FROM playlists WHERE id NOT IN (:ids)")
    suspend fun removeObsoletePlaylists(ids: List<Long>)
    
    @Query("SELECT * FROM songs")
    fun getAllSongsPaging(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE isFavorite = 1")
    fun getFavoriteSongsPaging(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavoriteSongsPagingByTitle(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY artist COLLATE NOCASE ASC")
    fun getFavoriteSongsPagingByArtist(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY duration ASC")
    fun getFavoriteSongsPagingByDuration(): PagingSource<Int, SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKeys(remoteKeys: List<RemoteKey>)

    @Query("SELECT * FROM remote_keys WHERE label = :label")
    suspend fun getRemoteKey(label: String): RemoteKey?

    @Query("DELETE FROM remote_keys WHERE label = :label")
    suspend fun deleteRemoteKey(label: String)
    
    @Query("DELETE FROM remote_keys")
    suspend fun clearAllRemoteKeys()
}
