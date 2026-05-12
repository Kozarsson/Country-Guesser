package org.kth.countryguesser.ui.model

data class PlayerUiModel(
    val nickname: String,
    val gamesPlayedDaily: Int,
    val currentStreakDaily: Int,
    val bestStreakDaily: Int,
)