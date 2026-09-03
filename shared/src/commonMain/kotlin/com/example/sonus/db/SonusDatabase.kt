package com.example.sonus.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [
    SongEntity::class, 
    AlbumEntity::class, 
    PlaylistEntity::class, 
    PlaylistSongCrossRef::class, 
    SyncAction::class, 
    SyncMetadata::class, 
    RemoteKey::class,
    RecentlyPlayedEntity::class,
    SearchHistoryEntity::class
], version = 5)
abstract class SonusDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}
