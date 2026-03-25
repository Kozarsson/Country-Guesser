package org.kth.countryguesser.model.repository

class CountryRepository {

    fun extractYearFromWikiData(wikiData: String): InceptionYear {
        var datingSystem = ""
        if (wikiData.startsWith("+")) {
            datingSystem = "AD"
        } else if (wikiData.startsWith("-")) {
            datingSystem = "BC" // Kinda redundant because the oldest country according to wikipedia is Japan at 539 AD
        }
        val year: Int = wikiData.trimStart('+', '-').take(4).toInt()
        return InceptionYear(datingSystem, year)
    }
}

data class InceptionYear(
    val datingSystem: String,
    val year: Int
) : Comparable<InceptionYear> {
    override fun compareTo(other: InceptionYear): Int {
        val thisNormalized = if (this.datingSystem == "BC") -this.year else this.year
        val otherNormalized = if (other.datingSystem == "BC") -other.year else other.year
        return thisNormalized.compareTo(otherNormalized)
    }
}
