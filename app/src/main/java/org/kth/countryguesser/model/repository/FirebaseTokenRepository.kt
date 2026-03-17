package org.kth.countryguesser.model.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseTokenRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun saveTokenToFirestore(token: String) {
        val user = auth.currentUser
        if (user == null) {
            Log.e("Firestore", "User is null")
            return
        }
        user.let {
            val tokenMap = mapOf("token" to token)
            store.collection("users")
                .document(user.email!!)
                .set(tokenMap, SetOptions.merge())
        }
    }

    suspend fun getRecipientToken(email: String): String? {
        return try {
            val document = store.collection("users")
                .document(email)
                .get()
                .await()
            document.getString("token")
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching token", e)
            null
        }
    }

}