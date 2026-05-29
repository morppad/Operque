package com.morppad.operque.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morppad.operque.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState(isConfigured = repository.isConfigured()))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        if (repository.isConfigured()) {
            runCatching { repository.hasActiveSession() }
                .onSuccess { authorized -> _state.update { it.copy(isAuthorized = authorized) } }
        }
    }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    fun onPasswordRepeatChange(value: String) = _state.update { it.copy(passwordRepeat = value, errorMessage = null) }
    fun switchMode(mode: AuthMode) = _state.update { it.copy(mode = mode, errorMessage = null) }

    fun submit() {
        val current = state.value
        validate(current)?.let { error ->
            _state.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                when (current.mode) {
                    AuthMode.Login -> repository.signIn(current.email, current.password)
                    AuthMode.Register -> repository.signUp(current.email, current.password)
                }
                repository.hasActiveSession()
            }.onSuccess { hasActiveSession ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isAuthorized = hasActiveSession,
                        registrationCompleted = current.mode == AuthMode.Register && !hasActiveSession,
                        mode = if (current.mode == AuthMode.Register && !hasActiveSession) AuthMode.Login else current.mode,
                        password = "",
                        passwordRepeat = ""
                    )
                }
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, errorMessage = throwable.toAuthMessage()) }
            }
        }
    }

    fun clearRegistrationCompleted() = _state.update { it.copy(registrationCompleted = false) }

    fun signOut() {
        viewModelScope.launch {
            runCatching { repository.signOut() }
            _state.value = AuthUiState(isConfigured = repository.isConfigured())
        }
    }

    private fun validate(state: AuthUiState): String? {
        if (!state.isConfigured) return "Supabase keys are not configured in local.properties"
        if (!state.email.contains("@")) return "Enter a valid email"
        if (state.password.length < 6) return "Password must contain at least 6 characters"
        if (state.mode == AuthMode.Register && state.password != state.passwordRepeat) return "Passwords do not match"
        return null
    }
}

private fun Throwable.toAuthMessage(): String {
    val message = message.orEmpty()
    return when {
        message.contains("invalid input syntax for type bigint", ignoreCase = true) ->
            "Supabase profiles.id must be uuid and reference auth.users.id."
        message.contains("row-level security", ignoreCase = true) ->
            "Profile access is blocked by Supabase RLS policy."
        else -> message.substringBefore("URL:").trim().ifBlank { "Authorization failed" }
    }
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.Login,
    val email: String = "",
    val password: String = "",
    val passwordRepeat: String = "",
    val isLoading: Boolean = false,
    val isAuthorized: Boolean = false,
    val registrationCompleted: Boolean = false,
    val errorMessage: String? = null,
    val isConfigured: Boolean = true
)

enum class AuthMode { Login, Register }
