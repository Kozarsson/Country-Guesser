package org.kth.countryguesser.data.repository

import org.kth.countryguesser.data.remote.firebase.AuthRemoteDataSource
import org.kth.countryguesser.data.remote.firebase.FirestoreRemoteDataSource
import org.kth.countryguesser.data.remote.entity.LastGuessedDailyEntity
import org.kth.countryguesser.data.remote.entity.UserEntity
import org.kth.countryguesser.data.remote.entity.UserProfileEntity
import org.kth.countryguesser.data.remote.entity.UserSettingsEntity
import javax.inject.Inject

interface FirebaseAuthRepository {
    suspend fun signInAnonymously(): Boolean
    suspend fun login(email: String, password: String): Boolean
    suspend fun register(nickname: String, email: String, password: String): RegistrationResult
    fun getCurrentUser(): UserEntity?
    suspend fun getUserProfile(): UserProfileEntity?
    suspend fun updateNickname(nickname: String): Boolean
    suspend fun updateSettings(settings: UserSettingsEntity): Boolean
    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean
    fun signOut()
}

sealed interface RegistrationResult {
    data object Success : RegistrationResult
    data object EmailTaken : RegistrationResult
    data object NicknameTaken : RegistrationResult
    data class Error(val message: String) : RegistrationResult
}

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val firestoreRemoteDataSource: FirestoreRemoteDataSource,
    private val lastDailyRepository: LastDailyRepository,
    private val firestoreRepository: FirestoreRepository
) : FirebaseAuthRepository {

    override suspend fun signInAnonymously(): Boolean {
        val success = authRemoteDataSource.signInAnonymously()
        if (success) {
            runCatching { bootstrapProfileForCurrentUser() }
        }
        return success
    }

    override suspend fun login(email: String, password: String): Boolean {
        val success = authRemoteDataSource.login(email, password)
        if (success) {
            runCatching { bootstrapProfileForCurrentUser() }
            runCatching { syncLocalDailyGuessToFirestoreIfNeeded() }
        }
        return success
    }

    override suspend fun register(nickname: String, email: String, password: String): RegistrationResult {
        val normalizedNickname = nickname.trim()
        val normalizedEmail = email.trim()

        if (normalizedNickname.isEmpty()) {
            return RegistrationResult.Error("Nickname is required.")
        }
        if (normalizedEmail.isEmpty()) {
            return RegistrationResult.Error("Email is required.")
        }
        if (password.isBlank()) {
            return RegistrationResult.Error("Password is required.")
        }

        if (authRemoteDataSource.isEmailTaken(normalizedEmail)) {
            return RegistrationResult.EmailTaken
        }

        val success = authRemoteDataSource.register(normalizedNickname, normalizedEmail, password)
        if (!success) {
            return if (authRemoteDataSource.isEmailTaken(normalizedEmail)) {
                RegistrationResult.EmailTaken
            } else {
                RegistrationResult.Error("This email address is already in use, try another one.")
            }
        }

        val currentUser = getCurrentUser() ?: return RegistrationResult.Error("Missing authenticated user.")

        return try {
            firestoreRemoteDataSource.createProfileForRegistration(
                currentUser.uid,
                normalizedNickname
            )
            syncLocalDailyGuessToFirestoreIfNeeded()
            RegistrationResult.Success
        } catch (e: IllegalStateException) {
            authRemoteDataSource.signOut()
            RegistrationResult.NicknameTaken
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            authRemoteDataSource.signOut()
            RegistrationResult.Error("Could not create profile.")
        } catch (e: Exception) {
            RegistrationResult.Error("Account created, but profile setup failed.")
        }
    }

    override fun getCurrentUser(): UserEntity? = authRemoteDataSource.getCurrentUser()

    override suspend fun getUserProfile(): UserProfileEntity? {
        val user = getCurrentUser() ?: return null
        return try {
            firestoreRemoteDataSource.createProfileIfMissing(user.uid, user.defaultNickname())
            firestoreRemoteDataSource.getProfile(user.uid)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateNickname(nickname: String): Boolean {
        val user = getCurrentUser() ?: return false
        return try {
            firestoreRemoteDataSource.updateNickname(user.uid, nickname)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateSettings(settings: UserSettingsEntity): Boolean {
        val user = getCurrentUser() ?: return false
        return try {
            firestoreRemoteDataSource.updateSettings(user.uid, settings)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val user = getCurrentUser() ?: return false
        return try {
            authRemoteDataSource.updatePassword(oldPassword, newPassword)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun signOut() = authRemoteDataSource.signOut()

    private suspend fun syncLocalDailyGuessToFirestoreIfNeeded() {
        val user = getCurrentUser() ?: return
        val localDaily = lastDailyRepository.getLastGuessedDaily() ?: return
        if (localDaily.date.isBlank() && localDaily.countryName.isBlank() && localDaily.flagUrl.isBlank()) {
            return
        }
        val profile = firestoreRemoteDataSource.getProfile(user.uid) ?: return
        val remoteDaily = profile.lastGuessedDaily
        if (
            remoteDaily.date == localDaily.date &&
            remoteDaily.countryName == localDaily.countryName &&
            remoteDaily.flagUrl == localDaily.flagUrl
        ) {
            return
        }
        firestoreRemoteDataSource.updateLastDailyGuess(
            user.uid,
            LastGuessedDailyEntity(
                date = localDaily.date,
                countryName = localDaily.countryName,
                flagUrl = localDaily.flagUrl
            )
        )
        firestoreRepository.updateStreak("daily")
        firestoreRepository.updateGamesPlayed("daily")
        firestoreRepository.updateScore(localDaily.score)
    }

    private suspend fun bootstrapProfileForCurrentUser(preferredNickname: String? = null) {
        val user = getCurrentUser() ?: return
        firestoreRemoteDataSource.createProfileIfMissing(
            user.uid,
            preferredNickname ?: user.defaultNickname()
        )
    }

    private fun UserEntity.defaultNickname(): String =
        displayName?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@").orEmpty()

}