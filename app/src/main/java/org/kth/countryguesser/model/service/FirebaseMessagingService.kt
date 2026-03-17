package org.kth.countryguesser.model.service

import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kth.countryguesser.model.repository.FirebaseTokenRepository

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
//    private lateinit var notificationHelper: NotificationHelper
//
//    override fun onCreate() {
//        super.onCreate()
//        tokenRepository = FirebaseTokenRepository()
//        notificationHelper = NotificationHelper(applicationContext)
//    }
//
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        tokenRepository.saveTokenToFirestore(token)
    }
//
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//        if (remoteMessage.data.isNotEmpty()) {
//            CoroutineScope(Dispatchers.IO).launch {
//                if (remoteMessage.data["messageType"].equals("recipe_notification")) {
//                    notificationHelper.showRecipeNotification(
//                        remoteMessage.data["title"],
//                        remoteMessage.data["recipeId"]
//                    )
//                } else {
//                    notificationHelper.showNotification(
//                        remoteMessage.data["title"],
//                        remoteMessage.data["body"]
//                    )
//                }
//            }
//        }
//    }

}