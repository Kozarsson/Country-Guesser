package org.kth.countryguesser

import org.junit.Assert.*
import org.junit.Test
import org.kth.countryguesser.model.api.RestCountriesEndpoints
import org.kth.countryguesser.model.service.RestCountriesApiService
import kotlinx.coroutines.runBlocking
import org.kth.countryguesser.model.dto.CountryResultDto

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
}