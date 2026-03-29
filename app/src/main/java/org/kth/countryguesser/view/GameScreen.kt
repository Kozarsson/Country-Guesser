package org.kth.countryguesser.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.kth.countryguesser.model.repository.GameModel
import org.kth.countryguesser.model.repository.GameModelImpl
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.viewmodel.IAuthViewModel

@Composable
fun GameScreen(
    navController: NavHostController,
    authViewModel: IAuthViewModel,
    mode: String, // 'daily' or 'endless'
) {
    val user by authViewModel.userEntity.collectAsState()
    val game = remember { GameModelImpl(mode) }
    LaunchedEffect(Unit) {
        game.fetchCountry()
    }

    var showSuccessDialog by remember { mutableStateOf(false) }
    if (showSuccessDialog) {
        SuccessScreen(onConfirm = {
            showSuccessDialog = false
            if (mode == "daily") {
                navController.popBackStack()
            }
            // stay here (do nothing) if 'endless' mode
        })
    }

    GameScreenContent(
        navController = navController,
        bottomBar = {
            BottomBar(navController = navController, authViewModel = authViewModel, user = user)
        },
        game = game,
        mode = mode,
        onCorrectGuess = { showSuccessDialog = true },
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenContent(
    navController: NavHostController,
    bottomBar: @Composable () -> Unit = {},
    game: GameModel,
    mode: String,
    onCorrectGuess: () -> Unit,
) {
    Scaffold(
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
        ) {
            Header(navController, mode, game.score)
            if (game.country == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // TODO: add loading indicator
                    Text("Loading Country Data...", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        HorizontalDivider(thickness = 2.dp)
                        Text(
                            text = "Clues ${game.numClues}/5",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }

                    // CLUES 1-5
                    ClueBox(
                        numClues = game.numClues,
                        answers = game.getClues(),
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider(thickness = 2.dp)
                    Input(game, onCorrectGuess)
                }
            }
        }

    }
}


@Composable
private fun Header(
    navController: NavHostController,
    mode: String,
    score: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = mode.replaceFirstChar { it.uppercase() },
                style = TextStyle(
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                ),

            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "$score",
                style = TextStyle(
                    fontSize = 200.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-25).sp,
                    color = MaterialTheme.colorScheme.surfaceTint,
                ),
            )
            Text(
                text = "pts",
                style = TextStyle(
                    fontSize = 60.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-3).sp,
                    color = MaterialTheme.colorScheme.surfaceTint,
                ),
                modifier = Modifier.padding(bottom = 20.dp, end = 20.dp),
            )
        }

    }
}


@Composable
private fun ClueBox(
    numClues: Int = 1,
    answers: List<String>,
) {
    val labels = listOf("Population", "Area", "Color in Flag", "Region", "Last Guess") // subject to change

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            Clue(
                label = label,
                info = answers.getOrElse(index) { "N/A" },
                enabled = numClues > index,
            )
        }
    }

    // Note to self: display color as an actual color (not in text)
}

@Composable
private fun Clue(
    label: String,
    info: String,
    enabled: Boolean = false,
) {
    if (enabled)
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
                    text = info,
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

@Composable
private fun Input(
    game: GameModel,
    onCorrectGuess: () -> Unit,
) {
    var guess by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        // INPUT BOX
        OutlinedTextField(
            label = { Text("Guess") },
            value = guess,
            textStyle = MaterialTheme.typography.bodyMedium,
            onValueChange = { inp -> guess = inp},
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
        )

        // SUBMIT BUTTON
        OutlinedButton(
            onClick = {
                if (game.checkGuess(guess)) {
                    onCorrectGuess()
                    guess = ""
                }
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(56.dp), // aligns with text field outline
        ) {
            Text(
                text = "Submit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun SuccessScreen(
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Correct!") },
        text = { Text("You found the correct country!") },
        confirmButton = {
            TextButton( onClick = {
                onConfirm()
            }) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
//    SuccessScreen(onConfirm = {})
    val navController = rememberNavController()
    GameScreenContent(
        navController = navController,
        game = GameModelImpl(),
        mode = "daily",
        onCorrectGuess = { }
    )
}