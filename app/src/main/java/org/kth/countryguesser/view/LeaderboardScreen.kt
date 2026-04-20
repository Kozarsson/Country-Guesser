package org.kth.countryguesser.view

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.view.components.TopBar
import org.kth.countryguesser.view.components.WIPAlert


@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    onMenuClick: () -> Unit,
) {
    WIPAlert(onPress = { navController.popBackStack() })  // TODO: remove when page is implemented

    StudyScreenContent(
        topBar = {
            TopBar(onMenuClick = onMenuClick)
        },
        bottomBar = {
            BottomBar(navController = navController)
        },
    )
}

@Composable
private fun StudyScreenContent(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
) {
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text("WIP")
            }
        }
    )
}

