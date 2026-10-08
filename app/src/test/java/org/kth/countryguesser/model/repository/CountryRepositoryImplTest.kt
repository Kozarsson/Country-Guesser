package org.kth.countryguesser.model.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.kth.countryguesser.data.remote.dto.CountryResultDto
import org.kth.countryguesser.model.api.RestCountriesEndpoints
import org.kth.countryguesser.model.api.WikiDataEndpoints
import java.io.IOException
import android.util.Log
import io.mockk.mockkStatic
import io.mockk.every

class CountryRepositoryImplTest {

    private lateinit var restCountriesApi: RestCountriesEndpoints
    private lateinit var wikiDataApi: WikiDataEndpoints
    private lateinit var repository: CountryRepositoryImpl

    @Before
    fun setup() {
        mockkStatic(Log::class)

        every {
            Log.e(any(), any<String>())
        } returns 0

        restCountriesApi = mockk()
        wikiDataApi = mockk()

        repository = CountryRepositoryImpl(
            restCountriesApiService = restCountriesApi,
            wikiDataApiService = wikiDataApi
        )
    }

    @Test
    fun `searchCountries returns countries from API`() = runTest {
        // Arrange
        val country = mockk<CountryResultDto>(relaxed = true)

        coEvery {
            restCountriesApi.searchCountries("Sweden")
        } returns listOf(country)

        // Act
        val result = repository.searchCountries("Sweden")

        // Assert
        assertEquals(1, result.size)

        coVerify(exactly = 1) {
            restCountriesApi.searchCountries("Sweden")
        }
    }

    @Test
    fun `searchCountries returns empty list when API throws IOException`() = runTest {
        // Arrange
        coEvery {
            restCountriesApi.searchCountries("Sweden")
        } throws IOException("Network unavailable")

        // Act
        val result = repository.searchCountries("Sweden")

        // Assert
        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun `getCountryByName returns null when REST API throws IOException`() = runTest {
        // Arrange
        coEvery {
            restCountriesApi.searchCountry("Sweden")
        } throws IOException("Network unavailable")

        // Act
        val result = repository.getCountryByName("Sweden")

        // Assert
        assertNull(result)

        // WikiData should never be called
        coVerify(exactly = 0) {
            wikiDataApi.wikiDataCountryIdByName(search = any())
        }
    }

    @Test
    fun `getCountryByName returns null when country is not found`() = runTest {
        // Arrange
        coEvery {
            restCountriesApi.searchCountry("Atlantis")
        } returns emptyList()

        // Act
        val result = repository.getCountryByName("Atlantis")

        // Assert
        assertNull(result)
    }

    @Test
    fun `getAllCountrySearchResults returns mapped countries`() = runTest {
        // Arrange
        val country1 = mockk<CountryResultDto>(relaxed = true)
        val country2 = mockk<CountryResultDto>(relaxed = true)

        coEvery {
            restCountriesApi.getAllCountries()
        } returns listOf(country1, country2)

        // Act
        val result = repository.getAllCountrySearchResults()

        // Assert
        assertEquals(2, result.size)

        coVerify(exactly = 1) {
            restCountriesApi.getAllCountries()
        }
    }

    @Test
    fun `getAllCountrySearchResults uses cache on second call`() = runTest {
        // Arrange
        val country = mockk<CountryResultDto>(relaxed = true)

        coEvery {
            restCountriesApi.getAllCountries()
        } returns listOf(country)

        // Act
        repository.getAllCountrySearchResults()
        repository.getAllCountrySearchResults()

        // Assert
        coVerify(exactly = 1) {
            restCountriesApi.getAllCountries()
        }
    }

    @Test
    fun `getAllCountrySearchResults returns empty list when API fails`() = runTest {
        // Arrange
        coEvery {
            restCountriesApi.getAllCountries()
        } throws IOException("Network unavailable")

        // Act
        val result = repository.getAllCountrySearchResults()

        // Assert
        assertEquals(emptyList<Any>(), result)
    }
}