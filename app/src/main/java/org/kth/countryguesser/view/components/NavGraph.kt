package org.kth.countryguesser.view.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.kth.countryguesser.view.HomeScreen
import org.kth.countryguesser.view.LoginScreen
import org.kth.countryguesser.view.RegisterScreen
import org.kth.countryguesser.view.StudyScreen
import org.kth.countryguesser.view.UserScreen
import org.kth.countryguesser.viewmodel.AuthVM
import org.kth.countryguesser.viewmodel.GameVM

@Composable
fun NavGraph(
    navController: NavHostController,
    authVM: AuthVM,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController, authVM)
        }
        composable(Routes.LOGIN) {
            LoginScreen(navController, authVM)
        }
        composable(Routes.REGISTER) {
            RegisterScreen(navController, authVM)
        }

        composable("${Routes.GAME}/{mode}") { stackEntry ->
            val mode = stackEntry.arguments?.getString("mode") ?: "daily"
            org.kth.countryguesser.view.GameScreen(
                navController = navController,
                authViewModel = authVM,
                mode = mode
            )
        }
        composable(Routes.PROFILE) {
            UserScreen(navController, authVM)
        }
        composable(Routes.STUDY) {
            StudyScreen(navController, authVM)
        }
    }

}

object Routes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val GAME = "game"
    const val PROFILE = "profile"

    const val STUDY = "study"

}