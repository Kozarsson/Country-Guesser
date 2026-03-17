package org.kth.countryguesser.model.api

import org.kth.countryguesser.model.data.CountryResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface CountriesApi {
    @GET("name/{name}")
    suspend fun searchCountries(@Path("name") name: String): List<CountryResult>
}