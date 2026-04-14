package org.kth.countryguesser.view

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.viewmodel.AuthVM
import androidx.hilt.navigation.compose.hiltViewModel
import org.kth.countryguesser.model.CountryAttributeResult
import org.kth.countryguesser.viewmodel.GameVMImpl
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    navController: NavHostController,
    authViewModel: AuthVM,
    mode: String, // 'daily' or 'endless'
) {
    val user by authViewModel.userEntity.collectAsState()
    val gameVM = hiltViewModel<GameVMImpl>()
    val isGameWon by gameVM.gameWon.collectAsState()

    LaunchedEffect(mode) {
        gameVM.setGamemode(mode)
    }

    if (isGameWon) {
        SuccessScreen(
            onConfirm = {
                if (mode == "daily") {
                    navController.popBackStack()
                }
                // stay here (do nothing) if 'endless' mode
                gameVM.resetGameState()
            },
        )
    }
    GameScreenContent(
        navController = navController,
        bottomBar = {
            BottomBar(navController = navController, authViewModel = authViewModel, user = user)
        },
        mode = mode,
        vm = gameVM,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenContent(
    navController: NavHostController,
    bottomBar: @Composable () -> Unit = {},
    mode: String,
    vm: GameVMImpl
) {
    // TODO: add loading icon until answer country is fetched
    val context = LocalContext.current
    Scaffold(
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val answer = vm.getAnswer()
                    Toast.makeText(context, "Answer: $answer", Toast.LENGTH_SHORT).show()
                },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Icon(imageVector = Icons.Default.BugReport, contentDescription = "Reveal Answer")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
        ) {
            Header(navController, mode, vm.score.collectAsState().value)
            Spacer(modifier = Modifier.height(16.dp))
            Input(vm = vm)
            Spacer(modifier = Modifier.height(16.dp))
                    GuessedCountries(
                        guessedCountries = vm.guessedCountries.collectAsState().value,
                        modifier = Modifier.weight(1f)
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Input(
    vm: GameVMImpl
) {
    var guess by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val searchResults by vm.searchResults.collectAsState()
    val focusManager = LocalFocusManager.current

    // Trigger search on every text change
    LaunchedEffect(guess) {
        if (guess.isNotBlank()) {
            vm.searchCountries(guess)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            ExposedDropdownMenuBox(
                expanded = isFocused && searchResults.isNotEmpty(),
                onExpandedChange = { isFocused = it },
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    label = { Text("Guess") },
                    value = guess,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    onValueChange = { input ->
                        guess = input
                        isFocused = true
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryEditable,
                            enabled = true
                        ), // only used for emulator with keyboard
                    shape = RoundedCornerShape(8.dp),
                )

                ExposedDropdownMenu(
                    expanded = isFocused && searchResults.isNotEmpty(),
                    onDismissRequest = { isFocused = false },
                ) {
                    searchResults.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = "Flag",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        text = country,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            onClick = {
                                guess = country
                                isFocused = false
                            },
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    vm.guessCountry(guess)
                    focusManager.clearFocus()
                    guess = ""
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(56.dp),
            ) {
                Text(
                    text = "Submit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
private fun SuccessScreen(
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Correct!") },
        text = {
            Text("You found the correct country!")
        },
        confirmButton = {
            TextButton( onClick = {
                onConfirm()
            }) {
                Text("OK")
            }
        }
    )
}

@Composable
private fun GuessedCountries(
    guessedCountries: List<CountryUiModel>,
    modifier: Modifier = Modifier,
) {
    val cellSize = 96.dp
    var displayedCountries by remember { mutableStateOf(guessedCountries) }
    var revealingCountryKey by remember { mutableStateOf<String?>(null) }
    var revealCount by remember { mutableStateOf(4) }

    LaunchedEffect(guessedCountries) {
        if (guessedCountries.isEmpty()) {
            displayedCountries = emptyList()
            revealingCountryKey = null
            revealCount = 4
            return@LaunchedEffect
        }

        val newTop = guessedCountries.first()
        val oldTop = displayedCountries.firstOrNull()
        val isNewTop = oldTop?.countryName != newTop.countryName

        if (!isNewTop) {
            displayedCountries = guessedCountries
            return@LaunchedEffect
        }

        // Phase 1: insert a blank top row so existing rows shift down first.
        displayedCountries = listOf(newTop) + displayedCountries.filterNot { it.countryName == newTop.countryName }

        // Let list movement finish before revealing cells.
        revealingCountryKey = newTop.countryName
        revealCount = 0
        delay(220)

        // Phase 2: reveal new row attributes from left to right.
        repeat(4) {
            revealCount = it + 1
            delay(90)
        }

        // Keep VM list as source of truth after animation.
        displayedCountries = guessedCountries
        revealingCountryKey = null
        revealCount = 4
    }

    Column(modifier = modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Country", modifier = Modifier.width(cellSize), textAlign = TextAlign.Center)
            Text(text = "Population", modifier = Modifier.width(cellSize), textAlign = TextAlign.Center)
            Text(text = "Area", modifier = Modifier.width(cellSize), textAlign = TextAlign.Center)
            Text(text = "Inception Year", modifier = Modifier.width(cellSize), textAlign = TextAlign.Center)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .animateContentSize(
                    animationSpec = tween<IntSize>(durationMillis = 1000)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = displayedCountries,
                key = { it.countryName }
            ) { country ->
                val visibleAttributes = if (country.countryName == revealingCountryKey) revealCount else 4
                CountryRow(
                    country = country,
                    cellSize = cellSize,
                    visibleAttributes = visibleAttributes,
                )
            }
        }
    }
}

@Composable
private fun CountryRow(
    country: CountryUiModel,
    cellSize: Dp,
    visibleAttributes: Int = 4,
) {
    LazyRow(
        modifier = Modifier
            .width(cellSize * 4)
            .height(cellSize),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val attrs = listOf(
            country.countryName,
            country.population?.toString(),
            country.area?.toString(),
            country.inceptionYear?.year?.toString(),
            // TODO: add country flag
        )
        val diffs = listOf(
            null,
            country.populationDiff,
            country.areaDiff,
            country.inceptionYearDiff,
        )
        items(attrs.size) { idx ->
            val cellColor = countryAttributeGuessColor(diffs[idx])
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .padding(2.dp)
                    .background(
                        color = cellColor,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = idx < visibleAttributes,
                    enter = fadeIn(animationSpec = tween(1000)) +
                        slideInHorizontally(
                            initialOffsetX = { -it / 2 },
                            animationSpec = tween(1000)
                        ),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (diffs[idx] != null && diffs[idx]?.comparison != null && diffs[idx]?.comparison != 0) {
                            val arrow: ImageVector = if (diffs[idx]?.comparison == 1) {
                                Icons.Default.ArrowUpward
                            } else if (diffs[idx]?.comparison == -1) {
                                Icons.Default.ArrowDownward
                            } else {
                                Icons.Default.ErrorOutline
                            }
                            Icon(
                                imageVector = arrow,
                                contentDescription = "Flag",
                                tint = lerp(cellColor, Color.Black, 0.10f),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = attrs[idx] ?: "N/A",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.background,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun countryAttributeGuessColor(attrComparisonResult: CountryAttributeResult?): Color {
    if (attrComparisonResult == null) {
        return MaterialTheme.colorScheme.primary
    } else {
        if (attrComparisonResult.isClose == true && attrComparisonResult.comparison != 0) {
            return Color.Yellow
        }
        return when (attrComparisonResult.comparison) {
            -1 -> Color.Red
            1 -> Color.Red
            0 -> Color.Green
            else -> MaterialTheme.colorScheme.primary
        }
    }
}
