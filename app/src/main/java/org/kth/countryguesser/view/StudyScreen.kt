package org.kth.countryguesser.view

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.kth.countryguesser.util.InceptionYear
import org.kth.countryguesser.util.PopupState
import org.kth.countryguesser.view.components.Alert
import org.kth.countryguesser.view.components.BottomBar
import org.kth.countryguesser.view.components.LoadingAlert
import org.kth.countryguesser.view.components.NoInternetAlert
import org.kth.countryguesser.view.components.TopBar
import org.kth.countryguesser.viewmodel.StudyVMImpl
import java.text.NumberFormat
import java.util.Locale


@Composable
fun StudyScreen(
    navController: NavHostController,
    onMenuClick: () -> Unit,
) {
    val studyVM = hiltViewModel<StudyVMImpl>()
    val popupState by studyVM.popupState.collectAsState()
    val errorMessage by studyVM.errorMessage.collectAsState()

    when (popupState) {
        PopupState.NONE -> {}
        PopupState.LOADING -> {LoadingAlert("Loading...")}
        PopupState.ERROR -> {Alert(onPress = {studyVM.resetPopupState()}, title = "Error", message = errorMessage ?: "Unknown error, try again")}
        PopupState.NO_INTERNET -> {NoInternetAlert(onPress = {studyVM.resetPopupState()})}
    }

    StudyScreenContent(
        topBar = {
            TopBar(onMenuClick = onMenuClick)
        },
        bottomBar = {
            BottomBar(navController = navController)
        },
        vm = studyVM,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudyScreenContent(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    vm: StudyVMImpl,
) {
    val countryInfo by vm.countryInfo.collectAsState()
    val countries by vm.searchResults.collectAsState()
    val listState = rememberLazyListState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(countries) {
        if (countries.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
        ) {
            // searchable/scrollable list of countries
            SearchBox(vm)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .animateContentSize(
                        animationSpec = tween<IntSize>(durationMillis = 1000)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = countries,
                    key = { country: Pair<String, String?> -> country.first }
                ) { country: Pair<String, String?> ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = country.first,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                vm.fetchCountry(country.first)
                            },
                        trailingContent = {
                            AsyncImage(
                                model = country.second,
                                contentDescription = "Flag",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }

            // on select: display country info in popup
            if (countryInfo != null) {
                ModalBottomSheet(
                    onDismissRequest = { vm.clearCountryInfo() },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    CountryInfo(
                        name = countryInfo!!.countryName,
                        population = countryInfo!!.population,
                        area = countryInfo!!.area,
                        continents = countryInfo!!.continents,
                        inceptionYear = countryInfo!!.inceptionYear,
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryInfo(
    name: String,
    population: Long?,
    area: Double?,
    continents: List<String>?,
    inceptionYear: InceptionYear?,
) {
    val formatter = NumberFormat.getInstance(Locale.getDefault())

    // format larger numbers
    val formatPopulation = formatter.format(population ?: "N/A")
    val formatArea = formatter.format(area ?: "N/A")


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = name, style = MaterialTheme.typography.titleLarge)
        Stat(label = "Population", stat = formatPopulation)
        Stat(label = "Area", stat = "$formatArea km²")
        Stat(label = "Continent", stat = continents?.joinToString { it } ?: "N/A")
        Stat(label = "Inception Year", stat = inceptionYear?.year.toString())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBox(
    vm: StudyVMImpl,
) {
    var search by remember { mutableStateOf("") }

    LaunchedEffect(search) {
        vm.searchCountries(search)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        OutlinedTextField(
            label = { Text("Search") },
            value = search,
            textStyle = MaterialTheme.typography.bodyMedium,
            onValueChange = { input ->
                search = input
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
        )
    }
}