package org.kth.countryguesser.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.kth.countryguesser.Application
import org.kth.countryguesser.model.CountryModel
import org.kth.countryguesser.model.repository.CountryRepository
import org.kth.countryguesser.model.repository.GameRepository
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.ui.model.toUiModel
import org.kth.countryguesser.util.NetworkUtils
import org.kth.countryguesser.util.PopupState
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.random.Random


interface GameVM {
    val score: StateFlow<Int>
    val gameWon: StateFlow<Boolean>
    val guessedCountries: StateFlow<List<CountryUiModel>>
    val searchResults: StateFlow<List<Pair<String, String?>>>

    fun setGamemode(gamemode: String)
    fun searchCountries(searchQuery: String)
    fun guessCountry(country: String)
    fun resetGameState()

    fun getAnswer(): String // TODO: for debug purposes, remove later
}

@HiltViewModel
class GameVMImpl @Inject constructor(
    private val countryRepository: CountryRepository,
    private val gameRepository: GameRepository,
) : BaseVM(), GameVM {
    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score
    private val _gameWon = MutableStateFlow(false)
    override val gameWon: StateFlow<Boolean>
        get() = _gameWon
    private val targetCountry = MutableStateFlow<CountryModel?>(null)
    private val _gamemode = MutableStateFlow<String>("daily")

    private var isFetching: Boolean = false



    init {
        fetchCountry()
    }

    private fun fetchCountry() {
        viewModelScope.launch {
            if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
            } else {
                startTimerUntilLoadingPopup(3000)
                var countryName = ""
                val countries = countryRepository.getAllCountrySearchResults()

                if (_gamemode.value == "daily") {
                    val seed = LocalDate.now(ZoneOffset.UTC).toEpochDay()
                    countryName = countries.random(Random(seed)).first
                } else {
                    countryName = countries.random().first
                }

                val result = countryRepository.getCountryByName(countryName)
                targetCountry.value = result
                isFetching = false
            }
        }
    }

    private fun startTimerUntilLoadingPopup(timeMillis: Long) {
        isFetching = true
        viewModelScope.launch {
            delay(timeMillis)
            while (isFetching) {
                setPopupState(PopupState.LOADING)
            }
            setPopupState(PopupState.NONE) //This together with the while() prevents race conditions
        }
    }

    override fun setGamemode(gamemode: String) {
        _gamemode.value = gamemode
        _score.value = if (gamemode == "daily") 12 else 0
    }

    private val _guessedCountries = MutableStateFlow<List<CountryUiModel>>(listOf())
    override val guessedCountries: StateFlow<List<CountryUiModel>>
        get() = _guessedCountries

    private val _searchResults = MutableStateFlow<List<Pair<String, String?>>>(listOf())
    override val searchResults: StateFlow<List<Pair<String, String?>>>
        get() = _searchResults

    override fun searchCountries(searchQuery: String) {
        viewModelScope.launch {
            if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
            } else {
                val countries = countryRepository.getAllCountrySearchResults()
                val guessedCountries = _guessedCountries.value.map { it.countryName.lowercase() }
                val result = countries.filter {
                    it.first.contains(searchQuery, ignoreCase = true) &&
                            !guessedCountries.contains(it.first.lowercase())
                }
                val sortedResult = result.sortedWith(
                    compareBy<Pair<String, String?>> {
                        !it.first.startsWith(
                            searchQuery,
                            ignoreCase = true
                        )
                    }
                        .thenBy { it.first.lowercase() }
                )
                _searchResults.value = sortedResult.subList(0, 10.coerceAtMost(sortedResult.size))
            }
        }
    }

    override fun guessCountry(country: String) {
        if (targetCountry.value == null) {
            setError("Try again later")
            return
        }
        val guessedCountries = _guessedCountries.value.map { it.countryName.lowercase() }
        if (guessedCountries.contains(country.lowercase())) {
            setPopupState(PopupState.DUPLICATE_SEARCH)
            return
        }
        viewModelScope.launch {
            if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
            } else {
                startTimerUntilLoadingPopup(5000)
                val result = countryRepository.getCountryByName(country)
                if (result != null) {
                    val comp = targetCountry.value?.compareAttributesTo(result, 0.10)

                    if (targetCountry.value?.countryName == result.countryName) {
                        _gameWon.value = true
                        Log.d("GameVM", "Correct guess")

                        if (_gamemode.value == "endless") {
                            fetchCountry()
                            _score.value++
                        }
                    }
                    Log.d("GameVM", _guessedCountries.value.toString())
                    _guessedCountries.value = listOf(
                        result.toUiModel(
                            comp?.populationComparison,
                            comp?.areaComparison,
                            comp?.inceptionYearComparison,
                            comp?.continentsComparison
                        )
                    ) + _guessedCountries.value
                    if (_gamemode.value == "daily") {
                        _score.value = 12 - 2 * _guessedCountries.value.size
                    }

                    Log.d("GameVM", "Guessed country: ${_guessedCountries.value}")
                    setPopupState(PopupState.NONE)
                } else {
                    setPopupState(PopupState.NO_RESULT)
                    Log.e("GameVM", "No country found with name $country")
                }
                isFetching = false
            }
        }
    }

    override fun resetGameState() {
        _guessedCountries.value = listOf()
        _searchResults.value = listOf()
        _gameWon.value = false
    }

    override fun getAnswer(): String { // TODO: for debug purposes, remove later
        return targetCountry.value?.countryName.toString()
    }
}