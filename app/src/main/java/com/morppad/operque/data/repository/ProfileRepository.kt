package com.morppad.operque.data.repository

import com.morppad.operque.data.model.Profile
import com.morppad.operque.data.model.ProfileRole
import com.morppad.operque.data.services.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileRepository {
    private companion object {
        const val ProfilesTable = "profiles"
    }

    private val client get() = SupabaseClientProvider.client

    fun currentUserId(): String? = client.auth.currentUserOrNull()?.id

    suspend fun getCurrentProfile(): Profile? {
        val userId = currentUserId() ?: return null
        return client.from(ProfilesTable).select {
            filter { eq("id", userId) }
            limit(1)
        }.decodeSingleOrNull<Profile>()
    }

    suspend fun getEmployees(): List<Profile> {
        return client.from(ProfilesTable).select {
            filter { eq("role", ProfileRole.User) }
            order("email", Order.ASCENDING)
        }.decodeList<Profile>()
    }

    suspend fun getAllProfiles(): List<Profile> {
        return client.from(ProfilesTable).select {
            order("created_at", Order.DESCENDING)
        }.decodeList<Profile>()
    }

    suspend fun updateRole(userId: String, role: String) {
        client.postgrest.rpc(
            function = "update_user_role",
            parameters = buildJsonObject {
                put("target_user_id", userId)
                put("new_role", role)
            }
        )
    }
}
