package org.kth.countryguesser.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
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
	suspend fun updateNickname(uid: String, nickname: String)
	suspend fun updateSettings(uid: String, settings: UserSettingsEntity)
	suspend fun updateStats(uid: String, stats: UserStatsEntity)
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

		return UserProfileEntity(
			uid = uid,
			nickname = data[NICKNAME_FIELD] as? String ?: "",
			stats = UserStatsEntity(
				gamesPlayed = statsMap.intValue(GAMES_PLAYED_FIELD),
				wins = statsMap.intValue(WINS_FIELD),
				currentStreak = statsMap.intValue(CURRENT_STREAK_FIELD),
				bestStreak = statsMap.intValue(BEST_STREAK_FIELD)
			),
			settings = UserSettingsEntity(
				notificationsEnabled = settingsMap.booleanValue(NOTIFICATIONS_ENABLED_FIELD, true),
				darkModeEnabled = settingsMap.booleanValue(DARK_MODE_ENABLED_FIELD, false)
			)
		)
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

	private fun UserProfileEntity.toFirestoreMap(): Map<String, Any> {
		return mapOf(
			UID_FIELD to uid,
			NICKNAME_FIELD to nickname,
			NICKNAME_NORMALIZED_FIELD to normalizeNickname(nickname),
			STATS_FIELD to stats.toFirestoreMap(),
			SETTINGS_FIELD to settings.toFirestoreMap()
		)
	}

	private fun normalizeNickname(nickname: String): String = nickname.trim().lowercase()

	private fun UserStatsEntity.toFirestoreMap(): Map<String, Any> {
		return mapOf(
			GAMES_PLAYED_FIELD to gamesPlayed,
			WINS_FIELD to wins,
			CURRENT_STREAK_FIELD to currentStreak,
			BEST_STREAK_FIELD to bestStreak
		)
	}

	private fun UserSettingsEntity.toFirestoreMap(): Map<String, Any> {
		return mapOf(
			NOTIFICATIONS_ENABLED_FIELD to notificationsEnabled,
			DARK_MODE_ENABLED_FIELD to darkModeEnabled
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

		const val GAMES_PLAYED_FIELD = "gamesPlayed"
		const val WINS_FIELD = "wins"
		const val CURRENT_STREAK_FIELD = "currentStreak"
		const val BEST_STREAK_FIELD = "bestStreak"

		const val NOTIFICATIONS_ENABLED_FIELD = "notificationsEnabled"
		const val DARK_MODE_ENABLED_FIELD = "darkModeEnabled"
	}
}