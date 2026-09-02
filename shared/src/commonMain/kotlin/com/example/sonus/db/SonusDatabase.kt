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
    RemoteKey::class
], version = 4)
abstract class SonusDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}
