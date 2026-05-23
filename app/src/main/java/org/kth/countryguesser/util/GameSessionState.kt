package org.kth.countryguesser.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameSessionState @Inject constructor() {
    private val _inGame = MutableStateFlow(false)
    val inGame: StateFlow<Boolean> get() = _inGame

    fun setInGame(value: Boolean) {
        _inGame.value = value
    }
}

