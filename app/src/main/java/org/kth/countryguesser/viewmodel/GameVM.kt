package org.kth.countryguesser.viewmodel

import android.util.Log
import androidx.compose.ui.geometry.Offset
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
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.ui.model.toUiModel
import org.kth.countryguesser.util.NetworkUtils
import org.kth.countryguesser.util.PopupState
import org.kth.countryguesser.util.GamePopupState
import org.kth.countryguesser.util.GameSessionState
import org.kth.countryguesser.util.getCurrentDateFromFirebase
import javax.inject.Inject
import kotlin.random.Random


interface GameVM {
    val score: StateFlow<Int>
    val gameWon: StateFlow<Boolean>
    val guessedCountries: StateFlow<List<CountryUiModel>>
    val searchResults: StateFlow<List<Pair<String, String?>>>
    val mapZoom: StateFlow<Float>
    val mapPan: StateFlow<Offset>

    fun setGamemode(gamemode: String)
    fun searchCountries(searchQuery: String)
    fun guessCountry(country: String)
    fun resetGameState()
    fun saveToFirestore()

    fun setMapParam(zoom: Float, pan: Offset)

    fun getTargetCountryName(): String
    fun getTargetCountryFlagUrl(): String?

    fun onGameOver()
}

@HiltViewModel
class GameVMImpl @Inject constructor(
    private val countryRepository: CountryRepository,
    private val firestoreRepository: FirestoreRepository,
    private val gameSessionState: GameSessionState
) : BaseVM(), GameVM {
    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score
    private val _gameWon = MutableStateFlow(false)
    override val gameWon: StateFlow<Boolean>
        get() = _gameWon
    private val targetCountry = MutableStateFlow<CountryModel?>(null)
    private val _gamemode = MutableStateFlow<String>("daily")

    private val _mapZoom = MutableStateFlow(-1f)
    override val mapZoom: StateFlow<Float>
        get() = _mapZoom

    private val _mapPan = MutableStateFlow(Offset.Zero)
    override val mapPan: StateFlow<Offset>
        get() = _mapPan


    private val _gamePopupState = MutableStateFlow(GamePopupState.NONE)
    val gamePopupState: StateFlow<GamePopupState>
        get() = _gamePopupState

    internal fun setGamePopupState(state: GamePopupState) {
        _gamePopupState.value = state
    }

    fun resetGamePopupState() {
        _gamePopupState.value = GamePopupState.NONE
    }

    init {
        fetchCountry()
    }

    private fun fetchCountry() {
        viewModelScope.launch {
            while (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
                delay(1000)
            }
                startTimerUntilLoadingPopup(1000)
                var countryName: String
                val countries = countryRepository.getAllCountrySearchResults()

                if(countries.isEmpty()) {
                    isFetching = false
                    setError("Error connecting to the servers, try again later")
                    gameSessionState.setInGame(false)
                    return@launch
                }

                if (_gamemode.value == "daily") {
                    val seed = getCurrentDateFromFirebase().toEpochDay()
                    countryName = countries.random(Random(seed)).first
                } else {
                    countryName = countries.random().first
                }

                val result = countryRepository.getCountryByName(countryName)
                targetCountry.value = result
                isFetching = false
                setPopupState(PopupState.NONE)
        }
    }

    override fun setGamemode(gamemode: String) {
        _gamemode.value = gamemode

        viewModelScope.launch {
            if (gamemode == "daily" && firestoreRepository.getCurrentUser() == null &&
                firestoreRepository.isLocalDailyDoneToday()
            ) {
                setGamePopupState(GamePopupState.GAME_OVER)
                gameSessionState.setInGame(false)
                return@launch
            }

            if (gamemode == "endless") {
                fetchCountry()
            }

            if (gamemode == "daily") {
                _score.value = 10

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
        viewModelScope.launch {
            if (_gamemode.value == "daily" && firestoreRepository.getCurrentUser() == null &&
                firestoreRepository.isLocalDailyDoneToday()
            ) {
                setGamePopupState(GamePopupState.GAME_OVER)
                gameSessionState.setInGame(false)
                return@launch
            }

            if (targetCountry.value == null) {
                setError("Try again later")
                return@launch
            }
            val guessedCountries = _guessedCountries.value.map { it.countryName.lowercase() }
            if (guessedCountries.contains(country.lowercase())) {
                setGamePopupState(GamePopupState.DUPLICATE_SEARCH)
                return@launch
            }
            if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
            } else {
                startTimerUntilLoadingPopup(2000)
                val result = countryRepository.getCountryByName(country)
                if (result != null) {
                    val comp = targetCountry.value?.compareAttributesTo(result, 0.10)

                    if (targetCountry.value?.countryName == result.countryName) {
                        _gameWon.value = true
                        gameSessionState.setInGame(false)
                        viewModelScope.launch {
                            delay(1500)
                            if (_gamemode.value == "daily") {
                                saveToFirestore()
                                setGamePopupState(GamePopupState.GAME_WON_DAILY)
                            } else {
                                setGamePopupState(GamePopupState.GAME_WON_ENDLESS)
                                _score.value++
                            }

                        }
                        Log.d("GameVM", "Correct guess")
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
                        _score.value = 10 - _guessedCountries.value.size
                    }

                    Log.d("GameVM", "Guessed country: ${_guessedCountries.value}")
                    setPopupState(PopupState.NONE)
                } else {
                    setGamePopupState(GamePopupState.NO_RESULT)
                    Log.e("GameVM", "No country found with name $country")
                }
                isFetching = false
                setPopupState(PopupState.NONE)
            }

            // check if user has guessed too many times
            if (_guessedCountries.value.size >= 10 && _gameWon.value == false) {
                setGamePopupState(GamePopupState.GAME_OVER)
                onGameOver()
            }
        }
    }
    override fun resetGameState() {
        _guessedCountries.value = listOf()
        _searchResults.value = listOf()
        _gameWon.value = false
        gameSessionState.setInGame(false)
        if (_gamemode.value == "endless") {
            fetchCountry()
        }
        resetGamePopupState()

        _mapZoom.value = -1f
        _mapPan.value = Offset.Zero
    }

    override fun saveToFirestore() {
        viewModelScope.launch {
            if (!NetworkUtils.isNetworkAvailable(Application.APPLICATION.applicationContext)) {
                setPopupState(PopupState.NO_INTERNET)
            } else {
                if (_gamemode.value == "daily") {
                    if(!firestoreRepository.updateLastDailyGuess(
                            targetCountry.value?.countryName ?: "",
                            targetCountry.value?.flagUrl ?: "",
                            _score.value
                        )) Log.e("GameVM", "Failed to update daily stats in Firestore")
                } else {
                    if(!firestoreRepository.updateStreak(_gamemode.value)) Log.e("GameVM", "Failed to update streak in Firestore")
                    if(!firestoreRepository.updateGamesPlayed(_gamemode.value)) Log.e("GameVM", "Failed to update games played in Firestore")
                    if(!firestoreRepository.updateScore(_score.value)) Log.e("GameVM", "Failed to update score in Firestore") // TODO: score is not updating correctly in database
                }
            }
        }
    }

    override fun setMapParam(zoom: Float, pan: Offset) {
        _mapZoom.value = zoom
        _mapPan.value = pan
    }
    override fun getTargetCountryName(): String {
        return targetCountry.value?.countryName.toString()
    }

    override fun getTargetCountryFlagUrl(): String? {
        return targetCountry.value?.flagUrl
    }

    override fun onGameOver() {
        viewModelScope.launch {
            if (_guessedCountries.value.isNotEmpty()) {
                firestoreRepository.resetStreak(_gamemode.value)
            }
            gameSessionState.setInGame(false)
        }
    }

    fun setInGameActive(active: Boolean) {
        gameSessionState.setInGame(active)
    }
}

