package org.kth.countryguesser.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.kth.countryguesser.model.entity.LastGuessedDailyEntity
import org.kth.countryguesser.model.entity.UserProfileEntity
import org.kth.countryguesser.model.entity.UserSettingsEntity
import org.kth.countryguesser.model.entity.UserStatsEntity
import javax.inject.Inject

interface FirestoreRemoteDataSource {
	suspend fun createProfileIfMissing(uid: String, defaultNickname: String? = null)
	suspend fun createProfileForRegistration(uid: String, nickname: String)
	suspend fun isNicknameTaken(nickname: String): Boolean
	suspend fun claimNickname(nickname: String, uid: String): Boolean
	suspend fun releaseNickname(nickname: String, uid: String)
	suspend fun getProfile(uid: String): UserProfileEntity?
	suspend fun getAllProfiles(): List<UserProfileEntity>
	suspend fun updateNickname(uid: String, nickname: String)
	suspend fun updateSettings(uid: String, settings: UserSettingsEntity)
	suspend fun updateStats(uid: String, stats: UserStatsEntity)
	suspend fun updateLastDailyGuess(uid: String, lastGuessedDaily: LastGuessedDailyEntity)
}

class FirestoreRemoteDataSourceImpl @Inject constructor() : FirestoreRemoteDataSource {

	private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

	override suspend fun createProfileIfMissing(uid: String, defaultNickname: String?) {
		val documentRef = firestore.collection(USERS_COLLECTION).document(uid)
		val existing = documentRef.get().await()
		if (existing.exists()) return

		val nickname = defaultNickname.orEmpty()
		if (nickname.isNotBlank() && !claimNickname(nickname, uid)) {
			throw IllegalStateException("Nickname already taken")
		}

		val profile = UserProfileEntity(
			uid = uid,
			nickname = nickname
		)
		documentRef.set(profile.toFirestoreMap()).await()
	}

	override suspend fun createProfileForRegistration(uid: String, nickname: String) {
		val normalized = normalizeNickname(nickname)
		if (normalized.isBlank()) throw IllegalArgumentException("Nickname is required")

		if (!claimNickname(nickname, uid)) {
			throw IllegalStateException("Nickname already taken")
		}

		try {
			firestore.collection(USERS_COLLECTION)
				.document(uid)
				.set(
					UserProfileEntity(
						uid = uid,
						nickname = nickname.trim()
					).toFirestoreMap()
				)
				.await()
		} catch (e: Exception) {
			releaseNickname(nickname, uid)
			throw e
		}
	}

	override suspend fun isNicknameTaken(nickname: String): Boolean {
		val normalized = normalizeNickname(nickname)
		if (normalized.isBlank()) return false
		return firestore.collection(NICKNAMES_COLLECTION)
			.document(normalized)
			.get()
			.await()
			.exists()
	}

	override suspend fun claimNickname(nickname: String, uid: String): Boolean {
		val normalized = normalizeNickname(nickname)
		if (normalized.isBlank()) return false

		return try {
			firestore.collection(NICKNAMES_COLLECTION)
				.document(normalized)
				.set(
					mapOf(
						UID_FIELD to uid,
						NICKNAME_FIELD to nickname.trim(),
						NICKNAME_NORMALIZED_FIELD to normalized
					)
				)
				.await()
			true
		} catch (e: Exception) {
			false
		}
	}

	override suspend fun releaseNickname(nickname: String, uid: String) {
		val normalized = normalizeNickname(nickname)
		if (normalized.isBlank()) return

		firestore.collection(NICKNAMES_COLLECTION)
			.document(normalized)
			.delete()
			.await()
	}

	override suspend fun getProfile(uid: String): UserProfileEntity? {
		val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
		val data = snapshot.data ?: return null

		val statsMap = data[STATS_FIELD] as? Map<*, *> ?: emptyMap<String, Any>()
		val settingsMap = data[SETTINGS_FIELD] as? Map<*, *> ?: emptyMap<String, Any>()
		val lastGuessDailyMap = data[LAST_GUESSED_DAILY_FIELD] as? Map<*, *> ?: emptyMap<String, Any>()

		return UserProfileEntity(
			uid = uid,
			nickname = data[NICKNAME_FIELD] as? String ?: "",
			stats = UserStatsEntity(
				gamesPlayedDaily = statsMap.intValue(GAMES_PLAYED_DAILY_FIELD),
				// DAILY mode
				currentStreakDaily = statsMap.intValue(CURRENT_STREAK_DAILY_FIELD),
				bestStreakDaily = statsMap.intValue(BEST_STREAK_DAILY_FIELD),
				//lastGuessedDaily = statsMap[LAST_GUESSED_DAILY_FIELD] as? String ?: "",
				totalScore = statsMap.intValue(TOTAL_SCORE_FIELD),
				// ENDLESS mode
				gamesPlayedEndless = statsMap.intValue(GAMES_PLAYED_ENDLESS_FIELD),
				currentStreakEndless = statsMap.intValue(CURRENT_STREAK_ENDLESS_FIELD),
				bestStreakEndless = statsMap.intValue(BEST_STREAK_ENDLESS_FIELD),
			),
			settings = UserSettingsEntity(
				notificationsEnabled = settingsMap.booleanValue(NOTIFICATIONS_ENABLED_FIELD, true),
				darkModeEnabled = settingsMap.booleanValue(DARK_MODE_ENABLED_FIELD, false)
			),
			lastGuessedDaily = LastGuessedDailyEntity(
				date = lastGuessDailyMap[LAST_GUESSED_DAILY_DATE_FIELD] as? String ?: "",
				countryName = lastGuessDailyMap[LAST_GUESSED_DAILY_COUNTRY_NAME_FIELD] as? String ?: "",
				flagUrl = lastGuessDailyMap[LAST_GUESSED_DAILY_FLAG_URL_FIELD] as? String ?: ""
			)
		)
	}

	override suspend fun getAllProfiles(): List<UserProfileEntity> {
		val snapshots = firestore.collection(USERS_COLLECTION).get().await()
		return snapshots.documents.mapNotNull { snapshot ->
			val data = snapshot.data ?: return@mapNotNull null
			val uid = data[UID_FIELD] as? String ?: return@mapNotNull null

			val statsMap = data[STATS_FIELD] as? Map<*, *> ?: emptyMap<String, Any>()
			val settingsMap = data[SETTINGS_FIELD] as? Map<*, *> ?: emptyMap<String, Any>()
			val lastGuessDailyMap = data[LAST_GUESSED_DAILY_FIELD] as? Map<*, *> ?: emptyMap<String, Any>()

			UserProfileEntity(
				uid = uid,
				nickname = data[NICKNAME_FIELD] as? String ?: "",
				stats = UserStatsEntity(
					gamesPlayedDaily = statsMap.intValue(GAMES_PLAYED_DAILY_FIELD),
					// DAILY mode
					currentStreakDaily = statsMap.intValue(CURRENT_STREAK_DAILY_FIELD),
					bestStreakDaily = statsMap.intValue(BEST_STREAK_DAILY_FIELD),
					//lastGuessedDaily = statsMap[LAST_GUESSED_DAILY_FIELD] as? String ?: "",
					totalScore = statsMap.intValue(TOTAL_SCORE_FIELD),
					// ENDLESS mode
					gamesPlayedEndless = statsMap.intValue(GAMES_PLAYED_ENDLESS_FIELD),
					currentStreakEndless = statsMap.intValue(CURRENT_STREAK_ENDLESS_FIELD),
					bestStreakEndless = statsMap.intValue(BEST_STREAK_ENDLESS_FIELD),
				),
				settings = UserSettingsEntity(
					notificationsEnabled = settingsMap.booleanValue(NOTIFICATIONS_ENABLED_FIELD, true),
					darkModeEnabled = settingsMap.booleanValue(DARK_MODE_ENABLED_FIELD, false)
				),
				lastGuessedDaily = LastGuessedDailyEntity(
					date = lastGuessDailyMap[LAST_GUESSED_DAILY_DATE_FIELD] as? String ?: "",
					countryName = lastGuessDailyMap[LAST_GUESSED_DAILY_COUNTRY_NAME_FIELD] as? String ?: "",
					flagUrl = lastGuessDailyMap[LAST_GUESSED_DAILY_FLAG_URL_FIELD] as? String ?: ""
				)
			)
		}
	}

	override suspend fun updateNickname(uid: String, nickname: String) {
		val profile = getProfile(uid)
		if (!claimNickname(nickname, uid)) {
			throw IllegalStateException("Nickname already taken")
		}

		firestore.collection(USERS_COLLECTION)
			.document(uid)
			.set(
				mapOf(
					NICKNAME_FIELD to nickname.trim(),
					NICKNAME_NORMALIZED_FIELD to normalizeNickname(nickname)
				),
				SetOptions.merge()
			)
			.await()

		profile?.nickname
			?.takeIf { it.isNotBlank() && it.trim() != nickname.trim() }
			?.let { oldNickname ->
				releaseNickname(oldNickname, uid)
			}
	}

	override suspend fun updateSettings(uid: String, settings: UserSettingsEntity) {
		firestore.collection(USERS_COLLECTION)
			.document(uid)
			.set(mapOf(SETTINGS_FIELD to settings.toFirestoreMap()), SetOptions.merge())
			.await()
	}

	override suspend fun updateStats(uid: String, stats: UserStatsEntity) {
		firestore.collection(USERS_COLLECTION)
			.document(uid)
			.set(mapOf(STATS_FIELD to stats.toFirestoreMap()), SetOptions.merge())
			.await()
	}

	override suspend fun updateLastDailyGuess(uid: String, lastGuessedDaily: LastGuessedDailyEntity) {
		firestore.collection(USERS_COLLECTION)
			.document(uid)
			.set(mapOf(LAST_GUESSED_DAILY_FIELD to lastGuessedDaily.toFirestoreMap()), SetOptions.merge())
			.await()
	}

	private fun UserProfileEntity.toFirestoreMap(): Map<String, Any> {
		return mapOf(
			UID_FIELD to uid,
			NICKNAME_FIELD to nickname,
			NICKNAME_NORMALIZED_FIELD to normalizeNickname(nickname),
			STATS_FIELD to stats.toFirestoreMap(),
			SETTINGS_FIELD to settings.toFirestoreMap(),
			LAST_GUESSED_DAILY_FIELD to lastGuessedDaily.toFirestoreMap()
		)
	}

	private fun normalizeNickname(nickname: String): String = nickname.trim().lowercase()

	private fun UserStatsEntity.toFirestoreMap(): Map<String, Any> {
		return mapOf(
			GAMES_PLAYED_DAILY_FIELD to gamesPlayedDaily,
			// DAILY mode
			CURRENT_STREAK_DAILY_FIELD to currentStreakDaily,
			BEST_STREAK_DAILY_FIELD to bestStreakDaily,
			//LAST_GUESSED_DAILY_FIELD to lastGuessedDaily,
			TOTAL_SCORE_FIELD to totalScore,
			// ENDLESS mode
			GAMES_PLAYED_ENDLESS_FIELD to gamesPlayedEndless,
			CURRENT_STREAK_ENDLESS_FIELD to currentStreakEndless,
			BEST_STREAK_ENDLESS_FIELD to bestStreakEndless,
		)
	}

	private fun UserSettingsEntity.toFirestoreMap(): Map<String, Any> {
		return mapOf(
			NOTIFICATIONS_ENABLED_FIELD to notificationsEnabled,
			DARK_MODE_ENABLED_FIELD to darkModeEnabled
		)
	}

	private fun LastGuessedDailyEntity.toFirestoreMap(): Map<String, Any> {
		return mapOf(
			LAST_GUESSED_DAILY_DATE_FIELD to date,
			LAST_GUESSED_DAILY_COUNTRY_NAME_FIELD to countryName,
			LAST_GUESSED_DAILY_FLAG_URL_FIELD to flagUrl
		)
	}

	private fun Map<*, *>.intValue(key: String): Int = (this[key] as? Number)?.toInt() ?: 0

	private fun Map<*, *>.booleanValue(key: String, fallback: Boolean): Boolean =
		this[key] as? Boolean ?: fallback

	private companion object {
		const val USERS_COLLECTION = "users"
		const val NICKNAMES_COLLECTION = "nicknames"
		const val UID_FIELD = "uid"
		const val NICKNAME_FIELD = "nickname"
		const val NICKNAME_NORMALIZED_FIELD = "nicknameNormalized"
		const val STATS_FIELD = "stats"
		const val SETTINGS_FIELD = "settings"

		const val GAMES_PLAYED_DAILY_FIELD = "gamesPlayedDaily"
		// DAILY mode
		const val CURRENT_STREAK_DAILY_FIELD = "currentStreakDaily"
		const val BEST_STREAK_DAILY_FIELD = "bestStreakDaily"
		const val LAST_GUESSED_DAILY_FIELD = "lastGuessedDaily"
		const val TOTAL_SCORE_FIELD = "totalScore"
		// ENDLESS mode
		const val GAMES_PLAYED_ENDLESS_FIELD = "gamesPlayedEndless"
		const val CURRENT_STREAK_ENDLESS_FIELD = "currentStreakEndless"
		const val BEST_STREAK_ENDLESS_FIELD = "bestStreakEndless"


		const val NOTIFICATIONS_ENABLED_FIELD = "notificationsEnabled"
		const val DARK_MODE_ENABLED_FIELD = "darkModeEnabled"

		const val LAST_GUESSED_DAILY_DATE_FIELD = "lastGuessedDailyDate"
		const val LAST_GUESSED_DAILY_COUNTRY_NAME_FIELD = "lastGuessedDailyCountryName"
		const val LAST_GUESSED_DAILY_FLAG_URL_FIELD = "lastGuessedDailyFlagUrl"
	}
}