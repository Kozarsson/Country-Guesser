package org.kth.countryguesser.model.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
// Represents the top-level response from the Wikidata API
// Only includes the list of search results
// Each result only contains the 'id' field
// Example: { "search": [ { "id": "Q419" }, ... ] }
data class WikiDataSearchResponseDto(
    @SerializedName("search")
    val search: List<SearchResult>
) : Parcelable {
    @Parcelize
    data class SearchResult(
        @SerializedName("id")
        val id: String
    ) : Parcelable
}
