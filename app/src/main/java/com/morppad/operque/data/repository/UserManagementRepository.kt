package com.morppad.operque.data.repository

import com.morppad.operque.data.model.ManageUserRequest
import com.morppad.operque.data.services.SupabaseClientProvider
import io.github.jan.supabase.functions.functions
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class UserManagementRepository {
    private companion object {
        const val ManageUsersFunction = "manage-users"
    }

    private val client get() = SupabaseClientProvider.client

    suspend fun createUser(email: String, password: String, role: String) {
        invoke(
            ManageUserRequest(
                action = "create",
                email = email.trim(),
                password = password,
                role = role
            )
        )
    }

    suspend fun deleteUser(userId: String) {
        invoke(ManageUserRequest(action = "delete", userId = userId))
    }

    private suspend fun invoke(request: ManageUserRequest) {
        client.functions.invoke(
            function = ManageUsersFunction,
            body = request,
            headers = Headers.build {
                append(HttpHeaders.ContentType, "application/json")
            }
        )
    }
}
