package org.kth.countryguesser.model.repository

import android.util.Log
import org.kth.countryguesser.model.CountryModel
import org.kth.countryguesser.model.CountryModelImpl
import org.kth.countryguesser.model.api.RestCountriesEndpoints
import org.kth.countryguesser.model.api.WikiDataEndpoints
import org.kth.countryguesser.ui.model.CountryUiModel
import org.kth.countryguesser.ui.model.toUiModel
import org.kth.countryguesser.util.extractYearFromWikiData
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

interface CountryRepository {
    suspend fun searchCountries(searchQuery: String): List<CountryUiModel>
    suspend fun getCountryByName(name: String): CountryModel?
    suspend fun getAllCountrySearchResults(): List<Pair<String, String?>>
}

@Singleton
class CountryRepositoryImpl @Inject constructor(
    private val restCountriesApiService: RestCountriesEndpoints,
    private val wikiDataApiService: WikiDataEndpoints,
) : CountryRepository {

    // In-memory cache
    private var cachedCountrySearchResults: List<Pair<String, String?>>? = null

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
//            restCountriesApiService.searchCountries(name)
//                .firstOrNull { country ->
//                    country.name?.common?.equals(name, ignoreCase = true) == true ||
//                        country.name?.official?.equals(name, ignoreCase = true) == true
//                }
            restCountriesApiService.searchCountry(name).firstOrNull()
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
            val inceptionYear = inceptionYearResult?.let { extractYearFromWikiData(it) }
            return CountryModelImpl(
                countryName = restCountriesResult.name?.common ?: name,
                population = restCountriesResult.population,
                area = restCountriesResult.area,
                inceptionYear = inceptionYear,
                flagUrl = restCountriesResult.flags?.png, //TODO: Fix bug that when searching for a country before the guessing country is loaded, the app crashes due to this image url
                continents = restCountriesResult.continents,
                borders = restCountriesResult.borders,
                cioc = restCountriesResult.cioc,
                cca2 = restCountriesResult.cca2,
            )
        } catch (e: HttpException) {
            Log.e("CountryRepository", "WikiData HTTP exception: ${e.message}")
            if (e.code() == 404 || e.code() == 403) return null else throw e
        }
    }

    override suspend fun getAllCountrySearchResults(): List<Pair<String, String?>> {
        // Return cached version if available
        if (cachedCountrySearchResults?.isNotEmpty() == true) {
            return cachedCountrySearchResults!!
        }

        return try {
            val result = restCountriesApiService.getAllCountries()
            val countrySearchResults = result.mapNotNull { country ->
                val name = country.name?.common ?: return@mapNotNull null
                name to country.flags?.png
            }
            cachedCountrySearchResults = countrySearchResults
            countrySearchResults
        } catch (e: Exception) {
            Log.e("CountryRepository", "Error fetching all countries: ${e.message}")
            emptyList()
        }
    }
}
