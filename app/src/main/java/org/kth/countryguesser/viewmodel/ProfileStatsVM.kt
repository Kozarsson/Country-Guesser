package org.kth.countryguesser.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kth.countryguesser.data.repository.FirestoreRepository
import org.kth.countryguesser.model.entity.UserProfileEntity
import org.kth.countryguesser.util.PopupState
import javax.inject.Inject

interface ProfileStatsVM {
    val nickname: StateFlow<String>

    // DAILY mode
    val gamesPlayedDaily: StateFlow<Int>
    val currentStreakDaily: StateFlow<Int>
    val bestStreakDaily: StateFlow<Int>
    val lastGuessedDaily: StateFlow<LastGuessedDaily>
    val totalScore: StateFlow<Int>

    // ENDLESS mode
    val gamesPlayedEndless: StateFlow<Int>
    val currentStreakEndless: StateFlow<Int>
    val bestStreakEndless: StateFlow<Int>
}

@HiltViewModel
class ProfileStatsVMImpl @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
) : BaseVM(), ProfileStatsVM {

    private val _nickname = MutableStateFlow<String>("")
    override val nickname: StateFlow<String>
        get() = _nickname

    // DAILY mode
    private val _gamesPlayedDaily = MutableStateFlow(0)
    override val gamesPlayedDaily: StateFlow<Int>
        get() = _gamesPlayedDaily

    private val _currentStreakDaily = MutableStateFlow(0)
    override val currentStreakDaily: StateFlow<Int>
        get() = _currentStreakDaily

    private val _bestStreakDaily = MutableStateFlow(0)
    override val bestStreakDaily: StateFlow<Int>
        get() = _bestStreakDaily

    private val _lastGuessedDaily = MutableStateFlow<LastGuessedDaily>(LastGuessedDaily(null, null, null))
    override val lastGuessedDaily: StateFlow<LastGuessedDaily>
        get() = _lastGuessedDaily

    private val _totalScore = MutableStateFlow(0)
    override val totalScore: StateFlow<Int>
        get() = _totalScore

    // ENDLESS mode
    private val _gamesPlayedEndless = MutableStateFlow(0)
    override val gamesPlayedEndless: StateFlow<Int>
        get() = _gamesPlayedEndless

    private val _currentStreakEndless = MutableStateFlow(0)
    override val currentStreakEndless: StateFlow<Int>
        get() = _currentStreakEndless
    private val _bestStreakEndless = MutableStateFlow(0)
    override val bestStreakEndless: StateFlow<Int>
        get() = _bestStreakEndless


    fun refreshStats() { // used in home screen
        viewModelScope.launch {
            val profile = firestoreRepository.getUserProfile()
            //_lastGuessedDaily.value = profile?.stats?.lastGuessedDaily ?: ""
            _lastGuessedDaily.value = LastGuessedDaily(
                date = profile?.lastGuessedDaily?.date,
                countryName = profile?.lastGuessedDaily?.countryName,
                flagUrl = profile?.lastGuessedDaily?.flagUrl
            )
            _currentStreakDaily.value = profile?.stats?.currentStreakDaily ?: 0
            _currentStreakEndless.value = profile?.stats?.currentStreakEndless ?: 0
            // best streak does not need to be refreshed
        }
    }

    init {
        var playerInfo: UserProfileEntity? = null
        setPopupState(PopupState.LOADING)
        viewModelScope.launch {
            try {
                val playerInfo = firestoreRepository.getUserProfile()
                if (playerInfo != null) {
                    _nickname.value = playerInfo.nickname
                    // DAILY mode
                    _gamesPlayedDaily.value = playerInfo.stats.gamesPlayedDaily
                    _currentStreakDaily.value = playerInfo.stats.currentStreakDaily
                    _bestStreakDaily.value = playerInfo.stats.bestStreakDaily
                    //_lastGuessedDaily.value = playerInfo.stats.lastGuessedDaily
                    _totalScore.value = playerInfo.stats.totalScore
                    _lastGuessedDaily.value = LastGuessedDaily(
                        date = playerInfo.lastGuessedDaily.date,
                        countryName = playerInfo.lastGuessedDaily.countryName,
                        flagUrl = playerInfo.lastGuessedDaily.flagUrl
                    )

                    // ENDLESS mode
                    _gamesPlayedEndless.value = playerInfo.stats.gamesPlayedEndless
                    _currentStreakEndless.value = playerInfo.stats.currentStreakEndless
                    _bestStreakEndless.value = playerInfo.stats.bestStreakEndless
                }
            } finally {
                if (playerInfo == null) {
                    setError("Failed to fetch user profile")
                } else {
                    setPopupState(PopupState.NONE)
                }
            }
        }
    }
}

data class LastGuessedDaily (
    val date: String?,
    val countryName: String?,
    val flagUrl: String?
)