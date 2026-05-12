package org.kth.countryguesser.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kth.countryguesser.data.repository.FirestoreRepository
import org.kth.countryguesser.model.repository.CountryRepository
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.ui.model.PlayerUiModel
import org.kth.countryguesser.util.PopupState
import javax.inject.Inject

interface LeaderboardVM {
    val players: StateFlow<List<PlayerUiModel>>
}

@HiltViewModel
class LeaderboardVMImpl @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
) : BaseVM(), LeaderboardVM {

    private val _players = MutableStateFlow<List<PlayerUiModel>>(listOf())
    override val players: StateFlow<List<PlayerUiModel>>
        get() = _players


    init {
        setPopupState(PopupState.LOADING)
        viewModelScope.launch {
            try {
                val players = firestoreRepository.getLeaderboard()
                if (players != null) {
                    _players.value = players.map { PlayerUiModel(it.nickname, it.gamesPlayedDaily, it.currentStreakDaily, it.bestStreakDaily) }
                } else {
                    //setPopupState(PopupState.ERROR)
                }
                setPopupState(PopupState.NONE)
            } catch (e: Exception) {
                setError("Failed to fetch leaderboard")
            }
        }
    }
}