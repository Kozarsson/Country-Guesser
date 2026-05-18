package org.kth.countryguesser.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.kth.countryguesser.util.getCurrentDateFromFirebase
import org.kth.countryguesser.util.getTimeUntilNextDay
import org.kth.countryguesser.util.formatCountdownTime
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.view.components.Routes
import org.kth.countryguesser.view.components.TopBar
import org.kth.countryguesser.viewmodel.LastGuessedDaily
import org.kth.countryguesser.viewmodel.ProfileStatsVMImpl

@Composable
fun HomeScreen(
    navController: NavHostController,
    onMenuClick: () -> Unit,
) {
    val profileStatsVM = hiltViewModel<ProfileStatsVMImpl>()
    val lastGuessedDaily by profileStatsVM.lastGuessedDaily.collectAsState()
    val currentStreakEndless by profileStatsVM.currentStreakEndless.collectAsState()
    var timeUntilNextDay by remember { mutableLongStateOf(getTimeUntilNextDay()) }
    
    


    LaunchedEffect(Unit) {
        profileStatsVM.refreshStats()
    }

    val today = getCurrentDateFromFirebase().toString()
    val isDailyDone = lastGuessedDaily.date == today

    LaunchedEffect(Unit) {
        while (timeUntilNextDay > 0) {
            delay(1000)
            timeUntilNextDay -= 1000
        }
    }

    Scaffold(
        topBar = {
            TopBar(onMenuClick = onMenuClick)
        },
        bottomBar = {
            BottomBar(navController = navController)
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background,
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                TodaysCountry(isDailyDone, lastGuessedDaily)

				MenuActionButton(
					label = "DAILY ${if (isDailyDone) "(time until next country: " + formatCountdownTime(timeUntilNextDay) + ")" else ""}",
					icon = Icons.Filled.SportsEsports,
                    enabled = !isDailyDone,
					onClick = {
                        navController.navigate("${Routes.GAME}/daily")
                    },
				)
                MenuActionButton(
                    label = "ENDLESS ${ if (currentStreakEndless > 2 ) "(${currentStreakEndless} streak)" else ""}",
                    icon = Icons.Filled.AllInclusive,
                    onClick = {
                        navController.navigate("${Routes.GAME}/endless")
                    },
                )
                MenuActionButton(
                    label = "LEADERBOARDS",
                    icon = Icons.Filled.EmojiEvents,
                    onClick = { navController.navigate(Routes.LEADERBOARD) },
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun MenuActionButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
            )
            Text(label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun TodaysCountry(
    isDailyDone: Boolean,
    lastGuessedDaily: LastGuessedDaily,
) {

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxSize(0.8f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TODAY'S COUNTRY${if (isDailyDone) ": " + lastGuessedDaily.countryName?.uppercase() else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if(isDailyDone) {
                        AsyncImage(
                            model = lastGuessedDaily.flagUrl!!,
                            contentDescription = "Flag",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color.Black, shape = MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "?",
                                style = MaterialTheme.typography.displayLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
