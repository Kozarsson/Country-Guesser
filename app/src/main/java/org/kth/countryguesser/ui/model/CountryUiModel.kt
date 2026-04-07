package org.kth.countryguesser.ui.model

import org.kth.countryguesser.util.InceptionYear

data class CountryUiModel(
    val countryName: String,
    val population: Long?,
    val populationDiff: Int? = null,
    val area: Double?,
    val areaDiff: Int? = null,
    val inceptionYear: InceptionYear?,
    val inceptionYearDiff: Int? = null,
    //TODO: add Country flag image
)