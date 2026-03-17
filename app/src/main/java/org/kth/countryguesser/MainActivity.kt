package org.kth.countryguesser

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kth.countryguesser.model.repository.FirebaseAuthRepository
import org.kth.countryguesser.model.repository.FirebaseTokenRepository
import org.kth.countryguesser.ui.theme.DefaultTheme
import org.kth.countryguesser.view.components.NavGraph
import org.kth.countryguesser.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        initializeViewModels()
//        handleIncomingNotification()

        setContent {
            DefaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        authVM = authViewModel
                    )
                }
            }
        }
    }

//    private fun handleIncomingNotification() {
//        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
//        if (notificationId != -1) {
//            NotificationManagerCompat.from(this).cancel(notificationId)
//        }
//    }

    private fun initializeViewModels() {
        val firebaseAuthRepository = FirebaseAuthRepository()
        val tokenRepository = FirebaseTokenRepository()

        val authViewModelFactory = AuthViewModel.Factory(firebaseAuthRepository)

        authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
    }
}