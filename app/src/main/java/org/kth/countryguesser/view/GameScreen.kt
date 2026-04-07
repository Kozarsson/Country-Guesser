package org.kth.countryguesser.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.viewmodel.GameVM
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.viewmodel.AuthVM
import androidx.hilt.navigation.compose.hiltViewModel
import org.kth.countryguesser.viewmodel.GameVMImpl

@Composable
fun GameScreen(
    navController: NavHostController,
    authViewModel: AuthVM,
    mode: String, // 'daily' or 'endless'
) {
    val user by authViewModel.userEntity.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    val gameVM = hiltViewModel<GameVMImpl>()

    // Reset guessed countries when mode changes or on first composition
    LaunchedEffect(mode) {
        //gameViewModelImpl.resetGuessedCountries()
    }

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
        vm = gameVM
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreenContent(
    navController: NavHostController,
    bottomBar: @Composable () -> Unit = {},
    mode: String,
    onCorrectGuess: () -> Unit,
    vm: GameVMImpl
) {


    Scaffold(
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
        ) {
            Header(navController, mode, 0) // TODO: Replace 0 with actual score
            Spacer(modifier = Modifier.height(16.dp))
            Input(onCorrectGuess = onCorrectGuess, vm = vm)
            Spacer(modifier = Modifier.height(16.dp))
//            if (vm.guessedCountries.collectAsState().value.isNotEmpty()) {
            GuessedCountries(guessedCountries = vm.guessedCountries.collectAsState().value)
//            }
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
private fun Input(
    onCorrectGuess: () -> Unit,
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
            OutlinedTextField(
                label = { Text("Guess") },
                value = guess,
                textStyle = MaterialTheme.typography.bodyMedium,
                onValueChange = { input -> guess = input },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isFocused = it.isFocused },
                shape = RoundedCornerShape(8.dp),
            )
            OutlinedButton(
                onClick = {
                    vm.guessCountry(guess)
                    focusManager.clearFocus()
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
        // Show suggestions dropdown
        if (isFocused && searchResults.isNotEmpty()) {
            SuggestionsDropdown(
                results = searchResults,
                onSuggestionClick = { country ->
                    guess = country.countryName
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 64.dp) // below the text field
            )
        }
    }
}

@Composable
private fun SuggestionsDropdown(
    results: List<CountryUiModel>,
    onSuggestionClick: (CountryUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        results.forEach { country ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSuggestionClick(country) }
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
                    text = country.countryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
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

@Composable
fun GuessedCountries(
    guessedCountries: List<CountryUiModel>,
) {
    val cellSize = 96.dp

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
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(5) { idx ->
            if (idx < guessedCountries.size) {
                val country = guessedCountries[idx]
                CountryRow(country, cellSize)
            } else {
                EmptyRow(cellSize)
            }
        }
    }
}

@Composable
fun CountryRow(
    country: CountryUiModel,
    cellSize: Dp,
) {
    Row(
        modifier = Modifier
            .width(cellSize * 4)
            .height(cellSize),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(cellSize)
                .padding(2.dp)
                .background(
                    color = getColor(country.countryName),
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = country.countryName, //TODO: replace with flag
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.background,
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .size(cellSize)
                .padding(2.dp)
                .background(
                    color = getColor(country.population?.toString()),
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = country.population?.toString() ?: "N/A",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.background,
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .size(cellSize)
                .padding(2.dp)
                .background(
                    color = getColor(country.area?.toString()),
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = country.area?.toString()?: "N/A",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.background,
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .size(cellSize)
                .padding(2.dp)
                .background(
                    color = getColor(country.inceptionYear?.year?.toString()),
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = country.inceptionYear?.year?.toString() ?: "N/A",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.background,
                maxLines = 1
            )
        }
    }
}
@Composable
private fun EmptyRow(cellSize: Dp) {
    LazyRow(
        modifier = Modifier
            .width(cellSize * 4)
            .height(cellSize),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(5) { idx ->
            Box(
                modifier = Modifier
                    .size(cellSize)
                    .padding(2.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {}
        }
    }
}

@Composable
private fun getColor(value: String?): Color {
    if (value != null) {
        // TODO: select colors based on value differential to answer
        return MaterialTheme.colorScheme.primary
    }

    return MaterialTheme.colorScheme.surfaceVariant
}
