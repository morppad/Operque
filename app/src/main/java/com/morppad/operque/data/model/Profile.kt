package com.morppad.operque.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val DefaultProfileRole = "user"

object ProfileRole {
    const val User = "user"
    const val Manager = "manager"
    const val Admin = "admin"

    fun canManageTasks(role: String): Boolean = role == Manager || role == Admin
}

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
