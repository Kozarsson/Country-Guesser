package org.kth.countryguesser.model.repository

import android.util.Log
import org.kth.countryguesser.model.CountryModel
import org.kth.countryguesser.model.CountryModelImpl
import org.kth.countryguesser.model.api.RestCountriesEndpoints
import org.kth.countryguesser.model.api.WikiDataEndpoints
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.ui.model.toUiModel
import org.kth.countryguesser.util.WikiDataParser
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

interface CountryRepository {
    suspend fun searchCountries(searchQuery: String): List<CountryUiModel>
    suspend fun getCountryByName(name: String): CountryModel?
}

@Singleton
class CountryRepositoryImpl @Inject constructor(
    private val restCountriesApiService: RestCountriesEndpoints,
    private val wikiDataApiService: WikiDataEndpoints,
    private val wikiDataParser: WikiDataParser
) : CountryRepository {
    override suspend fun searchCountries(searchQuery: String): List<CountryUiModel> {
        return try {
            val result = restCountriesApiService.searchCountries(searchQuery)
            result.map { it.toUiModel() }
        } catch (e: HttpException) {
            Log.e("CountryRepository", "HTTP exception: ${e.message}")
            if (e.code() == 404) emptyList() else throw e
        }
    }

    override suspend fun getCountryByName(name: String): CountryModel? {
        val restCountriesResult = try {
            restCountriesApiService.searchCountries(name).firstOrNull()
        } catch (e: HttpException) {
            Log.e("CountryRepository", "HTTP exception: ${e.message}")
            if (e.code() == 404) return null else throw e
        }
        if (restCountriesResult == null) {
            Log.e("CountryRepository", "No country found with name $name")
            return null
        }
        try {
            val countryIdResult = wikiDataApiService.wikiDataCountryIdByName(search = name)
            val entityId = countryIdResult.search.firstOrNull()?.id ?: return null
            val entityResult = wikiDataApiService.wikiDataEntityById(entityId)
            val inceptionYearResult = entityResult.entities[entityId]?.claims?.inception?.firstOrNull()?.mainsnak?.datavalue?.value?.time
            val inceptionYear = inceptionYearResult?.let { wikiDataParser.extractYearFromWikiData(it) }
            return CountryModelImpl(
                countryName = restCountriesResult.name?.common ?: name,
                population = restCountriesResult.population,
                area = restCountriesResult.area,
                inceptionYear = inceptionYear
            )
        } catch (e: HttpException) {
            Log.e("CountryRepository", "WikiData HTTP exception: ${e.message}")
            if (e.code() == 404 || e.code() == 403) return null else throw e
        }
    }
}
