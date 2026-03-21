package org.kth.countryguesser.model.service

import org.kth.countryguesser.BuildConfig
import org.kth.countryguesser.model.api.CountriesEndpoints
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/** Main Country Guesser Api Service. */
object RestCountriesApiService : RetrofitApiService<CountriesEndpoints>() {
    override val api: CountriesEndpoints by lazy {
        Retrofit.Builder()
            .client(network)
            .baseUrl(BuildConfig.BASE_RESTCOUNTRIES_URI)
            .addConverterFactory(MoshiConverterFactory.create(parser))
            .build()
            .create(CountriesEndpoints::class.java)
    }
}
//TODO: add the other api we need (one service per base api url)