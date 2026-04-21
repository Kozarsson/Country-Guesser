package org.kth.countryguesser

import org.junit.Assert.*
import org.junit.Test
import org.kth.countryguesser.model.api.RestCountriesEndpoints
import org.kth.countryguesser.model.service.RestCountriesApiService
import kotlinx.coroutines.runBlocking
import org.kth.countryguesser.model.api.WikiDataEndpoints
import org.kth.countryguesser.model.dto.CountryResultDto
import org.kth.countryguesser.model.service.WikiDataApiService

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ApiUnitTest {
    @Test
    fun testAllCountrySearchResultsAreValid() = runBlocking {
        val restCountriesApiService: RestCountriesEndpoints = RestCountriesApiService.api

        val result = restCountriesApiService.getAllCountriesTest()

        assertTrue("Expected non-empty country list", result.isNotEmpty())
        for (country: CountryResultDto in result) {
            for (field: String in listOf("name", "population", "area", "flags", "continents")) {
                val value = when (field) {
                    "name" -> country.name?.common
                    "population" -> country.population
                    "area" -> country.area
                    "flags" -> country.flags?.png
                    "continents" -> country.continents?.firstOrNull()
                    else -> null
                }
                assertNotNull("Expected non-null value for field '$field' in country '${country.name?.common}'", value)
            }
        }
    }

    @Test
    //Currently doesn't work due to WikiData enforcing a limit of requests, see more at https://phabricator.wikimedia.org/T400119
    fun testAllWikiDataResultsAreValid() = runBlocking {
        val restCountriesApiService: RestCountriesEndpoints = RestCountriesApiService.api
        val wikiDataApiService: WikiDataEndpoints = WikiDataApiService.api

        val restCountriesResult = restCountriesApiService.getAllCountries()
        val countryNames = restCountriesResult.map { it.name?.common }
        for (name: String? in countryNames) {
            assertNotNull("Expected non-null value for country name", name)
            val countryIdResult = wikiDataApiService.wikiDataCountryIdByName(search = name!!)
            val entityId = countryIdResult.search.firstOrNull()?.id
            assertNotNull("Expected non-null value for entity ID with country name '$name", entityId)
            val entityResult = wikiDataApiService.wikiDataEntityById(entityId!!)
            val inceptionYearResult = entityResult.entities[entityId]?.claims?.inception?.firstOrNull()?.mainsnak?.datavalue
            assertNotNull("Expected non-null value for inception year with country name '$name'", inceptionYearResult)
            val inceptionYear = inceptionYearResult?.value?.time
            assertNotNull("Expected non-null value for inception year with country name '$name'", inceptionYear)
        }
    }
}