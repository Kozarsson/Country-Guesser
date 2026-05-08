package org.kth.countryguesser.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.kth.countryguesser.Application
import org.kth.countryguesser.data.repository.FirestoreRepository
import org.kth.countryguesser.model.CountryModel
import org.kth.countryguesser.model.repository.CountryRepository
import org.kth.countryguesser.model.repository.GameRepository
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.ui.model.toUiModel
import org.kth.countryguesser.util.NetworkUtils
import org.kth.countryguesser.util.PopupState
import org.kth.countryguesser.util.getCurrentDateFromFirebase
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
    private val firestoreRepository: FirestoreRepository,
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
    private var timerJob: Job? = null


    init {
        fetchCountry()
    }

    private fun fetchCountry() {
        //TODO: Refetch after internet establishment
        viewModelScope.launch {
            if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
            } else {
                startTimerUntilLoadingPopup(1000)
                var countryName: String
                val countries = countryRepository.getAllCountrySearchResults()

                if (_gamemode.value == "daily") {
                    //val seed = LocalDate.now(ZoneOffset.UTC).toEpochDay()
                    val seed = getCurrentDateFromFirebase().toEpochDay()
                    countryName = countries.random(Random(seed)).first
                } else {
                    countryName = countries.random().first //TODO: Make actually random
                }

                val result = countryRepository.getCountryByName(countryName)
                targetCountry.value = result
                isFetching = false
                setPopupState(PopupState.NONE)
            }
        }
    }

    private fun startTimerUntilLoadingPopup(timeMillis: Long) {
        timerJob?.cancel()
        isFetching = true
        timerJob = viewModelScope.launch {
            delay(timeMillis)
            while (isFetching) {
                setPopupState(PopupState.LOADING)
                delay(100)
            }
        }
    }

    override fun setGamemode(gamemode: String) {
        _gamemode.value = gamemode

        viewModelScope.launch {
            if (gamemode == "daily") {
                _score.value = 12
            } else {
                val profile = firestoreRepository.getUserProfile()
                _score.value = profile?.stats?.currentStreakEndless ?: 0
            }
        }
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
                startTimerUntilLoadingPopup(2000)
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
                            comp?.inceptionYearComparison,
                            comp?.continentsComparison,
                            comp?.bordersComparison
                        )
                    ) + _guessedCountries.value
                    if (_gamemode.value == "daily" && !_gameWon.value) {
                        _score.value = 12 - 2 * _guessedCountries.value.size
                    }

                    Log.d("GameVM", "Guessed country: ${_guessedCountries.value}")
                    setPopupState(PopupState.NONE)
                } else {
                    setPopupState(PopupState.NO_RESULT)
                    Log.e("GameVM", "No country found with name $country")
                }
                isFetching = false
                setPopupState(PopupState.NONE)
            }
        }

        // TODO: run check if user has guessed too many times
    }
    override fun resetGameState() {
        _guessedCountries.value = listOf()
        _searchResults.value = listOf()
        _gameWon.value = false
        viewModelScope.launch {
            if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
            } else {
                if (!firestoreRepository.updateStreak(_gamemode.value)) Log.e("GameVM", "Failed to update streak in Firestore")
                if(!firestoreRepository.updateGamesPlayed(_gamemode.value)) Log.e("GameVM", "Failed to update games played in Firestore")
                if (_gamemode.value == "daily") {
                    Log.d("GameVM", "Updating last daily guess in Firestore with country: ${targetCountry.value?.countryName}")
                    if(!firestoreRepository.updateLastDailyGuess(
                        targetCountry.value?.countryName ?: "",
                        targetCountry.value?.flagUrl ?: ""
                    )) Log.e("GameVM", "Failed to update daily guess in Firestore")
                    if(!firestoreRepository.updateScore(_score.value)) Log.e("GameVM", "Failed to update score in Firestore")// TODO: score is not updating correctly in database
                }
            }
        }
    }

    override fun getAnswer(): String { // TODO: for debug purposes, remove later
        return targetCountry.value?.countryName.toString()
    }
}