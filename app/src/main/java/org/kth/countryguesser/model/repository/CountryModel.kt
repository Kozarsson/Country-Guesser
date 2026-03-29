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
    fun compareAttributesTo(other: CountryModel, closenessCriteria: Double?): CountryComparisonResult
    fun compareAttributesTo(other: CountryModel): CountryComparisonResult
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
    override fun compareAttributesTo(other: CountryModel, closenessCriteria: Double?): CountryComparisonResult {
        return CountryComparisonResult(
            populationComparison = compareAttribute(this.population, other.population, closenessCriteria),
            areaComparison = compareAttribute(this.area, other.area, closenessCriteria),
            inceptionYearComparison = compareAttribute<InceptionYear>(this.inceptionYear, other.inceptionYear, closenessCriteria)
        )
    }

    override fun compareAttributesTo(other: CountryModel): CountryComparisonResult {
        return compareAttributesTo(other, closenessCriteria = null)
    }

    override suspend fun saveToDatabase() {
        // TODO: Implement save logic
    }
    
    private fun <T : Comparable<T>> compareAttribute(value1: T?, value2: T?, closenessCriteria: Double?): CountryAttributeResult {
        if (value1 == null || value2 == null) {
            return CountryAttributeResult(comparison = null, isClose = null)
        }
        val comparison = value1.compareTo(value2)
        if (closenessCriteria == null) {
            return CountryAttributeResult(comparison = comparison, isClose = null)
        }
        var isClose: Boolean? = null
        if (value1 is Number && value2 is Number) {
            val diff = kotlin.math.abs(value1.toDouble() - value2.toDouble())
            isClose = diff < closenessCriteria * value1.toDouble()
        } else if (value1 is InceptionYear && value2 is InceptionYear) {
            // Normalise BC/AD: BC years as negative, AD as positive
            val year1 = if (value1.datingSystem == "BC") -value1.year else value1.year
            val year2 = if (value2.datingSystem == "BC") -value2.year else value2.year
            val diff = kotlin.math.abs(year1 - year2)
            isClose = diff < closenessCriteria * kotlin.math.abs(year1)
        }
        return CountryAttributeResult(comparison = comparison, isClose = isClose)
    }

    companion object {
        suspend fun create(
            name: String,
            restCountriesApiService: RestCountriesEndpoints,
            wikiDataApiService: WikiDataEndpoints
        ): CountryModel {
            val restCountriesResult = restCountriesApiService.searchCountries(name).firstOrNull() ?: throw Exception("Country not found")
                ?: throw IllegalArgumentException("Country not found")
//            val countryIdResult = wikiDataApiService.wikiDataCountryIdByName(search = name)
//            val entityId = countryIdResult.search.firstOrNull()?.id ?: throw IllegalArgumentException("Entity ID not found")
//            val entityResult = wikiDataApiService.wikiDataEntityById(entityId)
//            val inceptionYearResult = entityResult.entities[entityId]?.claims?.inception?.firstOrNull()?.mainsnak?.datavalue?.value?.time
            return CountryModelImpl(
                countryName = restCountriesResult.name?.common!!,
                population = restCountriesResult.population,
                area = restCountriesResult.area,
//                inceptionYear = CountryRepository().extractYearFromWikiData(inceptionYearResult!!),
                restCountriesApiService = restCountriesApiService,
                wikiDataApiService = wikiDataApiService
            )
        }
    }
}

data class CountryComparisonResult(
    val populationComparison: CountryAttributeResult,
    val areaComparison: CountryAttributeResult,
    val inceptionYearComparison: CountryAttributeResult,
)

/**
 * Represents the result of comparing a single country attribute between two countries.
 *
 * @property comparison The result of the comparison: -1 if the first value is less than the second,
 * 0 if they are equal, 1 if the first is greater, or null if either value is null.
 * @property isClose Indicates whether the two values are considered "close" according to a given closeness criteria.
 */
data class CountryAttributeResult(
    val comparison: Int?, // -1, 0, 1, or null
    val isClose: Boolean?
)
