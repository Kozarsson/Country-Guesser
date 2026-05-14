package org.kth.countryguesser.data.repository

import org.kth.countryguesser.data.remote.firebase.AuthRemoteDataSource
import org.kth.countryguesser.data.remote.firebase.FirestoreRemoteDataSource
import org.kth.countryguesser.util.getCurrentDateFromFirebase
import org.kth.countryguesser.data.remote.entity.UserEntity
import org.kth.countryguesser.data.remote.entity.UserProfileEntity
import org.kth.countryguesser.data.remote.entity.UserStatsEntity
import org.kth.countryguesser.data.remote.entity.LastGuessedDailyEntity
import org.kth.countryguesser.ui.model.PlayerUiModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

interface FirestoreRepository {
    fun getCurrentUser(): UserEntity?
    suspend fun updateStats(stats: UserStatsEntity): Boolean
    suspend fun updateGamesPlayed(mode: String, gamesPlayed: Int = -1): Boolean
    suspend fun updateStreak(mode: String): Boolean
    suspend fun updateLastDailyGuess(countryName: String, flagUrl: String, score: Int): Boolean
    suspend fun resetStreak(mode: String): Boolean
    suspend fun updateScore(score: Int): Boolean
    suspend fun getUserProfile(): UserProfileEntity?
    suspend fun getLeaderboard(): List<PlayerUiModel>?
}

class FirestoreRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val firestoreRemoteDataSource: FirestoreRemoteDataSource,
    private val lastDailyRepository: LastDailyRepository
) : FirestoreRepository {

    override fun getCurrentUser(): UserEntity? = authRemoteDataSource.getCurrentUser()

    override suspend fun updateStats(stats: UserStatsEntity): Boolean {
        val user = getCurrentUser() ?: return false
        return try {
            firestoreRemoteDataSource.updateStats(user.uid, stats)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateGamesPlayed(mode: String, gamesPlayed: Int): Boolean {
        val user = getCurrentUser() ?: return false
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return false

        val gamesCount = if (gamesPlayed == -1) {
            if (mode == "daily")
                profile.stats.gamesPlayedDaily + 1
            else
                profile.stats.gamesPlayedEndless + 1
        } else {
            gamesPlayed
        }

        val newStats = if (mode == "daily")
            profile.stats.copy(gamesPlayedDaily = gamesCount)
        else profile.stats.copy(gamesPlayedEndless = gamesCount)
        return try {
            firestoreRemoteDataSource.updateStats(user.uid, newStats)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateStreak(mode: String): Boolean {
        val user = getCurrentUser() ?: return false
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return false //TODO: Rewrite to remove duplication of code
        val newStats: UserStatsEntity
        if (mode == "daily") {
            val todayDate = getCurrentDateFromFirebase()
            val lastDaily = lastDailyRepository.getLastGuessedDaily()
            val lastDateStr = lastDaily?.date.orEmpty()
            val streak = if (lastDateStr.isNotEmpty()) {
                val lastDate = LocalDate.parse(lastDateStr)
                if (ChronoUnit.DAYS.between(lastDate, todayDate) > 1) {
                    0
                } else {
                    profile.stats.currentStreakDaily
                }
            } else {
                0
            }

            newStats = if (profile.stats.bestStreakDaily < streak + 1) {
                profile.stats.copy(
                    bestStreakDaily = streak + 1,
                    currentStreakDaily = streak + 1,
                    //lastGuessedDaily = todayDate.toString(),
                )
            } else {
                profile.stats.copy(
                    currentStreakDaily = streak + 1,
                    //lastGuessedDaily = todayDate.toString(),
                )
            }

        } else { // ENDLESS mode
            newStats = if (profile.stats.bestStreakEndless < profile.stats.currentStreakEndless + 1) {
                profile.stats.copy(
                    bestStreakEndless = profile.stats.currentStreakEndless + 1,
                    currentStreakEndless = profile.stats.currentStreakEndless + 1,
                )
            } else {
                profile.stats.copy(
                    currentStreakEndless = profile.stats.currentStreakEndless + 1,
                )
            }
        }
        return try {
            firestoreRemoteDataSource.updateStats(user.uid, newStats)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateLastDailyGuess(countryName: String, flagUrl: String, score: Int): Boolean {
        val user = getCurrentUser() ?: return false
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return false
        val todayDate = getCurrentDateFromFirebase()
        val lastDateStr = profile.lastGuessedDaily.date

        val newCurrentStreak = if (lastDateStr.isNotEmpty()) {
            val lastDate = LocalDate.parse(lastDateStr)
            val daysBetween = ChronoUnit.DAYS.between(lastDate, todayDate)
            when {
                daysBetween <= 0L -> profile.stats.currentStreakDaily
                daysBetween == 1L -> profile.stats.currentStreakDaily + 1
                else -> 1
            }
        } else {
            1
        }

        val newBestStreak = maxOf(profile.stats.bestStreakDaily, newCurrentStreak)
        val newStats = profile.stats.copy(
            gamesPlayedDaily = profile.stats.gamesPlayedDaily + 1,
            currentStreakDaily = newCurrentStreak,
            bestStreakDaily = newBestStreak,
            totalScore = profile.stats.totalScore + score
        )

        return try {
            firestoreRemoteDataSource.updateStats(user.uid, newStats)
            firestoreRemoteDataSource.updateLastDailyGuess(
                user.uid,
                LastGuessedDailyEntity(
                    date = todayDate.toString(),
                    countryName = countryName,
                    flagUrl = flagUrl
                )
            )
            lastDailyRepository.saveLastGuessedDaily(
                date = todayDate.toString(),
                countryName = countryName,
                flagUrl = flagUrl,
                score = score
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun resetStreak(mode: String): Boolean {
        val user = getCurrentUser() ?: return false
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return false
        val newStats: UserStatsEntity
        if (mode == "daily") {
            newStats = profile.stats.copy(currentStreakDaily = 0)
        } else { // ENDLESS mode
            newStats = profile.stats.copy(currentStreakEndless = 0)
        }
        return try {
            firestoreRemoteDataSource.updateStats(user.uid, newStats)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateScore(score: Int): Boolean {
        val user = getCurrentUser() ?: return false
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return false
        val newStats = profile.stats.copy(totalScore = profile.stats.totalScore + score)

        return try {
            firestoreRemoteDataSource.updateStats(user.uid, newStats)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserProfile(): UserProfileEntity? {
        val user = getCurrentUser() ?: return null
        return firestoreRemoteDataSource.getProfile(user.uid)
    }

    /**
     * Returns a list of [PlayerUiModel] sorted by longest daily streak in descending order.
     */
    override suspend fun getLeaderboard(): List<PlayerUiModel>? {
        val profiles = firestoreRemoteDataSource.getAllProfiles()
        return profiles.sortedByDescending { it.stats.bestStreakDaily }.map {
            PlayerUiModel(
                nickname = it.nickname,
                gamesPlayedDaily = it.stats.gamesPlayedDaily,
                currentStreakDaily = it.stats.currentStreakDaily,
                bestStreakDaily = it.stats.bestStreakDaily,
            )
        }
    }
}