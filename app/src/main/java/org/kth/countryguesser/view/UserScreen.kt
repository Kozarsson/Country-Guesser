package org.kth.countryguesser.view


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import org.kth.countryguesser.util.PopupState
import org.kth.countryguesser.view.components.Alert
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.view.components.LoadingAlert
import org.kth.countryguesser.view.components.NoInternetAlert
import org.kth.countryguesser.view.components.TopBar
import org.kth.countryguesser.view.components.WIPAlert
import org.kth.countryguesser.viewmodel.AuthVMImpl
import org.kth.countryguesser.viewmodel.ProfileStatsVMImpl

@Composable
fun UserScreen(
    navController: NavHostController,
    onMenuClick: () -> Unit,
) {
    val authVM = hiltViewModel<AuthVMImpl>()
    val user by authVM.userEntity.collectAsState()
    val popupState by authVM.popupState.collectAsState()
    var errorMessage by remember { mutableStateOf("") }
    val profileStatsVM = hiltViewModel<ProfileStatsVMImpl>()
    val nickname = profileStatsVM.nickname.collectAsState()
    val gamesPlayedDaily = profileStatsVM.gamesPlayedDaily.collectAsState()
    // DAILY mode
    val currentStreakDaily = profileStatsVM.currentStreakDaily.collectAsState()
    val bestStreakDaily = profileStatsVM.bestStreakDaily.collectAsState()
    val totalScore = profileStatsVM.totalScore.collectAsState()
    // ENDLESS mode
    val gamesPlayedEndless = profileStatsVM.gamesPlayedEndless.collectAsState()
    val currentStreakEndless = profileStatsVM.currentStreakEndless.collectAsState()
    val bestStreakEndless = profileStatsVM.bestStreakEndless.collectAsState()


    when (popupState) {
        PopupState.NONE -> {}
        PopupState.LOADING -> {LoadingAlert("Loading...")}
        PopupState.ERROR -> {Alert(onPress = {profileStatsVM.resetPopupState()}, title = "Error", message = errorMessage ?: "Unknown error, try again")}
        PopupState.NO_INTERNET ->{NoInternetAlert(onPress = {profileStatsVM.resetPopupState()})}
        PopupState.TUTORIAL -> {}
    }


    UserScreenContent(
        topBar = {
            TopBar(onMenuClick = onMenuClick)
        },
        bottomBar = {
            BottomBar(navController = navController)
        },
        nickname = nickname.value,
        gamesPlayedDaily = gamesPlayedDaily.value,
        currentStreakDaily = currentStreakDaily.value,
        bestStreakDaily = bestStreakDaily.value,
        totalScore = totalScore.value,
        gamesPlayedEndless = gamesPlayedEndless.value,
        currentStreakEndless = currentStreakEndless.value,
        bestStreakEndless = bestStreakEndless.value,
    )
}


@Composable
private fun UserScreenContent(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    nickname: String,
    gamesPlayedDaily: Int,
    currentStreakDaily: Int,
    bestStreakDaily: Int,
    totalScore: Int,
    gamesPlayedEndless: Int,
    currentStreakEndless: Int,
    bestStreakEndless: Int,
) {
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Header(
                nickname,
                currentStreakDaily,
            )

            Stats(
                gamesPlayedDaily,
                currentStreakDaily,
                bestStreakDaily,
                totalScore,
                gamesPlayedEndless,
                currentStreakEndless,
                bestStreakEndless
            )
        }
    }
}


@Composable
private fun Header(
    nickname: String,
    currentStreakDaily: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy((-50).dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // username
            Text(
                text = nickname,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            // daily streak
            Row() {
                Text(
                    text = "\uD83D\uDD25",
                    fontSize = 36.sp,
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "$currentStreakDaily",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Daily Streak",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

        }
    }
}

@Composable
private fun Stats(
    gamesPlayedDaily: Int,
    currentStreakDaily: Int,
    bestStreakDaily: Int,
    totalScore: Int,
    gamesPlayedEndless: Int,
    currentStreakEndless: Int,
    bestStreakEndless: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // DAILY CHALLENGE STATS
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            HorizontalDivider(thickness = 2.dp)
            Text(
                text = "Daily Challenge",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        Stat(label = "Games Played", stat = "$gamesPlayedDaily")
        // max day's in a row
//        Stat(label = "Current Streak", stat = "$currentStreakDaily")

        Stat(label = "Longest Streak", stat = "$bestStreakDaily")

        // total score
        Stat(label = "Total Score", stat = "$totalScore")

        // average score
        Stat(label = "Avg. Score", stat = "${totalScore/maxOf(gamesPlayedDaily,1)}")


        // COUNTRY STREAK STATS
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            HorizontalDivider(thickness = 2.dp)
            Text(
                text = "Country Streak",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
        Stat(label = "Games Played", stat = "$gamesPlayedEndless")
        Stat(label = "Current Streak", stat = "$currentStreakEndless")
        Stat(label = "Longest Streak", stat = "$bestStreakEndless")

    }
}

@Composable
private fun Stat(
    label: String,
    stat: String,
) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        trailingContent = {
            Text(
                text = stat,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.surfaceTint,
                fontWeight = FontWeight.Bold,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    )
}