package com.example.sonus.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<SonusDatabase> {
    val dbFile = context.getDatabasePath("sonus_database")
    return Room.databaseBuilder<SonusDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath
    ).fallbackToDestructiveMigration(dropAllTables = true)
}
