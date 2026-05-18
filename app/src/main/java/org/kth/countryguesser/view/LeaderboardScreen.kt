package org.kth.countryguesser.view

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.view.components.TopBar
import org.kth.countryguesser.view.components.WIPAlert
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import org.kth.countryguesser.ui.theme.AppTheme
import org.kth.countryguesser.viewmodel.GameVMImpl
import org.kth.countryguesser.viewmodel.LeaderboardVMImpl
import org.kth.countryguesser.viewmodel.ProfileStatsVMImpl


@Composable
fun LeaderboardScreen(
    navController: NavHostController,
    onMenuClick: () -> Unit,
) {
    val leaderboardVM = hiltViewModel<LeaderboardVMImpl>()

    LeaderboardScreenContent(
        topBar = {
            TopBar(onMenuClick = onMenuClick)
        },
        bottomBar = {
            BottomBar(navController = navController)
        },
        vm = leaderboardVM
    )
}

@Composable
private fun LeaderboardScreenContent(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    vm: LeaderboardVMImpl,
) {

    val players by vm.players.collectAsState()

    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .animateContentSize(
                        animationSpec = tween<IntSize>(durationMillis = 1000)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    LeaderboardTitle()
                }
                item {
                    PlayerHeaderRow("Streak")
                }
                itemsIndexed(
                    items = players,
                    key = { _, player -> player.nickname }
                ) { index, player ->
                    PlayerRow(
                        position = index + 1,
                        playerName = player.nickname,
                        stat = player.bestStreakDaily,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerHeaderRow(statName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Name",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = statName,
            modifier = Modifier.width(72.dp),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PlayerRow(
    position: Int,
    playerName: String,
    stat: Int
) {
    val medalColor = when (position) {
        1 -> AppTheme.colors.leaderBoardGold
        2 -> AppTheme.colors.leaderBoardSilver
        3 -> AppTheme.colors.leaderBoardBronze
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = medalColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = position.toString(),
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center,
                fontWeight = if (position <= 3) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = playerName,
                modifier = Modifier.weight(1f),
                fontWeight = if (position <= 3) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = stat.toString(),
                modifier = Modifier.width(72.dp),
                textAlign = TextAlign.End,
                fontWeight = if (position <= 3) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun LeaderboardTitle() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Country Guesser",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Daily Country Streak",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Leaderboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
