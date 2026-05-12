package org.kth.countryguesser.model

import android.util.Log
import org.kth.countryguesser.util.InceptionYear
import kotlin.math.abs


interface CountryModel {
    val countryName: String
    val population: Long?
    val area: Double?
    val inceptionYear: InceptionYear?
    val flagUrl: String?
    val continents: List<String>?
    val borders: List<String>?
    val cca3: String?
    
//    fun compareTo(other: CountryModel): CountryComparisonResult
    fun compareAttributesTo(other: CountryModel, closenessCriteria: Double?): CountryComparisonResult
    fun compareAttributesTo(other: CountryModel): CountryComparisonResult
    suspend fun saveToDatabase()
}

class CountryModelImpl(
    override var countryName: String = "",
    override var population: Long? = null,
    override var area: Double? = null,
    override var inceptionYear: InceptionYear? = null,
    override var flagUrl: String? = null,
    override val continents: List<String>? = null,
    override val borders: List<String>? = null,
    override val cca3: String? = null,
) : CountryModel {
    override fun compareAttributesTo(other: CountryModel, closenessCriteria: Double?): CountryComparisonResult {
        return CountryComparisonResult(
            populationComparison = compareAttribute(this.population, other.population, closenessCriteria),
            areaComparison = compareAttribute(this.area, other.area, closenessCriteria),
            inceptionYearComparison = compareAttribute<InceptionYear>(this.inceptionYear, other.inceptionYear, closenessCriteria),
            continentsComparison = compareAttribute(this.continents, other.continents),
            bordersComparison = checkMember(other.cca3, this.borders),
        )
    }

    override fun compareAttributesTo(other: CountryModel): CountryComparisonResult {
        return compareAttributesTo(other, closenessCriteria = null)
    }

    override suspend fun saveToDatabase() {
        // TODO: Implement save logic
    }

    private fun compareAttribute(value1: List<*>?, value2: List<*>?): CountryAttributeResult {
        return if (value1 == null || value2 == null) {
            CountryAttributeResult(comparison = null, isClose = null)
        } else {
            CountryAttributeResult(comparison = value1 == value2, isClose = null)
        }
    }
    
    private fun <T : Comparable<T>> compareAttribute(value1: T?, value2: T?, closenessCriteria: Double?): CountryAttributeResult {
        if (value1 == null || value2 == null) {
            return CountryAttributeResult(comparison = null, isClose = null)
        }
        if (value1 is List<*> && value2 is List<*>) {
            return CountryAttributeResult(comparison = value1.compareTo(value2), isClose = null)
        }
        val comparison = value1.compareTo(value2)
        if (closenessCriteria == null) {
            return CountryAttributeResult(comparison = comparison, isClose = null)
        }
        var isClose: Boolean? = null
        if (value1 is Number && value2 is Number) {
            val diff = abs(value1.toDouble() - value2.toDouble())
            isClose = diff < closenessCriteria * value1.toDouble()
        } else if (value1 is InceptionYear && value2 is InceptionYear) {
            // Normalise BC/AD: BC years as negative, AD as positive
            val year1 = if (value1.datingSystem == "BC") -value1.year else value1.year
            val year2 = if (value2.datingSystem == "BC") -value2.year else value2.year
            val diff = abs(year1 - year2)
            isClose = diff < closenessCriteria * abs(year1)
        }
        return CountryAttributeResult(comparison = comparison, isClose = isClose)
    }

    private fun <T : Comparable<T>> checkMember(member: T?, list: List<T>?): CountryAttributeResult {
        return if (list == null) {
            CountryAttributeResult(comparison = false, isClose = null)
        } else if (member == null) {
            CountryAttributeResult(comparison = null, isClose = null)
        } else if (member == this.cca3) {
            CountryAttributeResult(comparison = true, isClose = null)
        } else {
            CountryAttributeResult(comparison = list.contains(member), isClose = null)
        }
    }
}

data class CountryComparisonResult(
    val populationComparison: CountryAttributeResult,
    val areaComparison: CountryAttributeResult,
    val inceptionYearComparison: CountryAttributeResult,
    val continentsComparison: CountryAttributeResult,
    val bordersComparison: CountryAttributeResult,
)

/**
 * Represents the result of comparing a single country attribute between two countries.
 *
 * @property comparison The result of the comparison: -1 if the first value is less than the second,
 * 0/true if they are equal, 1 if the first is greater, false if they are not equal and not comparable, or null if either value is null.
 * @property isClose Indicates whether the two values are considered "close" according to a given closeness criteria.
 */
data class CountryAttributeResult(
    val comparison: Any?,
    val isClose: Boolean?
)
