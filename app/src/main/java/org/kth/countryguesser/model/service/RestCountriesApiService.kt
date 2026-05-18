package org.kth.countryguesser.model.service

import org.kth.countryguesser.BuildConfig
import org.kth.countryguesser.model.api.RestCountriesEndpoints
import org.kth.countryguesser.model.api.WikiDataEndpoints
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/** Main Country Guesser Api Service. */
object RestCountriesApiService : RetrofitApiService<RestCountriesEndpoints>() {
    override val api: RestCountriesEndpoints by lazy {
        Retrofit.Builder()
            .client(network)
            .baseUrl(BuildConfig.BASE_RESTCOUNTRIES_URI)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RestCountriesEndpoints::class.java)
    }
}

/** WikiData (Wikipedia) Api Service. */
object WikiDataApiService : RetrofitApiService<WikiDataEndpoints>() {
    override val api: WikiDataEndpoints by lazy {
        Retrofit.Builder()
            .client(network)
            .baseUrl(BuildConfig.BASE_WIKIDATA_URI)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikiDataEndpoints::class.java)
    }
}