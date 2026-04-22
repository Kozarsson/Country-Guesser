package org.kth.countryguesser.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
        val tokenMap = mapOf(
            "uid" to user.uid,
            "token" to token,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        store.collection("userTokens")
            .document(user.uid)
            .set(tokenMap, SetOptions.merge())
    }

    suspend fun getRecipientToken(uid: String): String? {
        return try {
            val document = store.collection("userTokens")
                .document(uid)
                .get()
                .await()
            document.getString("token")
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching token", e)
            null
        }
    }

}