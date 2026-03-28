package org.kth.countryguesser.model.repository

import org.kth.countryguesser.model.service.RestCountriesApiService
import org.kth.countryguesser.model.service.WikiDataApiService

interface GameModel {
    val gamemode: String
    val country: CountryModel?
    val numClues: Int
    val clues: List<String>
    val score: Int

    suspend fun checkGuess(guess: String) : Boolean

    suspend fun fetchCountry() : CountryModel
}

class GameModelImpl private constructor(
    override var gamemode: String = "daily",
    override var country: CountryModel? = null,
    override var numClues: Int = 0,
    override var clues: List<String>,
    override var score: Int = if (gamemode == "daily") 10 else 0,
) : GameModel {
    override suspend fun checkGuess(guess: String) : Boolean {
        if (country?.countryName.equals(guess, ignoreCase = true)) { // CORRECT GUESS
            if (gamemode == "daily") {
                // TODO: save score
                // TODO: end game
            } else {
                score++
                country = fetchCountry()
            }
            return true
        }
        // WRONG GUESS
        if (numClues >= 5) {
            // TODO: end game
        }
        numClues++
        if (gamemode == "daily") score = 10 - 2*numClues
        return false
    }

    override suspend fun fetchCountry() : CountryModel {
        val restApi = RestCountriesApiService.api
        val wikiApi = WikiDataApiService.api
        var newCountry: String
        newCountry = "Sweden" // (debug) TODO: remove

        if (gamemode == "daily") {
            // TODO: determine the daily country
        } else {
            // TODO: get new country
        }

        return CountryModelImpl.create(newCountry, restApi, wikiApi)
    }
}