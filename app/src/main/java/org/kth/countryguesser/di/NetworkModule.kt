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
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "CountryGuesserApp/1.0; User:kozar@kth.se") // Required to bypass HTTP 403, see more at https://foundation.wikimedia.org/wiki/Policy:Wikimedia_Foundation_User-Agent_Policy
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
