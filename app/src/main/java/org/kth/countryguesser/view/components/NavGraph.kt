package org.kth.countryguesser.view.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import org.kth.countryguesser.view.HomeScreen
import org.kth.countryguesser.view.LeaderboardScreen
import org.kth.countryguesser.view.LoginScreen
import org.kth.countryguesser.view.RegisterScreen
import org.kth.countryguesser.view.StudyScreen
import org.kth.countryguesser.view.UserScreen
import org.kth.countryguesser.view.GameScreen

@Composable
fun NavGraph(
    navController: NavHostController,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }

    AppModalNavigationDrawer(
        navController = navController,
        drawerState = drawerState,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME
        ) {
            composable(Routes.HOME) {
                HomeScreen(navController, onMenuClick = openDrawer)
            }
            composable(Routes.LOGIN) {
                LoginScreen(navController)
            }
            composable(Routes.REGISTER) {
                RegisterScreen(navController)
            }

            composable("${Routes.GAME}/{mode}") { stackEntry ->
                val mode = stackEntry.arguments?.getString("mode") ?: "daily"
                GameScreen(
                    navController = navController,
                    mode = mode,
                    onMenuClick = openDrawer,
                )
            }
            composable(Routes.LEADERBOARD) {
                LeaderboardScreen(navController, onMenuClick = openDrawer)
            }
            composable(Routes.PROFILE) {
                UserScreen(navController, onMenuClick = openDrawer)
            }
            composable(Routes.STUDY) {
                StudyScreen(navController, onMenuClick = openDrawer)
            }
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