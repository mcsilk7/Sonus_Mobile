package com.example.sonus.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

object SonusDatabaseProvider {
    @Volatile
    private var INSTANCE: SonusDatabase? = null

    fun getDatabase(context: Context): SonusDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = getDatabaseBuilder(context)
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}
