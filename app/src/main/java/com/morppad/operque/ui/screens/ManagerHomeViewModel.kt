package com.morppad.operque.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morppad.operque.data.model.Comment
import com.morppad.operque.data.model.Profile
import com.morppad.operque.data.model.ProfileRole
import com.morppad.operque.data.model.Task
import com.morppad.operque.data.model.TaskStatus
import com.morppad.operque.data.repository.CommentRepository
import com.morppad.operque.data.repository.ProfileRepository
import com.morppad.operque.data.repository.TaskRepository
import com.morppad.operque.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ManagerHomeViewModel(
    private val profileRepository: ProfileRepository = ProfileRepository(),
    private val taskRepository: TaskRepository = TaskRepository(),
    private val commentRepository: CommentRepository = CommentRepository(),
    private val userManagementRepository: UserManagementRepository = UserManagementRepository()
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
                ManagerData(
                    profileRepository.getCurrentProfile(),
                    profileRepository.getAllProfiles(),
                    profileRepository.getEmployees(),
                    taskRepository.getAllTasks()
                )
            }.onSuccess { data ->
                _state.update {
                    it.copy(
                        managerProfile = data.profile,
                        profiles = data.profiles,
                        employees = data.employees,
                        tasks = data.tasks,
                        selectedEmployeeId = it.selectedEmployeeId ?: data.employees.firstOrNull()?.id,
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

    fun openUsers() = _state.update {
        it.copy(screen = ManagerScreen.UserList, errorMessage = null)
    }

    fun openCreateUser() = _state.update {
        it.copy(screen = ManagerScreen.CreateUser, errorMessage = null)
    }

    fun closeUsers() = _state.update {
        it.copy(screen = ManagerScreen.TaskList, errorMessage = null)
    }

    fun closeCreateUser() = _state.update {
        it.copy(
            screen = ManagerScreen.UserList,
            newUserEmail = "",
            newUserPassword = "",
            newUserRole = ProfileRole.User,
            errorMessage = null
        )
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

    fun onNewUserEmailChange(value: String) = _state.update {
        it.copy(newUserEmail = value, errorMessage = null)
    }

    fun onNewUserPasswordChange(value: String) = _state.update {
        it.copy(newUserPassword = value, errorMessage = null)
    }

    fun onNewUserRoleChange(value: String) = _state.update {
        it.copy(newUserRole = value, errorMessage = null)
    }

    fun createUser() {
        val current = state.value
        if (!current.newUserEmail.contains("@")) {
            _state.update { it.copy(errorMessage = "Введите корректный email") }
            return
        }
        if (current.newUserPassword.length < 6) {
            _state.update { it.copy(errorMessage = "Пароль должен содержать минимум 6 символов") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                userManagementRepository.createUser(
                    current.newUserEmail,
                    current.newUserPassword,
                    current.newUserRole
                )
                loadProfiles()
            }.onSuccess { (profiles, employees) ->
                _state.update {
                    it.copy(
                        profiles = profiles,
                        employees = employees,
                        screen = ManagerScreen.UserList,
                        newUserEmail = "",
                        newUserPassword = "",
                        newUserRole = ProfileRole.User,
                        isSaving = false
                    )
                }
            }.onFailure { throwable ->
                _state.update { it.copy(isSaving = false, errorMessage = throwable.toManagerMessage()) }
            }
        }
    }

    fun updateUserRole(userId: String, role: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                profileRepository.updateRole(userId, role)
                loadProfiles()
            }.onSuccess { (profiles, employees) ->
                _state.update { it.copy(profiles = profiles, employees = employees, isSaving = false) }
            }.onFailure { throwable ->
                _state.update { it.copy(isSaving = false, errorMessage = throwable.toManagerMessage()) }
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                userManagementRepository.deleteUser(userId)
                loadProfiles()
            }.onSuccess { (profiles, employees) ->
                _state.update { it.copy(profiles = profiles, employees = employees, isSaving = false) }
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

    fun deleteSelectedTask() {
        val taskId = state.value.selectedTask?.id ?: return
        deleteTask(taskId)
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching {
                taskRepository.deleteTaskAsManager(taskId)
                taskRepository.getAllTasks()
            }.onSuccess { tasks ->
                _state.update {
                    it.copy(
                        tasks = tasks,
                        selectedTask = if (it.selectedTask?.id == taskId) null else it.selectedTask,
                        comments = if (it.selectedTask?.id == taskId) emptyList() else it.comments,
                        screen = if (it.selectedTask?.id == taskId) ManagerScreen.TaskList else it.screen,
                        isSaving = false
                    )
                }
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

    private suspend fun loadProfiles(): Pair<List<Profile>, List<Profile>> {
        return profileRepository.getAllProfiles() to profileRepository.getEmployees()
    }
}

data class ManagerHomeUiState(
    val managerProfile: Profile? = null,
    val profiles: List<Profile> = emptyList(),
    val employees: List<Profile> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val comments: List<Comment> = emptyList(),
    val screen: ManagerScreen = ManagerScreen.TaskList,
    val selectedEmployeeId: String? = null,
    val newTaskTitle: String = "",
    val newTaskDescription: String = "",
    val newCommentText: String = "",
    val newUserEmail: String = "",
    val newUserPassword: String = "",
    val newUserRole: String = ProfileRole.User,
    val isLoading: Boolean = false,
    val isLoadingComments: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

enum class ManagerScreen { TaskList, CreateTask, TaskDetails, UserList, CreateUser }

private data class ManagerData(
    val profile: Profile?,
    val profiles: List<Profile>,
    val employees: List<Profile>,
    val tasks: List<Task>
)

private fun Throwable.toManagerMessage(): String {
    val message = message.orEmpty().substringBefore("URL:").trim()
    if (message.contains("NOT_FOUND") || message.contains("Requested function was not found")) {
        return "Функция управления пользователями не развернута в Supabase"
    }
    return message.ifBlank { "Operation failed" }
}
