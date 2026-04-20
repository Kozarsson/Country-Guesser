package org.kth.countryguesser.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import org.kth.countryguesser.model.entity.UserEntity
import org.kth.countryguesser.model.service.MyFirebaseMessagingService
import javax.inject.Inject

interface FirebaseAuthRepository {
    suspend fun signInAnonymously(): Boolean
    suspend fun login(email: String, password: String): Boolean
    suspend fun register(email: String, password: String): Boolean
    fun getCurrentUser(): UserEntity?
    fun signOut()
}

class FirebaseAuthRepositoryImpl @Inject constructor() : FirebaseAuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun signInAnonymously(): Boolean {
        return try {
            MyFirebaseMessagingService.disableTokenAutoInit()
            val result = auth.signInAnonymously().await()
            result.user?.toUser() != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun login(email: String, password: String): Boolean {
        val authResult = auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
        if (authResult.user != null) {
            MyFirebaseMessagingService.enableTokenAutoInit()
        }
        return authResult != null
    }

    override suspend fun register(email: String, password: String): Boolean {
        val authResult = auth.createUserWithEmailAndPassword(email.trim(), password.trim()).await()
        if (authResult.user != null) {
            MyFirebaseMessagingService.enableTokenAutoInit()
        }
        return authResult != null
    }

    override fun getCurrentUser(): UserEntity? {
        return auth.currentUser?.toUser()
    }

    override fun signOut() {
        auth.signOut()
        MyFirebaseMessagingService.disableTokenAutoInit()
    }

    private fun FirebaseUser.toUser(): UserEntity {
        return UserEntity(
            uid = this.uid,
            email = this.email,
            displayName = this.displayName,
            isAnonymous = this.isAnonymous
        )
    }

}