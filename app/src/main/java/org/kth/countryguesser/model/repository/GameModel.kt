package org.kth.countryguesser.model.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kth.countryguesser.model.service.RestCountriesApiService
import org.kth.countryguesser.model.service.WikiDataApiService

interface GameModel {
    val gamemode: String
    val country: CountryModel?
    val numClues: Int
    val score: Int

    fun getClues() : List<String>

    fun checkGuess(guess: String) : Boolean

    suspend fun fetchCountry()
}

class GameModelImpl constructor(
    override var gamemode: String = "daily",
) : GameModel {
    override var country: CountryModel? by mutableStateOf(null)
    override var numClues: Int by mutableIntStateOf(1)
    override var score: Int by mutableIntStateOf(if (gamemode == "daily") 10 else 0)

    override fun getClues() : List<String> {
        return listOf(
            country?.population.toString(),
            country?.area.toString(),
            country?.inceptionYear.toString()
        )
    }

    override fun checkGuess(guess: String) : Boolean {
        if (country?.countryName.equals(guess, ignoreCase = true)) { // CORRECT GUESS
            if (gamemode == "daily") {
                // TODO: save score
                // TODO: end game
            } else {
                score++
                numClues = 1
                country = null
                GlobalScope.launch { fetchCountry() }
            }
            return true
        }

        // WRONG GUESS
        if (numClues >= 5) {
            // TODO: end game
        }
        numClues++
        if (gamemode == "daily") {
            score = 10 - 2*(numClues-1)
        }
        return false
    }

    override suspend fun fetchCountry() {
        try {
            val restApi = RestCountriesApiService.api
            val wikiApi = WikiDataApiService.api
            var newCountry: String

            if (gamemode == "daily") {
                // TODO: determine the daily country
                newCountry = "Sweden" // (debug) TODO: remove
            } else {
                // TODO: get new country
                newCountry = "Switzerland" // (debug) TODO: remove
            }

            country = CountryModelImpl.create(newCountry, restApi, wikiApi)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}