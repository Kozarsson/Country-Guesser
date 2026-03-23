package org.kth.countryguesser.model.repository

import org.kth.countryguesser.model.api.CountriesEndpoints
import org.kth.countryguesser.model.dto.CountryResultDto
import org.kth.countryguesser.model.service.RestCountriesApiService


interface CountryModel {
    val country: CountryResultDto
    fun compareTo(other: CountryModel): CountryComparisonResult
    suspend fun saveToDatabase()
}

class CountryModelImpl private constructor(
    override val country: CountryResultDto,
    private val apiService: CountriesEndpoints = RestCountriesApiService.api
) : CountryModel {
    override fun compareTo(other: CountryModel): CountryComparisonResult {
        return CountryComparisonResult(
            populationComparison = compareValuesBy(this.country, other.country) { it.population },
            areaComparison = compareValuesBy(this.country, other.country) { it.area?.toLong() },
        )
    }

    override suspend fun saveToDatabase() {
        // TODO: Implement save logic
    }

    companion object {
        suspend fun create(
            name: String,
            apiService: CountriesEndpoints = RestCountriesApiService.api
        ): CountryModel {
            val result = apiService.searchCountries(name).firstOrNull()
                ?: throw IllegalArgumentException("Country not found")
            return CountryModelImpl(result, apiService)
        }
    }
}


data class CountryComparisonResult(
    val populationComparison: Int,
    val areaComparison: Int,
)
