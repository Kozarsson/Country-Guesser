package org.kth.countryguesser.view.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.kth.countryguesser.view.HomeScreen
import org.kth.countryguesser.view.LoginScreen
import org.kth.countryguesser.view.RegisterScreen
import org.kth.countryguesser.viewmodel.IAuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authVM: IAuthViewModel
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
    }

}

object Routes {
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"

}