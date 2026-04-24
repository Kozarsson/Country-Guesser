package org.kth.countryguesser.data.repository

import org.kth.countryguesser.data.remote.firebase.AuthRemoteDataSource
import org.kth.countryguesser.data.remote.firebase.FirestoreRemoteDataSource
import org.kth.countryguesser.model.entity.UserEntity
import org.kth.countryguesser.model.entity.UserProfileEntity
import org.kth.countryguesser.model.entity.UserStatsEntity
import javax.inject.Inject

interface FirestoreRepository {
    fun getCurrentUser(): UserEntity?
    suspend fun updateStats(stats: UserStatsEntity): Boolean
    suspend fun updateGamesPlayed(): Boolean
    suspend fun updateGamesPlayed(gamesPlayed: Int): Boolean
    suspend fun updateStreak(): Boolean
    suspend fun resetStreak(): Boolean
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

    override suspend fun updateStreak(): Boolean {
        val user = getCurrentUser() ?: return false
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return false //TODO: Rewrite to remove duplication of code
        val newStats: UserStatsEntity
        if (profile.stats.bestStreak < profile.stats.currentStreak + 1) {
            newStats = profile.stats.copy(bestStreak = profile.stats.currentStreak + 1, currentStreak = profile.stats.currentStreak + 1)
        } else {
            newStats = profile.stats.copy(currentStreak = profile.stats.currentStreak + 1)
        }
        return try {
            firestoreRemoteDataSource.updateStats(user.uid, newStats)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun resetStreak(): Boolean {
        val user = getCurrentUser() ?: return false
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return false
        val newStats = profile.stats.copy(currentStreak = 0)
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