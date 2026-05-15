package org.kth.countryguesser.util

enum class PopupState {
    NONE,
    LOADING,
    ERROR,
    NO_INTERNET,
}

enum class GamePopupState {
    NONE,
    NO_RESULT,
    DUPLICATE_SEARCH,
    GAME_WON_DAILY,
    GAME_WON_ENDLESS,
    CONFIRM_QUIT
}