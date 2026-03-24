package org.kth.countryguesser.model.repository

class CountryRepository {

    fun extractYearFromWikiData(wikiData: String): InceptionYear {
        var datingSystem: String = ""
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
)