package org.kth.countryguesser.view

import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.view.components.WIPAlert
import org.kth.countryguesser.viewmodel.AuthVM


@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    authViewModel: AuthVM,
) {
    val user by authViewModel.userEntity.collectAsState()

    WIPAlert(onPress = { navController.popBackStack() })  // TODO: remove when page is implemented

    StudyScreenContent(
        bottomBar = {
            BottomBar(navController = navController, authViewModel = authViewModel, user = user)
        },
    )
}

@Composable
private fun StudyScreenContent(
    bottomBar: @Composable () -> Unit = {},
) {
    Scaffold(
        bottomBar = bottomBar,
    ) { padding ->
        Text("WIP")
    }
}

