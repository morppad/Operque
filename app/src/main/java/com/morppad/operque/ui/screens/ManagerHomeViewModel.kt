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

class ManagerHomeViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val taskRepository: TaskRepository = TaskRepository(),
    private val commentRepository: CommentRepository = CommentRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(ManagerHomeUiState())
    val state: StateFlow<ManagerHomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                Triple(
                    profileRepository.getCurrentProfile(),
                    profileRepository.getEmployees(),
                    taskRepository.getAllTasks()
                )
            }.onSuccess { (profile, employees, tasks) ->
                _state.update {
                    it.copy(
                        managerProfile = profile,
                        employees = employees,
                        tasks = tasks,
                        selectedEmployeeId = it.selectedEmployeeId ?: employees.firstOrNull()?.id,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                _state.update { it.copy(isLoading = false, errorMessage = throwable.toManagerMessage()) }
            }
        }
    }

    fun openCreateTask() = _state.update {
        it.copy(screen = ManagerScreen.CreateTask, errorMessage = null)
    }

    fun closeCreateTask() = _state.update {
        it.copy(
            screen = ManagerScreen.TaskList,
            newTaskTitle = "",
            newTaskDescription = "",
            errorMessage = null
        )
    }

    fun selectEmployee(employeeId: String) = _state.update {
        it.copy(selectedEmployeeId = employeeId, errorMessage = null)
    }

    fun onNewTaskTitleChange(value: String) = _state.update {
        it.copy(newTaskTitle = value, errorMessage = null)
    }

    fun onNewTaskDescriptionChange(value: String) = _state.update {
        it.copy(newTaskDescription = value, errorMessage = null)
    }

    fun createTask() {
        val employeeId = state.value.selectedEmployeeId
        val title = state.value.newTaskTitle.trim()

        if (employeeId == null) {
            _state.update { it.copy(errorMessage = "Select employee") }
            return
        }
        if (title.isBlank()) {
            _state.update { it.copy(errorMessage = "Enter task title") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                taskRepository.createTaskForUser(employeeId, title, state.value.newTaskDescription)
                taskRepository.getAllTasks()
            }.onSuccess { tasks ->
                _state.update {
                    it.copy(
                        tasks = tasks,
                        screen = ManagerScreen.TaskList,
                        newTaskTitle = "",
                        newTaskDescription = "",
                        isSaving = false
                    )
                }
            }.onFailure { throwable ->
                _state.update { it.copy(isSaving = false, errorMessage = throwable.toManagerMessage()) }
            }
        }
    }

    fun openTask(task: Task) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    screen = ManagerScreen.TaskDetails,
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
            screen = ManagerScreen.TaskList,
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
                taskRepository.updateTaskStatusAsManager(task.id, status)
                taskRepository.getAllTasks()
            }.onSuccess { tasks ->
                val updatedTask = tasks.firstOrNull { it.id == task.id } ?: task.copy(status = status)
                _state.update { it.copy(tasks = tasks, selectedTask = updatedTask, isSaving = false) }
            }.onFailure { throwable ->
                _state.update { it.copy(isSaving = false, errorMessage = throwable.toManagerMessage()) }
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
                _state.update { it.copy(isSaving = false, errorMessage = throwable.toManagerMessage()) }
            }
        }
    }

    private suspend fun loadComments(taskId: String) {
        runCatching { commentRepository.getTaskComments(taskId) }
            .onSuccess { comments ->
                _state.update { it.copy(comments = comments, isLoadingComments = false) }
            }
            .onFailure { throwable ->
                _state.update {
                    it.copy(isLoadingComments = false, errorMessage = throwable.toManagerMessage())
                }
            }
    }
}

data class ManagerHomeUiState(
    val managerProfile: Profile? = null,
    val employees: List<Profile> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val comments: List<Comment> = emptyList(),
    val screen: ManagerScreen = ManagerScreen.TaskList,
    val selectedEmployeeId: String? = null,
    val newTaskTitle: String = "",
    val newTaskDescription: String = "",
    val newCommentText: String = "",
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

enum class ManagerScreen { TaskList, CreateTask, TaskDetails }

private fun Throwable.toManagerMessage(): String {
    val message = message.orEmpty().substringBefore("URL:").trim()
    return message.ifBlank { "Operation failed" }
}
