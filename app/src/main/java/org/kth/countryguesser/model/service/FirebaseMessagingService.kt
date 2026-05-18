package org.kth.countryguesser.model.service

import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kth.countryguesser.data.repository.FirebaseTokenRepository

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        fun enableTokenAutoInit() {
            Firebase.messaging.isAutoInitEnabled = true
        }

        fun disableTokenAutoInit() {
            Firebase.messaging.isAutoInitEnabled = false
            Firebase.messaging.deleteToken()
        }
    }

    private lateinit var tokenRepository: FirebaseTokenRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        tokenRepository.saveTokenToFirestore(token)
    }
}