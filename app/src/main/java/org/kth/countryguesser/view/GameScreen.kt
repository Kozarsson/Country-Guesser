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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImagePainter
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.view.components.Routes
import org.kth.countryguesser.viewmodel.IAuthViewModel

@Composable
fun GameScreen(
    navController: NavHostController,
    authViewModel: IAuthViewModel,
    mode: String, // 'daily' or 'endless'
) {
    val user by authViewModel.userEntity.collectAsState()
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
        mode = mode,
        onCorrectGuess = { showSuccessDialog = true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenContent(
    navController: NavHostController,
    bottomBar: @Composable () -> Unit = {},
    mode: String,
    onCorrectGuess: () -> Unit,
) {
    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalAlignment = Ali
//                    ) {
//                        Text(text = mode.replaceFirstChar { it.uppercase() })
//                    } },
//                navigationIcon = {
//                    IconButton(onClick = {
//                        navController.navigate(Routes.HOME) {
//                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
//                        }
//                    }) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Return to Home"
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    titleContentColor = MaterialTheme.colorScheme.primary,
//                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                ),
//            )
//        },
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
        ) {
            Header(navController, mode)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    HorizontalDivider(thickness = 2.dp)
                    Text(
                        text = "Clues 3/5", // TODO: show num clues visible
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }

                // CLUES 1-5
                ClueBox(
                    numClues = 3, // TODO: show num clues visible
                    answers = listOf("10.2mil", "Europe", "Blue", "450k sqkm", "1200km"), // TODO: get from API
                )

                Spacer(modifier = Modifier.weight(1f))

                HorizontalDivider(thickness = 2.dp)
                Input(onCorrectGuess)
            }
        }

    }
}


@Composable
private fun Header(
    navController: NavHostController,
    mode: String,
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
                text = "6", // TODO: display current score
                style = TextStyle(
                    fontSize = 200.sp,
                    fontWeight = FontWeight.Bold,
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
    val labels = listOf("Population", "Region", "Color in Flag", "Area", "Last Guess") // subject to change

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
    onCorrectGuess: () -> Unit
) {
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
            value = "",
            textStyle = MaterialTheme.typography.bodyMedium,
            onValueChange = {/* TODO: set current guess */},
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
        )

        // SUBMIT BUTTON
        OutlinedButton(
            onClick = {
                // TODO: validate guess
                onCorrectGuess()
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
        mode = "daily",
        onCorrectGuess = { }
    )
}