package org.kth.countryguesser.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
import kotlin.math.max
import kotlin.random.Random


interface GameVM {
    val score: StateFlow<Int>
    val gameWon: StateFlow<Boolean>
    val guessedCountries: StateFlow<List<CountryUiModel>>
    val searchResults: StateFlow<List<Pair<String, String?>>>
    val popupState: StateFlow<PopupState>
    val errorMessage: StateFlow<String?>

    fun setGamemode(gamemode: String)
    fun searchCountries(searchQuery: String)
    fun guessCountry(country: String)
    fun resetGameState()
    fun resetPopupState()

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
    private val _popupState = MutableStateFlow(PopupState.NONE)
    override val popupState: StateFlow<PopupState>
        get() = _popupState
    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?>
        get() = _errorMessage

    private var isFetching: Boolean = false



    init {
        fetchCountry()
    }

    private fun fetchCountry() {
        startTimerUntilLoadingPopup(3000)
        viewModelScope.launch {
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

    private fun startTimerUntilLoadingPopup(timeMillis: Long) {
        isFetching = true
        viewModelScope.launch {
            delay(timeMillis)
            while (isFetching) {
                _popupState.value = PopupState.LOADING
            }
            _popupState.value = PopupState.NONE //This together with the while() prevents race conditions
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
            val countries = countryRepository.getAllCountrySearchResults()
            val guessedCountries = _guessedCountries.value.map { it.countryName.lowercase() }
            val result = countries.filter {
                it.first.contains(searchQuery, ignoreCase = true) &&
                    !guessedCountries.contains(it.first.lowercase())
            }
            val sortedResult = result.sortedWith(
                compareBy<Pair<String, String?>> { !it.first.startsWith(searchQuery, ignoreCase = true) }
                    .thenBy { it.first.lowercase() }
            )
            _searchResults.value = sortedResult.subList(0, 10.coerceAtMost(sortedResult.size))
        }
    }

    override fun guessCountry(country: String) {
        if (targetCountry.value == null) {
            _errorMessage.value = "Try again later"
            _popupState.value = PopupState.ERROR
            return
        }
        val guessedCountries = _guessedCountries.value.map { it.countryName.lowercase() }
        if (guessedCountries.contains(country.lowercase())) {
            _popupState.value = PopupState.DUPLICATE_SEARCH
            return
        }
        startTimerUntilLoadingPopup(5000)
        viewModelScope.launch {
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

                _guessedCountries.value = listOf(
                    result.toUiModel(
                        comp?.populationComparison,
                        comp?.areaComparison,
                        comp?.inceptionYearComparison
                    )
                ) + _guessedCountries.value
                if (_gamemode.value == "daily") {
                    _score.value = 12 - 2 * _guessedCountries.value.size
                }

                Log.d("GameVM", "Guessed country: ${_guessedCountries.value}")
                _popupState.value = PopupState.NONE
            } else {
                _popupState.value = PopupState.NO_RESULT
                Log.e("GameVM", "No country found with name $country")
            }
            isFetching = false
        }
    }

    override fun resetGameState() {
        _guessedCountries.value = listOf()
        _searchResults.value = listOf()
        _gameWon.value = false
    }

    override fun resetPopupState() {
        _errorMessage.value = null
        _popupState.value = PopupState.NONE
    }

    override fun getAnswer(): String { // TODO: for debug purposes, remove later
        return targetCountry.value?.countryName.toString()
    }
}


enum class PopupState {
    NONE,
    LOADING,
    NO_RESULT,
    ERROR,
    DUPLICATE_SEARCH
}