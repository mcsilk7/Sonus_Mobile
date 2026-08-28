package com.example.sonus.db

import androidx.room.*

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val duration: Int?,
    val coverPath: String?,
    val blurHash: String?,
    val filePath: String?,
    val albumId: Long?,
    val isFavorite: Boolean,
    val isInPlaylist: Boolean
)

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val coverPath: String?,
    val blurHash: String?,
    val isSaved: Boolean
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String?,
    val songCount: Int?
)

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            PlaylistSongCrossRef::class,
            parentColumn = "playlistId",
            entityColumn = "songId"
        )
    )
    val songs: List<SongEntity>
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long
)

@Entity(tableName = "sync_queue")
data class SyncAction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionType: String,
    val songId: Long,
    val albumId: Long? = null,
    val playlistId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_metadata")
data class SyncMetadata(
    @PrimaryKey val resourceKey: String, // e.g., "favorites", "playlists", "albums"
    val lastSyncTimestamp: String?, // ISO 8601 for If-Modified-Since
    val lastSyncMillis: Long
)

@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey val label: String, // e.g., "song_id"
    val nextKey: Int?,
    val prevKey: Int?
)
