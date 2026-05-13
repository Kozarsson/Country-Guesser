package org.kth.countryguesser.data.remote.entity

data class UserEntity(
    val email: String? = null,
    val displayName: String? = null,
    val uid: String,
    val isAnonymous: Boolean = true
)