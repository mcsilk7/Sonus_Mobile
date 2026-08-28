package com.example.sonus.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SongEntity::class, AlbumEntity::class, PlaylistEntity::class, PlaylistSongCrossRef::class, SyncAction::class, SyncMetadata::class, RemoteKey::class], version = 4)
abstract class SonusDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: SonusDatabase? = null

        fun getDatabase(context: Context): SonusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SonusDatabase::class.java,
                    "sonus_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
