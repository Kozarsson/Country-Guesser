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
    val currentStreak: StateFlow<Int>
    val bestStreak: StateFlow<Int>
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

    private val _currentStreak = MutableStateFlow(0)
    override val currentStreak: StateFlow<Int>
        get() = _currentStreak

    private val _bestStreak = MutableStateFlow(0)
    override val bestStreak: StateFlow<Int>
        get() = _bestStreak



    init {
        var playerInfo: UserProfileEntity? = null
        setPopupState(PopupState.LOADING)
        viewModelScope.launch {
            try {
                playerInfo = firestoreRepository.getUserProfile()
                if (playerInfo != null) {
                    _nickname.value = playerInfo.nickname
                    _gamesPlayed.value = playerInfo.stats.gamesPlayed
                    _currentStreak.value = playerInfo.stats.currentStreak
                    _bestStreak.value = playerInfo.stats.bestStreak
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