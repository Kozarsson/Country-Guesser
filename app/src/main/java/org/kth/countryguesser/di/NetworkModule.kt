package org.kth.countryguesser.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.kth.countryguesser.model.api.RestCountriesEndpoints
import org.kth.countryguesser.model.api.WikiDataEndpoints
import javax.inject.Singleton
import org.kth.countryguesser.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "CountryGuesserApp/1.0 (kozar@kth.se)") // Required to bypass HTTP 403
                    .build()
                chain.proceed(request)
            }
            .build()

    @Provides
    @Singleton
    fun provideRestCountriesEndpoints(client: OkHttpClient): RestCountriesEndpoints =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_RESTCOUNTRIES_URI)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RestCountriesEndpoints::class.java)

    @Provides
    @Singleton
    fun provideWikiDataEndpoints(client: OkHttpClient): WikiDataEndpoints =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_WIKIDATA_URI)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WikiDataEndpoints::class.java)
}

