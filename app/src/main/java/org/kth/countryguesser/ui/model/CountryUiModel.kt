package org.kth.countryguesser.ui.model

import org.kth.countryguesser.util.InceptionYear

data class CountryUiModel(
    val countryName: String,
    val population: Long?,
    val area: Double?,
    val inceptionYear: InceptionYear?
    //TODO: add Country flag image
)