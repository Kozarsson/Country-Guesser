package org.kth.countryguesser.data.remote.entity

data class UserProfileEntity(
    val uid: String,
    val nickname: String = "",
    val stats: UserStatsEntity = UserStatsEntity(),
    val settings: UserSettingsEntity = UserSettingsEntity(),
    val lastGuessedDaily: LastGuessedDailyEntity = LastGuessedDailyEntity()
)

data class UserStatsEntity(
    val gamesPlayedDaily: Int = 0,
    val currentStreakDaily: Int = 0,
    val bestStreakDaily: Int = 0,
    val totalScore: Int = 0,
    val gamesPlayedEndless: Int = 0,
    val currentStreakEndless: Int = 0,
    val bestStreakEndless: Int = 0,
)

data class UserSettingsEntity(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
)

data class LastGuessedDailyEntity(
    val date: String = "",
    val countryName: String = "",
    val flagUrl: String = ""
)

