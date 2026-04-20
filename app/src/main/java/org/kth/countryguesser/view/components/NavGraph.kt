package org.kth.countryguesser.view.components

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.kth.countryguesser.view.HomeScreen
import org.kth.countryguesser.view.LeaderboardScreen
import org.kth.countryguesser.view.LoginScreen
import org.kth.countryguesser.view.RegisterScreen
import org.kth.countryguesser.view.StudyScreen
import org.kth.countryguesser.view.UserScreen
import org.kth.countryguesser.viewmodel.AuthVM
import org.kth.countryguesser.viewmodel.AuthVMImpl
import org.kth.countryguesser.viewmodel.GameVM
import org.kth.countryguesser.viewmodel.GameVMImpl

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }
        composable(Routes.REGISTER) {
            RegisterScreen(navController)
        }

        composable("${Routes.GAME}/{mode}") { stackEntry ->
            val mode = stackEntry.arguments?.getString("mode") ?: "daily"
            org.kth.countryguesser.view.GameScreen(
                navController = navController,
                mode = mode
            )
        }
        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(navController)
        }
        composable(Routes.PROFILE) {
            UserScreen(navController)
        }
        composable(Routes.STUDY) {
            StudyScreen(navController)
        }
    }

}

object Routes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val GAME = "game"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"

    const val STUDY = "study"

}