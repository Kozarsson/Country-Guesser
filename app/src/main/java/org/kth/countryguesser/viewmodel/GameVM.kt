package org.kth.countryguesser.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kth.countryguesser.model.CountryModel
import org.kth.countryguesser.model.repository.CountryRepository
import org.kth.countryguesser.model.repository.GameRepository
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.ui.model.toUiModel
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.random.Random


interface GameVM {
    val score: StateFlow<Int>
    val gameWon: StateFlow<Boolean>
    val guessedCountries: StateFlow<List<CountryUiModel>>
    val searchResults: StateFlow<List<String>>

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
) : ViewModel(), GameVM {
    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score
    private val _gameWon = MutableStateFlow(false)
    override val gameWon: StateFlow<Boolean>
        get() = _gameWon
    private val targetCountry = MutableStateFlow<CountryModel?>(null)
    private val _gamemode = MutableStateFlow<String>("daily")

    init {
        fetchCountry()
    }

    fun fetchCountry() {
        viewModelScope.launch {
            var countryName = ""
            val countries = countryRepository.getAllCountryNames()

            if (_gamemode.value == "daily") {
                val seed = LocalDate.now(ZoneOffset.UTC).toEpochDay()
                countryName = countries.random(Random(seed))
            } else {
                countryName = countries.random()
            }

            val result = countryRepository.getCountryByName(countryName)
            targetCountry.value = result
        }
    }

    override fun setGamemode(gamemode: String) {
        _gamemode.value = gamemode
        _score.value = if (gamemode == "daily") 12 else 0
    }

    private val _guessedCountries = MutableStateFlow<List<CountryUiModel>>(listOf())
    override val guessedCountries: StateFlow<List<CountryUiModel>>
        get() = _guessedCountries

    private val _searchResults = MutableStateFlow<List<String>>(listOf())
    override val searchResults: StateFlow<List<String>>
        get() = _searchResults

    override fun searchCountries(searchQuery: String) {
        viewModelScope.launch {
            val countries = countryRepository.getAllCountryNames()
            val result = countries.filter {
                it.contains(searchQuery, ignoreCase = true)
            }
            val sortedResult = result.sortedWith(
                compareBy<String> { !it.startsWith(searchQuery, ignoreCase = true) }
                    .thenBy { it.lowercase() }
            )
            _searchResults.value = sortedResult
        }
    }

    override fun guessCountry(country: String) {
        viewModelScope.launch {
            val result = countryRepository.getCountryByName(country)
            if (result != null) {
                val comp = targetCountry.value?.compareAttributesTo(result)

                if (targetCountry.value?.countryName == result.countryName) {
                    _gameWon.value = true
                    Log.d("GameVM", "Correct guess")

                    if (_gamemode.value == "endless") {
                        fetchCountry()
                        _score.value++
                    }
                }

                _guessedCountries.value = listOf(
                    result.toUiModel(
                        comp?.populationComparison?.comparison,
                        comp?.areaComparison?.comparison,
                        comp?.inceptionYearComparison?.comparison
                    )
                ) + _guessedCountries.value
                if (_gamemode.value == "daily") {
                    _score.value = 12 - 2 * _guessedCountries.value.size
                }

                Log.d("GameVM", "Guessed country: ${_guessedCountries.value}")
            } else {
                Log.e("GameVM", "No country found with name $country")
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


enum class PopupState {
    NONE,
    SEARCH,
    LOADING,
    NO_RESULT
}