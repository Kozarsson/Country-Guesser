package org.kth.countryguesser.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import org.kth.countryguesser.data.remote.entity.UserEntity
import org.kth.countryguesser.model.service.MyFirebaseMessagingService
import javax.inject.Inject

interface AuthRemoteDataSource {
    suspend fun signInAnonymously(): Boolean
    suspend fun login(email: String, password: String): Boolean
    suspend fun isEmailTaken(email: String): Boolean
    suspend fun register(nickname: String, email: String, password: String): Boolean
    suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean
    fun getCurrentUser(): UserEntity?
    fun signOut()
}

class AuthRemoteDataSourceImpl @Inject constructor() : AuthRemoteDataSource {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun signInAnonymously(): Boolean {
        return try {
            MyFirebaseMessagingService.disableTokenAutoInit()
            val result = auth.signInAnonymously().await()
            result.user?.getIdToken(true)?.await()
            result.user?.toUser() != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun login(email: String, password: String): Boolean {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
            if (authResult.user != null) {
                authResult.user?.getIdToken(true)?.await()
                MyFirebaseMessagingService.enableTokenAutoInit()
            }
            authResult.user != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun isEmailTaken(email: String): Boolean {
        return try {
            val signInMethods = auth.fetchSignInMethodsForEmail(email.trim()).await().signInMethods
            !signInMethods.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun register(nickname: String, email: String, password: String): Boolean {
        return try {
            val currentUser = auth.currentUser
            val credential = EmailAuthProvider.getCredential(email.trim(), password.trim())

            val user = if (currentUser?.isAnonymous == true) {
                currentUser.linkWithCredential(credential).await().user
            } else {
                auth.createUserWithEmailAndPassword(email.trim(), password.trim()).await().user
            }

            user?.let {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nickname.trim())
                    .build()
                it.updateProfile(profileUpdates).await()
                it.getIdToken(true).await()
                MyFirebaseMessagingService.enableTokenAutoInit()
            }

            user != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updatePassword(oldPassword: String, newPassword: String): Boolean {
        val user = auth.currentUser ?: return false
        val email = user.email ?: return false
        val credential = EmailAuthProvider.getCredential(email, oldPassword.trim())

        return try {
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword.trim()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getCurrentUser(): UserEntity? = auth.currentUser?.toUser()

    override fun signOut() {
        auth.signOut()
        MyFirebaseMessagingService.disableTokenAutoInit()
    }

    private fun FirebaseUser.toUser(): UserEntity {
        return UserEntity(
            uid = uid,
            email = email,
            displayName = displayName,
            isAnonymous = isAnonymous
        )
    }
}