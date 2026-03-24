package org.kth.countryguesser.model.api

import org.kth.countryguesser.model.dto.WikiDataSearchResponseDto
import org.kth.countryguesser.model.dto.WikiDataEntityResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** All endpoints related to WikiData. */
interface WikiDataEndpoints {
    @GET("w/api.php")
    suspend fun wikiDataCountryIdByName(
        @Query("action") action: String = "wbsearchentities",
        @Query("search") search: String,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json",
        @Query("type") type: String = "item"
    ): WikiDataSearchResponseDto

    @GET("wiki/Special:EntityData/{entityId}.json")
    suspend fun wikiDataEntityById(
        @Path("entityId") entityId: String
    ): WikiDataEntityResponseDto
}
