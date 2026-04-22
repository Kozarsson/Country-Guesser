package org.kth.countryguesser.model.api

import org.kth.countryguesser.data.remote.dto.CountryResultDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** All endpoints related to the searching of countries via RestCountries. */
interface RestCountriesEndpoints {
    @GET("name/{name}")
    suspend fun searchCountries(@Path("name") name: String): List<CountryResultDto>

    @GET("name/{name}")
    suspend fun searchCountry(@Path("name") name: String, @Query("fullText") fullText: String = "true"): List<CountryResultDto>

    @GET("all")
    suspend fun getAllCountries(@Query("fields") fields: String = "name,flags"): List<CountryResultDto>

    @GET("all")
    suspend fun getAllCountriesTest(@Query("fields") fields: String = "name,population,area,flags,continents"): List<CountryResultDto>
}