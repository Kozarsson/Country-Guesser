package org.kth.countryguesser.model.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import org.kth.countryguesser.model.entity.UserEntity
import org.kth.countryguesser.model.service.MyFirebaseMessagingService

class FirebaseAuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun signInAnonymously(): Boolean {
        return try {
            MyFirebaseMessagingService.disableTokenAutoInit()
            val result = auth.signInAnonymously().await()
            result.user?.toUser() != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        val authResult = auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
        if (authResult.user != null) {
            MyFirebaseMessagingService.enableTokenAutoInit()
        }
        return authResult != null
    }

    suspend fun register(email: String, password: String): Boolean {
        val authResult = auth.createUserWithEmailAndPassword(email.trim(), password.trim()).await()
        if (authResult.user != null) {
            MyFirebaseMessagingService.enableTokenAutoInit()
        }
        return authResult != null
    }

    fun getCurrentUser(): UserEntity? {
        return auth.currentUser?.toUser()
    }

    fun signOut() {
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