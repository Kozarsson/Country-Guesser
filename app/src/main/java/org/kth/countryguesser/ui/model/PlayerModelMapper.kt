package org.kth.countryguesser.ui.model

import org.kth.countryguesser.data.remote.entity.UserProfileEntity

/**
 * Maps a [UserProfileEntity] to a [PlayerUiModel] for UI display purposes.
 */
fun UserProfileEntity.toUiModel(): PlayerUiModel = PlayerUiModel(
    nickname = this.nickname,
    gamesPlayedDaily = this.stats.gamesPlayedDaily,
    currentStreakDaily = this.stats.currentStreakDaily,
    bestStreakDaily = this.stats.bestStreakDaily,
)