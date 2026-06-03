package com.morppad.operque.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morppad.operque.data.model.Comment
import com.morppad.operque.data.model.Profile
import com.morppad.operque.data.model.Task
import com.morppad.operque.data.model.TaskStatus
import com.morppad.operque.data.repository.CommentRepository
import com.morppad.operque.data.repository.ProfileRepository
import com.morppad.operque.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserHomeViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val taskRepository: TaskRepository = TaskRepository(),
    private val commentRepository: CommentRepository = CommentRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(UserHomeUiState())
    val state: StateFlow<UserHomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val profile = profileRepository.getCurrentProfile()
                val tasks = taskRepository.getCurrentUserTasks()
                profile to tasks
            }.onSuccess { (profile, tasks) ->
                _state.update { it.copy(profile = profile, tasks = tasks, isLoading = false) }
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, errorMessage = throwable.toUserMessage()) }
            }
        }
    }

    fun openTask(task: Task) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    screen = UserScreen.TaskDetails,
                    selectedTask = task,
                    comments = emptyList(),
                    isLoadingComments = true,
                    errorMessage = null
                )
            }
            loadComments(task.id)
        }
    }

    fun closeTaskDetails() = _state.update {
        it.copy(
            screen = UserScreen.TaskList,
            selectedTask = null,
            comments = emptyList(),
            newCommentText = "",
            errorMessage = null
        )
    }

    fun changeSelectedTaskStatus(status: String) {
        val task = state.value.selectedTask ?: return
        if (status !in TaskStatus.All || status == task.status) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                taskRepository.updateTaskStatus(task.id, status)
                taskRepository.getCurrentUserTasks()
            }.onSuccess { tasks ->
                val updatedTask = tasks.firstOrNull { it.id == task.id } ?: task.copy(status = status)
                _state.update {
                    it.copy(
                        tasks = tasks,
                        selectedTask = updatedTask,
                        isSaving = false
                    )
                }
            }.onFailure { throwable ->
                _state.update { it.copy(isSaving = false, errorMessage = throwable.toUserMessage()) }
            }
        }
    }

    fun onNewCommentChange(value: String) = _state.update {
        it.copy(newCommentText = value, errorMessage = null)
    }

    fun addComment() {
        val taskId = state.value.selectedTask?.id ?: return
        val text = state.value.newCommentText.trim()
        if (text.isBlank()) {
            _state.update { it.copy(errorMessage = "Enter comment text") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                commentRepository.addComment(taskId, text)
                commentRepository.getTaskComments(taskId)
            }.onSuccess { comments ->
                _state.update { it.copy(comments = comments, newCommentText = "", isSaving = false) }
            }.onFailure { throwable ->
                _state.update { it.copy(isSaving = false, errorMessage = throwable.toUserMessage()) }
            }
        }
    }

    private suspend fun loadComments(taskId: String) {
        runCatching { commentRepository.getTaskComments(taskId) }
            .onSuccess { comments ->
                _state.update { it.copy(comments = comments, isLoadingComments = false) }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(isLoadingComments = false, errorMessage = throwable.toUserMessage())
                }
            }
    }
}

data class UserHomeUiState(
    val profile: Profile? = null,
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val comments: List<Comment> = emptyList(),
    val screen: UserScreen = UserScreen.TaskList,
    val newCommentText: String = "",
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

enum class UserScreen { TaskList, TaskDetails }

private fun Throwable.toUserMessage(): String {
    val message = message.orEmpty().substringBefore("URL:").trim()
    return message.ifBlank { "Operation failed" }
}
