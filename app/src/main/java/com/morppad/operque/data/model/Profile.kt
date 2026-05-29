package com.morppad.operque.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val DefaultProfileRole = "user"

@Serializable
data class Profile(
    val id: String,
    val email: String,
    val role: String = DefaultProfileRole,
    @SerialName("created_at")
    val createdAt: Instant? = null
)

@Serializable
data class ProfileUpsertDto(
    val id: String,
    val email: String,
    val role: String = DefaultProfileRole
)
