package org.kth.countryguesser.model.entity

data class UserEntity(
    val email: String? = null,
    val displayName: String? = null,
    val uid: String,
    val isAnonymous: Boolean = true
)