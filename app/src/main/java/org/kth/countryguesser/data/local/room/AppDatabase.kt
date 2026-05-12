package org.kth.countryguesser.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LastGuessedDailyRoomEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lastGuessedDailyDao(): LastGuessedDailyDao
}
