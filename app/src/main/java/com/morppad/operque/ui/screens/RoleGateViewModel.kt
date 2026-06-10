package com.morppad.operque.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morppad.operque.data.model.ProfileRole
import com.morppad.operque.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoleGateViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(RoleGateUiState())
    val state: StateFlow<RoleGateUiState> = _state.asStateFlow()

    init {
        loadRole()
    }

    fun loadRole() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { profileRepository.getCurrentProfile() }
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            destination = if (ProfileRole.canManageTasks(profile?.role.orEmpty())) {
                                RoleDestination.Manager
                            } else {
                                RoleDestination.User
                            }
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message?.substringBefore("URL:")?.trim()
                                ?: "Profile loading failed"
                        )
                    }
                }
        }
    }
}

data class RoleGateUiState(
    val isLoading: Boolean = false,
    val destination: RoleDestination? = null,
    val errorMessage: String? = null
)

enum class RoleDestination { User, Manager }
