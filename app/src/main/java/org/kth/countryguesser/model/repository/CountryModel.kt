package org.kth.countryguesser.model.repository

import org.kth.countryguesser.model.api.RestCountriesEndpoints
import org.kth.countryguesser.model.api.WikiDataEndpoints
import org.kth.countryguesser.model.repository.CountryRepository


interface CountryModel {
    val countryName: String
    val population: Long?
    val area: Double?
    val inceptionYear: InceptionYear?
    
//    fun compareTo(other: CountryModel): CountryComparisonResult
    suspend fun saveToDatabase()
}

class CountryModelImpl private constructor(
    override var countryName: String = "",
    override var population: Long? = null,
    override var area: Double? = null,
    override var inceptionYear: InceptionYear? = null,
    private val restCountriesApiService: RestCountriesEndpoints,
    private val wikiDataApiService: WikiDataEndpoints
) : CountryModel {
//    override fun compareTo(other: CountryModel): CountryComparisonResult {
//        return CountryComparisonResult(
//            populationComparison = compareValuesBy(this.country, other.country) { it.population },
//            areaComparison = compareValuesBy(this.country, other.country) { it.area?.toLong() },
//        )
//    }

    override suspend fun saveToDatabase() {
        // TODO: Implement save logic
    }

    companion object {
        suspend fun create(
            name: String,
            restCountriesApiService: RestCountriesEndpoints,
            wikiDataApiService: WikiDataEndpoints
        ): CountryModel {
            val restCountriesResult = restCountriesApiService.searchCountries(name).firstOrNull()
                ?: throw IllegalArgumentException("Country not found")
            val countryIdResult = wikiDataApiService.wikiDataCountryIdByName(search = name)
            val entityId = countryIdResult.search.firstOrNull()?.id ?: throw IllegalArgumentException("Entity ID not found")
            val entityResult = wikiDataApiService.wikiDataEntityById(entityId)
                ?: throw IllegalArgumentException("Entity ID not found")
            val inceptionYearResult = entityResult.entities[entityId]?.claims?.inception?.firstOrNull()?.mainsnak?.datavalue?.value?.time
            return CountryModelImpl(
                countryName = restCountriesResult.name?.common!!,
                population = restCountriesResult.population,
                area = restCountriesResult.area,
                inceptionYear = CountryRepository().extractYearFromWikiData(inceptionYearResult!!),
                restCountriesApiService = restCountriesApiService,
                wikiDataApiService = wikiDataApiService
            )
        }
    }
}

//data class CountryComparisonResult(
//    val populationComparison: Int,
//    val areaComparison: Int,
//)
