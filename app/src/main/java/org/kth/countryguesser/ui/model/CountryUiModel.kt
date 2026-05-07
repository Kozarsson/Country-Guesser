package org.kth.countryguesser.ui.model

import org.kth.countryguesser.model.CountryAttributeResult
import org.kth.countryguesser.util.InceptionYear

data class CountryUiModel(
    val countryName: String,
    val population: Long?,
    val populationDiff: CountryAttributeResult? = null,
    val area: Double?,
    val areaDiff: CountryAttributeResult? = null,
    val inceptionYear: InceptionYear?,
    val inceptionYearDiff: CountryAttributeResult? = null,
    val flagUrl: String? = null,
    val continents: List<String>?,
    val continentsDiff: CountryAttributeResult? = null,
    val borders: List<String>?,
    val cioc: String?,
    val bordersDiff: CountryAttributeResult? = null,
    val cca2: String?,
)