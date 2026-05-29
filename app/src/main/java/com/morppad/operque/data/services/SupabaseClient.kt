package com.morppad.operque.data.services

import com.morppad.operque.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val client by lazy {
        require(isConfigured) { "Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties." }
        createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
}
