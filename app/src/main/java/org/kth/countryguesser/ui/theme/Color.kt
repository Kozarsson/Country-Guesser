package org.kth.countryguesser.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val GuessGreen = Color(0xFF4CAF50)
val GuessRed = Color(0xFFF44336)
val GuessOrange = Color(0xFFFF9800)
val GuessGrey = Color(0xFF757575)

val MapBlue = Color(0xff70d6ef)

val LeaderBoardGold = Color(0xFFFFD54F)
val LeaderBoardSilver = Color(0xFFCFD8DC)
val LeaderBoardBronze = Color(0xFFD7A86E)

// Named palette for game-specific colors.
data class AppColors(
    val guessGreen: Color = GuessGreen,
    val guessRed: Color = GuessRed,
    val guessOrange: Color = GuessOrange,
    val guessGrey: Color = GuessGrey,
    val mapBlue: Color = MapBlue,

    val leaderBoardGold: Color = LeaderBoardGold,
    val leaderBoardSilver: Color = LeaderBoardSilver,
    val leaderBoardBronze: Color = LeaderBoardBronze,
)

val LocalAppColors = staticCompositionLocalOf { AppColors() }
