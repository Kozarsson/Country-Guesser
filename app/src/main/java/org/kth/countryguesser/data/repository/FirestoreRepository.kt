package org.kth.countryguesser.data.repository

import org.kth.countryguesser.data.remote.firebase.AuthRemoteDataSource
import org.kth.countryguesser.data.remote.firebase.FirestoreRemoteDataSource
import org.kth.countryguesser.model.entity.UserEntity
import org.kth.countryguesser.model.entity.UserProfileEntity
import org.kth.countryguesser.model.entity.UserStatsEntity
import org.kth.countryguesser.util.getCurrentDateFromFirebase
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

interface FirestoreRepository {
    fun getCurrentUser(): UserEntity?
    suspend fun updateStats(stats: UserStatsEntity): Boolean
    suspend fun updateGamesPlayed(): Boolean
    suspend fun updateGamesPlayed(gamesPlayed: Int): Boolean
    suspend fun updateStreak(mode: String): Boolean
    suspend fun resetStreak(mode: String): Boolean
    suspend fun getUserProfile(): UserProfileEntity?
}

class FirestoreRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val firestoreRemoteDataSource: FirestoreRemoteDataSource
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

    override suspend fun updateGamesPlayed(): Boolean {
        val user = getCurrentUser() ?: return false
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return false
        return updateGamesPlayed(profile.stats.gamesPlayed + 1)
    }

    override suspend fun updateGamesPlayed(gamesPlayed: Int): Boolean {
        val user = getCurrentUser() ?: return false
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return false //TODO: Rewrite to remove duplication of code
        val newStats = profile.stats.copy(gamesPlayed = gamesPlayed)
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
            val lastDate = LocalDate.parse(profile.stats.lastGuessedDaily)
            val streak = if (ChronoUnit.DAYS.between(lastDate, todayDate) > 1) {
                0
            } else {
                profile.stats.currentStreakDaily
            }

            newStats = if (profile.stats.bestStreakDaily < streak + 1) {
                profile.stats.copy(
                    bestStreakDaily = streak + 1,
                    currentStreakDaily = streak + 1,
                    lastGuessedDaily = todayDate.toString(),
                )
            } else {
                profile.stats.copy(
                    currentStreakDaily = streak + 1,
                    lastGuessedDaily = todayDate.toString(),
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

    override suspend fun getUserProfile(): UserProfileEntity? {
        val user = getCurrentUser() ?: return null
        return firestoreRemoteDataSource.getProfile(user.uid)
    }

}