package org.kth.countryguesser.model.api

import org.kth.countryguesser.model.data.CountryResult
import retrofit2.http.GET
import retrofit2.http.Path

/** All endpoints related to the searching of countries. */
interface CountriesEndpoints {
    @GET("name/{name}")
    suspend fun searchCountries(@Path("name") name: String): List<CountryResult>
}