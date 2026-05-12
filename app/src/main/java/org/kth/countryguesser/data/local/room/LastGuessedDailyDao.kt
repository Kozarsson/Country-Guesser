package org.kth.countryguesser.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LastGuessedDailyDao {
    @Query("SELECT * FROM last_guessed_daily WHERE id = 1")
    suspend fun getLastGuessedDaily(): LastGuessedDailyRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLastGuessedDaily(entity: LastGuessedDailyRoomEntity)

    @Query("DELETE FROM last_guessed_daily")
    suspend fun clearLastGuessedDaily()
}

