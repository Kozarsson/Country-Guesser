package org.kth.countryguesser.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kth.countryguesser.data.repository.FirestoreRepository
import org.kth.countryguesser.model.entity.UserProfileEntity
import org.kth.countryguesser.model.repository.CountryRepository
import org.kth.countryguesser.model.repository.GameRepository
import org.kth.countryguesser.util.PopupState
import javax.inject.Inject

interface ProfileStatsVM {
    val nickname: StateFlow<String>
    val gamesPlayed: StateFlow<Int>

    // DAILY mode
    val currentStreakDaily: StateFlow<Int>
    val bestStreakDaily: StateFlow<Int>
    val lastGuessedDaily: StateFlow<String>

    // ENDLESS mode
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

    private val _gamesPlayed = MutableStateFlow(0)
    override val gamesPlayed: StateFlow<Int>
        get() = _gamesPlayed

    // DAILY mode
    private val _currentStreakDaily = MutableStateFlow(0)
    override val currentStreakDaily: StateFlow<Int>
        get() = _currentStreakDaily

    private val _bestStreakDaily = MutableStateFlow(0)
    override val bestStreakDaily: StateFlow<Int>
        get() = _bestStreakDaily

    private val _lastGuessedDaily = MutableStateFlow("")
    override val lastGuessedDaily: StateFlow<String>
        get() = _lastGuessedDaily

    // ENDLESS mode
    private val _currentStreakEndless = MutableStateFlow(0)
    override val currentStreakEndless: StateFlow<Int>
        get() = _currentStreakEndless
    private val _bestStreakEndless = MutableStateFlow(0)
    override val bestStreakEndless: StateFlow<Int>
        get() = _bestStreakEndless


    fun refreshStats() { // used in home screen
        viewModelScope.launch {
            val profile = firestoreRepository.getUserProfile()
            _lastGuessedDaily.value = profile?.stats?.lastGuessedDaily ?: ""
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
                    _gamesPlayed.value = playerInfo.stats.gamesPlayed
                    // DAILY mode
                    _currentStreakDaily.value = playerInfo.stats.currentStreakDaily
                    _bestStreakDaily.value = playerInfo.stats.bestStreakDaily
                    _lastGuessedDaily.value = playerInfo.stats.lastGuessedDaily
                    // ENDLESS mode
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