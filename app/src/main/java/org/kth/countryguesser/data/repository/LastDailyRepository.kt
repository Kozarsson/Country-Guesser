package org.kth.countryguesser.data.repository

import org.kth.countryguesser.data.local.room.LastGuessedDailyDao
import org.kth.countryguesser.data.local.room.LastGuessedDailyRoomEntity
import javax.inject.Inject

interface LastDailyRepository {
    suspend fun getLastGuessedDaily(): LastGuessedDailyRoomEntity?
    suspend fun saveLastGuessedDaily(date: String, countryName: String, flagUrl: String, score: Int)
    suspend fun clearLastGuessedDaily()
}

class LastDailyRepositoryImpl @Inject constructor(
    private val lastGuessedDailyDao: LastGuessedDailyDao
) : LastDailyRepository {

    override suspend fun getLastGuessedDaily(): LastGuessedDailyRoomEntity? {
        return lastGuessedDailyDao.getLastGuessedDaily()
    }

    override suspend fun saveLastGuessedDaily(date: String, countryName: String, flagUrl: String, score: Int) {
        lastGuessedDailyDao.upsertLastGuessedDaily(
            LastGuessedDailyRoomEntity(
                id = 1,
                date = date,
                countryName = countryName,
                flagUrl = flagUrl,
                score = score
            )
        )
    }

    override suspend fun clearLastGuessedDaily() {
        lastGuessedDailyDao.clearLastGuessedDaily()
    }
}
