package com.example.sonus.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<SonusDatabase> {
    val dbFile = File(System.getProperty("user.home"), "sonus_database.db")
    return Room.databaseBuilder<SonusDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(dropAllTables = true)
}
