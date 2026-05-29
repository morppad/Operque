package com.morppad.operque.data.repository

import com.morppad.operque.data.model.ProfileUpsertDto
import com.morppad.operque.data.services.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from

class AuthRepository {
    private companion object {
        const val ProfilesTable = "profiles"
    }

    private val client get() = SupabaseClientProvider.client

    fun isConfigured(): Boolean = SupabaseClientProvider.isConfigured

    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        createCurrentUserProfileIfMissing()
    }

    suspend fun signUp(email: String, password: String) {
        val authUser = client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        createProfileIfMissing(authUser ?: currentAuthUserOrNull(), email)
    }

    suspend fun signOut() = client.auth.signOut()
    fun hasActiveSession(): Boolean = client.auth.currentSessionOrNull() != null

    private suspend fun createCurrentUserProfileIfMissing() {
        createProfileIfMissing(currentAuthUserOrNull())
    }

    private suspend fun currentAuthUserOrNull(): UserInfo? {
        return client.auth.currentUserOrNull()
            ?: runCatching {
                if (client.auth.currentSessionOrNull() == null) {
                    null
                } else {
                    client.auth.retrieveUserForCurrentSession(updateSession = true)
                }
            }.getOrNull()
    }

    private suspend fun createProfileIfMissing(user: UserInfo?, fallbackEmail: String? = null) {
        val id = user?.id ?: return
        val email = user.email ?: fallbackEmail?.trim().orEmpty()
        if (email.isBlank()) return

        client.from(ProfilesTable).upsert(
            ProfileUpsertDto(
                id = id,
                email = email
            )
        ) {
            onConflict = "id"
            ignoreDuplicates = true
            defaultToNull = false
        }
    }
}
