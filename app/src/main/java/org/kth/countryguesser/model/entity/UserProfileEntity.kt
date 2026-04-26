package org.kth.countryguesser.model.entity

import java.time.LocalDate

data class UserProfileEntity(
    val uid: String,
    val nickname: String = "",
    val stats: UserStatsEntity = UserStatsEntity(),
    val settings: UserSettingsEntity = UserSettingsEntity()
)

data class UserStatsEntity(
    val gamesPlayedDaily: Int = 0,
    val currentStreakDaily: Int = 0,
    val bestStreakDaily: Int = 0,
    val lastGuessedDaily: String = "",
    val totalScore: Int = 0,
    val gamesPlayedEndless: Int = 0,
    val currentStreakEndless: Int = 0,
    val bestStreakEndless: Int = 0,
)

data class UserSettingsEntity(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
)

