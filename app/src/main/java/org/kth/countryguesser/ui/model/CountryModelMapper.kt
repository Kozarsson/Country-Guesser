package org.kth.countryguesser.ui.model

import org.kth.countryguesser.model.CountryAttributeResult
import org.kth.countryguesser.model.CountryModel
import org.kth.countryguesser.model.dto.CountryResultDto

/**
 * Maps a [CountryModel] to a [CountryUiModel] for UI display purposes.
 */
fun CountryModel.toUiModel(): CountryUiModel = CountryUiModel(
    countryName = this.countryName,
    population = this.population,
    area = this.area,
    inceptionYear = this.inceptionYear,
)
fun CountryModel.toUiModel(
    populationDiff: CountryAttributeResult?,
    areaDiff: CountryAttributeResult?,
    inceptionYearDiff: CountryAttributeResult?,
): CountryUiModel = CountryUiModel(
    countryName = this.countryName,
    population = this.population,
    populationDiff = populationDiff,
    area = this.area,
    areaDiff = areaDiff,
    inceptionYear = this.inceptionYear,
    inceptionYearDiff = inceptionYearDiff,
)

/**
 * Maps a [CountryResultDto] to a [CountryUiModel] for UI display purposes.
 */
fun CountryResultDto.toUiModel(): CountryUiModel = CountryUiModel(
    countryName = this.name?.common ?: "",
    population = this.population,
    area = this.area,
    inceptionYear = null // Map if available
)

/**
 * Maps a list of [CountryResultDto] to a list of [CountryUiModel] for UI display purposes.
 */
fun List<CountryResultDto>.toUiModelList(): List<CountryUiModel> = this.map { it.toUiModel() }
