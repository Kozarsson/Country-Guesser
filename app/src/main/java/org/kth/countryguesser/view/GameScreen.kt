package org.kth.countryguesser.view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ErrorOutline
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
import androidx.compose.ui.draw.clip
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
import androidx.hilt.navigation.compose.hiltViewModel
import org.kth.countryguesser.model.CountryAttributeResult
import org.kth.countryguesser.viewmodel.GameVMImpl
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import org.kth.countryguesser.util.PopupState
import org.kth.countryguesser.view.components.Alert
import org.kth.countryguesser.view.components.LoadingAlert
import org.kth.countryguesser.view.components.NoInternetAlert
import org.kth.countryguesser.view.components.TopBar
import java.util.Locale
import kotlin.math.abs

@Composable
fun GameScreen(
    navController: NavHostController,
    mode: String, // 'daily' or 'endless'
    onMenuClick: () -> Unit,
) {
    val gameVM = hiltViewModel<GameVMImpl>()
    val isGameWon by gameVM.gameWon.collectAsState()
    val popupState by gameVM.popupState.collectAsState()
    val errorMessage by gameVM.errorMessage.collectAsState()

    LaunchedEffect(mode) {
        gameVM.setGamemode(mode)
    }

        when (popupState) {
            PopupState.NONE -> {}
            PopupState.NO_RESULT -> {Alert(onPress = {gameVM.resetPopupState()}, title = "No results", message = "No country with that name found")}
            PopupState.LOADING -> {LoadingAlert("Loading...")}
            PopupState.DUPLICATE_SEARCH -> {Alert(onPress = {gameVM.resetPopupState()}, title = "Country already guessed", message = "You cannot guess the same country twice")}
            PopupState.ERROR -> {Alert(onPress = {gameVM.resetPopupState()}, title = "Error", message = errorMessage ?: "Unknown error, try again")}
            PopupState.NO_INTERNET ->{NoInternetAlert(onPress = {gameVM.resetPopupState()})}
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
        topBar = {
            TopBar(onMenuClick = onMenuClick)
        },
        bottomBar = {
            BottomBar(navController = navController)
        },
        mode = mode,
        vm = gameVM,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenContent(
    navController: NavHostController,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    mode: String,
    vm: GameVMImpl
) {
    val context = LocalContext.current
    Scaffold(
        topBar = topBar,
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
                                    AsyncImage(
                                        model = country.second,
                                        contentDescription = "Flag",
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                    Text(
                                        text = country.first,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            },
                            onClick = {
                                guess = country.first
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
    val headers = listOf("Country", "Population", "Area", "Continent", "Inception", "Bordering")
    val attributeCount = headers.size
    var displayedCountries by remember { mutableStateOf(guessedCountries) }
    var revealingCountryKey by remember { mutableStateOf<String?>(null) }
    var revealCount by remember { mutableStateOf(attributeCount) }

    LaunchedEffect(guessedCountries) {
        if (guessedCountries.isEmpty()) {
            displayedCountries = emptyList()
            revealingCountryKey = null
            revealCount = attributeCount
            return@LaunchedEffect
        }

        val newTop = guessedCountries.first()
        val oldTop = displayedCountries.firstOrNull()
        val isNewTop = oldTop?.countryName != newTop.countryName

        if (!isNewTop) {
            displayedCountries = guessedCountries
            return@LaunchedEffect
        }

        displayedCountries = listOf(newTop) + displayedCountries.filterNot { it.countryName == newTop.countryName }

        revealingCountryKey = newTop.countryName
        revealCount = 0
        delay(220)

        repeat(attributeCount) {
            revealCount = it + 1
            delay(90)
        }

        displayedCountries = guessedCountries
        revealingCountryKey = null
        revealCount = attributeCount
    }

    BoxWithConstraints(modifier = modifier.animateContentSize()) {
        val horizontalPadding = 16.dp
        val cellSize = ((maxWidth - horizontalPadding) / attributeCount).coerceIn(56.dp, 110.dp)
        val attributeFontSize = (cellSize.value * 0.18f).coerceIn(10f, 18f).sp

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.width(cellSize),
                        textAlign = TextAlign.Center,
                        fontSize = attributeFontSize,
                    )
                }
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
                    val visibleAttributes = if (country.countryName == revealingCountryKey) revealCount else attributeCount
                    CountryRow(
                        country = country,
                        cellSize = cellSize,
                        attributeFontSize = attributeFontSize,
                        visibleAttributes = visibleAttributes,
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryRow(
    country: CountryUiModel,
    cellSize: Dp,
    attributeFontSize: androidx.compose.ui.unit.TextUnit,
    visibleAttributes: Int,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(cellSize),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val attrs = listOf(
            //country.countryName,
            country.flagUrl,
            country.population,
            country.area,
            country.continents,
            country.inceptionYear?.year,
            //country.flagUrl
            if (country.bordersDiff?.comparison as Boolean) "Yes" else "No",
        )
        val diffs = listOf(
            null,
            country.populationDiff,
            country.areaDiff,
            country.continentsDiff,
            country.inceptionYearDiff,
            country.bordersDiff,
        )
        items(attrs.size) { idx ->
            val cellColor = lerp(countryAttributeGuessColor(diffs[idx]), Color.Black, 0.20f)
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
//                        if (diffs[idx] != null && diffs[idx]?.comparison != null && diffs[idx]?.comparison != 0 && diffs[idx]?.comparison != false && diffs[idx]?.comparison != true) {
                        if (diffs[idx]?.comparison == 1 || diffs[idx]?.comparison == -1) {
                            val arrow: ImageVector = if (diffs[idx]?.comparison == 1) {
                                Icons.Default.ArrowUpward
                            } else if (diffs[idx]?.comparison == -1) {
                                Icons.Default.ArrowDownward
                            } else {
                                Icons.Default.ErrorOutline
                            }
                            Icon(
                                imageVector = arrow,
                                contentDescription = "Arrow",
                                tint = lerp(cellColor, Color.Black, 0.10f),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if (idx == 0 ) {
                            if (attrs[idx] != null) {
                                AsyncImage(
                                    model = attrs[idx],
                                    contentDescription = "Flag",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Text(
                                    text = country.countryName.toString(),
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = attributeFontSize),
                                    color = MaterialTheme.colorScheme.background,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        } else {
                            var text = ""
                            text = when (val value = attrs[idx]) {
                                country.area -> displayNumber(value as Double) + " km²"
                                is Number -> displayNumber(value)
                                is List<*> -> value.filterNotNull().joinToString(separator = "\n") { it.toString() }
                                null -> "N/A"
                                else -> value.toString()
                            }
                            Text(
                                text = text,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = attributeFontSize),
                                color = MaterialTheme.colorScheme.background,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
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
            false -> Color.Red
            0 -> Color.Green
            true -> Color.Green
            else -> MaterialTheme.colorScheme.primary
        }
    }
}

private fun displayNumber(attribute: Number?): String {
    val value = attribute?.toDouble() ?: return "N/A"
    val absValue = abs(value)

    return when {
        absValue >= 1_000_000_000 -> String.format(Locale.UK, "%.1fB", value / 1_000_000_000)
        absValue >= 1_000_000     -> String.format(Locale.UK, "%.1fM", value / 1_000_000)
        absValue >= 10_000         -> String.format(Locale.UK, "%.1fK", value / 10_000)
        else                      -> value.toLong().toString()
    }
}
