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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.viewmodel.IAuthViewModel

@Composable
fun UserScreen(
    navController: NavHostController,
    authViewModel: IAuthViewModel,
) {
    val user by authViewModel.userEntity.collectAsState()

    UserScreenContent(
        bottomBar = {
            BottomBar(navController = navController, authViewModel = authViewModel, user = user)
        },
    )
}


@Preview(showBackground = true)
@Composable
private fun UserScreenContent(
    bottomBar: @Composable () -> Unit = {},
) {
    Scaffold(
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Header()

            Stats()
        }
    }
}


@Composable
private fun Header() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy((-50).dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
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
                .padding(start = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            // profile picture
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .width(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .border(8.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp))
                    .background(
                        Brush.verticalGradient( // TODO: replace with pfp
                            colors = listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer,
                            )
                        )
                    ),
            )

            // username
            Text(
                text = "John Doe", // TODO: replace
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Idea: we could do a 'current streak' with a fire icon showing the current number of days in a row played
        }
    }
}

@Composable
private fun Stats(
    // TODO: take stats from model
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

        // max day's in a row
        Stat(label = "Longest Streak", stat = "10d")

        // total score
        Stat(label = "Total Score", stat = "1,872")

        // average score
        Stat(label = "Avg. Score", stat = "4.2")


        // COUNTRY STREAK STATS
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            HorizontalDivider(thickness = 2.dp)
            Text(
                text = "Country Streak",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        // highest streak
        Stat(label = "Longest Streak", stat = "7")

        // average streak
        Stat(label = "Avg. Streak", stat = "2.3")
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