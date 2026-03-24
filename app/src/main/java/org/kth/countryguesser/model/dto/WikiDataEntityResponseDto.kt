package org.kth.countryguesser.model.dto

import com.google.gson.annotations.SerializedName

// Get the inception date by
// response.entities[entityId]?.claims?.inception?.firstOrNull()?.mainsnak?.datavalue?.value?.time
data class WikiDataEntityResponseDto(
    @SerializedName("entities")
    val entities: Map<String, Entity>
) {
    data class Entity(
        @SerializedName("claims")
        val claims: Claims?
    )

    data class Claims(
        @SerializedName("P571")
        val inception: List<InceptionStatement>?
    )

    data class InceptionStatement(
        @SerializedName("mainsnak")
        val mainsnak: MainSnak?
    )

    data class MainSnak(
        @SerializedName("datavalue")
        val datavalue: DataValue?
    )

    data class DataValue(
        @SerializedName("value")
        val value: TimeValue?
    )

    data class TimeValue(
        @SerializedName("time")
        val time: String?
    )
}

