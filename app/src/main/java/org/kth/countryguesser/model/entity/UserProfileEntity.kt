package org.kth.countryguesser.model.entity

data class UserProfileEntity(
    val uid: String,
    val nickname: String = "",
    val stats: UserStatsEntity = UserStatsEntity(),
    val settings: UserSettingsEntity = UserSettingsEntity()
)

data class UserStatsEntity(
    val gamesPlayed: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
)

data class UserSettingsEntity(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
)

