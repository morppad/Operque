package com.morppad.operque.data.repository

import com.morppad.operque.data.model.Profile
import com.morppad.operque.data.model.ProfileRole
import com.morppad.operque.data.services.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

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
}
