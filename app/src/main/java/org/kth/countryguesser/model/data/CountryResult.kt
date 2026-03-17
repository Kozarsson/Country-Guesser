package org.kth.countryguesser.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

//TODO: Remove every field not required for the app
@Parcelize
data class CountryResult(
	val tld: List<String>? = null,
	val cca2: String? = null,
	val ccn3: String? = null,
	val cca3: String? = null,
	val cioc: String? = null,
	val independent: Boolean? = null,
	val status: String? = null,
	val unMember: Boolean? = null,
	val idd: Idd? = null,
	val capital: List<String>? = null,
	val altSpellings: List<String>? = null,
	val region: String? = null,
	val subregion: String? = null,
	val landlocked: Boolean? = null,
	val borders: List<String>? = null,
	val area: Double? = null,
	val maps: CountryMaps? = null,
	val population: Long? = null,
	val fifa: String? = null,
	val car: Car? = null,
	val timezones: List<String>? = null,
	val continents: List<String>? = null,
	val flag: String? = null,
	val name: CountryName? = null,
	val currencies: Map<String, CurrencyInfo>? = null,
	val languages: Map<String, String>? = null,
	val latlng: List<Double>? = null,
	val demonyms: Map<String, GenderedDemonym>? = null,
	val translations: Map<String, Translation>? = null,
	val gini: Map<String, Double>? = null,
	val flags: Flags? = null,
	@SerializedName("coatOfArms")
	val coatOfArms: CoatOfArms? = null,
	val startOfWeek: String? = null,
	val capitalInfo: CapitalInfo? = null,
	val postalCode: PostalCode? = null
) : Parcelable

@Parcelize
data class Idd(
	val root: String? = null,
	val suffixes: List<String>? = null
) : Parcelable

@Parcelize
data class CountryMaps(
	val googleMaps: String? = null,
	val openStreetMaps: String? = null
) : Parcelable

@Parcelize
data class Car(
	val signs: List<String>? = null,
	val side: String? = null
) : Parcelable

@Parcelize
data class CountryName(
	val common: String? = null,
	val official: String? = null,
	val nativeName: Map<String, Translation>? = null
) : Parcelable

@Parcelize
data class CurrencyInfo(
	val symbol: String? = null,
	val name: String? = null
) : Parcelable

@Parcelize
data class GenderedDemonym(
	val f: String? = null,
	val m: String? = null
) : Parcelable

@Parcelize
data class Translation(
	val official: String? = null,
	val common: String? = null
) : Parcelable

@Parcelize
data class Flags(
	val png: String? = null,
	val svg: String? = null,
	val alt: String? = null
) : Parcelable

@Parcelize
data class CoatOfArms(
	val png: String? = null,
	val svg: String? = null
) : Parcelable

@Parcelize
data class CapitalInfo(
	val latlng: List<Double>? = null
) : Parcelable

@Parcelize
data class PostalCode(
	val format: String? = null,
	val regex: String? = null
) : Parcelable