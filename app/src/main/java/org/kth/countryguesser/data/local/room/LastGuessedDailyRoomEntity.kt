package org.kth.countryguesser.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_guessed_daily")
data class LastGuessedDailyRoomEntity(
    @PrimaryKey val id: Int = 1,
    val date: String,
    val countryName: String,
    val flagUrl: String,
    val score: Int
)
