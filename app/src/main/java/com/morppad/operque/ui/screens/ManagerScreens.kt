package com.morppad.operque.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morppad.operque.data.model.Profile
import com.morppad.operque.data.model.ProfileRole
import com.morppad.operque.data.model.Task
import com.morppad.operque.data.model.TaskStatus
import com.morppad.operque.ui.theme.JiraBlueLight
import com.morppad.operque.ui.theme.JiraBorder
import com.morppad.operque.ui.theme.JiraTextSubtle
import com.morppad.operque.ui.theme.JiraRed

@Composable
fun ManagerHomeRoute(
    onLogout: () -> Unit,
    viewModel: ManagerHomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    BackHandler(enabled = state.screen != ManagerScreen.TaskList) {
        when (state.screen) {
            ManagerScreen.CreateTask -> viewModel.closeCreateTask()
            ManagerScreen.TaskDetails -> viewModel.closeTaskDetails()
            ManagerScreen.UserList -> viewModel.closeUsers()
            ManagerScreen.CreateUser -> viewModel.closeCreateUser()
            ManagerScreen.TaskList -> Unit
        }
    }

    ManagerHomeScreen(
        state = state,
        onLogout = onLogout,
        onRefresh = viewModel::refresh,
        onOpenUsers = viewModel::openUsers,
        onCloseUsers = viewModel::closeUsers,
        onOpenCreateUser = viewModel::openCreateUser,
        onCloseCreateUser = viewModel::closeCreateUser,
        onNewUserEmailChange = viewModel::onNewUserEmailChange,
        onNewUserPasswordChange = viewModel::onNewUserPasswordChange,
        onNewUserRoleChange = viewModel::onNewUserRoleChange,
        onCreateUser = viewModel::createUser,
        onUpdateUserRole = viewModel::updateUserRole,
        onDeleteUser = viewModel::deleteUser,
        onOpenCreateTask = viewModel::openCreateTask,
        onCloseCreateTask = viewModel::closeCreateTask,
        onSelectEmployee = viewModel::selectEmployee,
        onNewTaskTitleChange = viewModel::onNewTaskTitleChange,
        onNewTaskDescriptionChange = viewModel::onNewTaskDescriptionChange,
        onCreateTask = viewModel::createTask,
        onOpenTask = viewModel::openTask,
        onCloseTaskDetails = viewModel::closeTaskDetails,
        onChangeTaskStatus = viewModel::changeSelectedTaskStatus,
        onDeleteTask = viewModel::deleteSelectedTask,
        onNewCommentChange = viewModel::onNewCommentChange,
        onAddComment = viewModel::addComment
    )
}

@Composable
fun ManagerHomeScreen(
    state: ManagerHomeUiState,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onOpenUsers: () -> Unit,
    onCloseUsers: () -> Unit,
    onOpenCreateUser: () -> Unit,
    onCloseCreateUser: () -> Unit,
    onNewUserEmailChange: (String) -> Unit,
    onNewUserPasswordChange: (String) -> Unit,
    onNewUserRoleChange: (String) -> Unit,
    onCreateUser: () -> Unit,
    onUpdateUserRole: (String, String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onOpenCreateTask: () -> Unit,
    onCloseCreateTask: () -> Unit,
    onSelectEmployee: (String) -> Unit,
    onNewTaskTitleChange: (String) -> Unit,
    onNewTaskDescriptionChange: (String) -> Unit,
    onCreateTask: () -> Unit,
    onOpenTask: (Task) -> Unit,
    onCloseTaskDetails: () -> Unit,
    onChangeTaskStatus: (String) -> Unit,
    onDeleteTask: () -> Unit,
    onNewCommentChange: (String) -> Unit,
    onAddComment: () -> Unit
) {
    val title = when (state.screen) {
        ManagerScreen.TaskList -> "Управление задачами"
        ManagerScreen.CreateTask -> "Новая задача"
        ManagerScreen.TaskDetails -> "Детали задачи"
        ManagerScreen.UserList -> "Пользователи"
        ManagerScreen.CreateUser -> "Новый пользователь"
    }

    OperqueScaffold(
        title = title,
        subtitle = state.managerProfile?.email,
        onRefresh = if (
            state.screen == ManagerScreen.TaskList || state.screen == ManagerScreen.UserList
        ) onRefresh else null,
        refreshEnabled = !state.isLoading && !state.isSaving,
        onLogout = onLogout
    ) { modifier ->
        Column(modifier = modifier) {
            state.errorMessage?.let {
                ErrorBanner(it)
                Spacer(Modifier.height(12.dp))
            }

            Box(modifier = Modifier.weight(1f)) {
                when (state.screen) {
                    ManagerScreen.TaskList -> ManagerTaskList(
                        state,
                        onRefresh,
                        onOpenCreateTask,
                        onOpenUsers,
                        onOpenTask
                    )
                    ManagerScreen.UserList -> ManagerUserList(
                        state,
                        onCloseUsers,
                        onOpenCreateUser,
                        onUpdateUserRole,
                        onDeleteUser
                    )
                    ManagerScreen.CreateUser -> ManagerCreateUser(
                        state,
                        onCloseCreateUser,
                        onNewUserEmailChange,
                        onNewUserPasswordChange,
                        onNewUserRoleChange,
                        onCreateUser
                    )
                    ManagerScreen.CreateTask -> ManagerCreateTask(
                        state,
                        onSelectEmployee,
                        onNewTaskTitleChange,
                        onNewTaskDescriptionChange,
                        onCreateTask,
                        onCloseCreateTask
                    )
                    ManagerScreen.TaskDetails -> ManagerTaskDetails(
                        state,
                        onCloseTaskDetails,
                        onChangeTaskStatus,
                        onDeleteTask,
                        onNewCommentChange,
                        onAddComment
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagerTaskList(
    state: ManagerHomeUiState,
    onRefresh: () -> Unit,
    onCreate: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenTask: (Task) -> Unit
) {
    if (state.isLoading) {
        LoadingState("Загружаем задачи команды...")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionHeader(
                title = "Задачи команды",
                supporting = "Задач: ${state.tasks.size}  •  Сотрудников: ${state.employees.size}",
                actionLabel = "Назначить",
                onAction = onCreate
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenUsers,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Управление пользователями")
            }
            Spacer(Modifier.height(6.dp))
        }

        if (state.tasks.isEmpty()) {
            item {
                EmptyState(
                    title = "Задач пока нет",
                    body = "Создайте первую задачу и назначьте ее сотруднику.",
                    actionLabel = if (state.employees.isEmpty()) "Обновить" else "Назначить задачу",
                    onAction = if (state.employees.isEmpty()) onRefresh else onCreate
                )
            }
        } else {
            items(state.tasks, key = { it.id }) { task ->
                ManagerTaskCard(
                    task = task,
                    assignee = state.employeeEmail(task.userId),
                    onClick = { onOpenTask(task) }
                )
            }
        }
    }
}

@Composable
private fun ManagerTaskCard(
    task: Task,
    assignee: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, JiraBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(task.title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                StatusBadge(task.status)
            }
            Spacer(Modifier.height(6.dp))
            Text(assignee, style = MaterialTheme.typography.bodySmall, color = JiraTextSubtle)
        }
    }
}

@Composable
private fun ManagerUserList(
    state: ManagerHomeUiState,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onUpdateRole: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            BackToTasksButton(onClick = onBack)
            Spacer(Modifier.height(6.dp))
            SectionHeader(
                title = "Пользователи",
                supporting = "Аккаунтов: ${state.profiles.size}",
                actionLabel = "Создать",
                onAction = onCreate
            )
            Spacer(Modifier.height(12.dp))
        }

        if (state.profiles.isEmpty()) {
            item { EmptyState("Пользователи не найдены", "Обновите данные и попробуйте снова.") }
        } else {
            items(state.profiles, key = { it.id }) { profile ->
                UserManagementCard(
                    profile = profile,
                    currentProfile = state.managerProfile,
                    isSaving = state.isSaving,
                    onUpdateRole = onUpdateRole,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun UserManagementCard(
    profile: Profile,
    currentProfile: Profile?,
    isSaving: Boolean,
    onUpdateRole: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val isCurrentUser = profile.id == currentProfile?.id
    val roles = if (currentProfile?.role == ProfileRole.Admin) {
        ProfileRole.Manageable
    } else {
        listOf(ProfileRole.User, ProfileRole.Manager)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, JiraBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(profile.email, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(roles) { role ->
                    JiraFilterChip(
                        selected = profile.role == role,
                        onClick = { onUpdateRole(profile.id, role) },
                        enabled = !isSaving && !isCurrentUser && profile.role != role,
                        label = roleLabel(role)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    enabled = !isSaving && !isCurrentUser,
                    border = BorderStroke(1.dp, if (isCurrentUser) JiraBorder else JiraRed)
                ) {
                    Text(
                        if (isCurrentUser) "Текущий аккаунт" else "Удалить",
                        color = if (isCurrentUser) JiraTextSubtle else JiraRed
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "Удалить пользователя?",
            body = "Аккаунт ${profile.email} и связанные данные будут удалены.",
            onConfirm = {
                confirmDelete = false
                onDelete(profile.id)
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun ManagerCreateUser(
    state: ManagerHomeUiState,
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onCreate: () -> Unit
) {
    val roles = if (state.managerProfile?.role == ProfileRole.Admin) {
        ProfileRole.Manageable
    } else {
        listOf(ProfileRole.User, ProfileRole.Manager)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack, enabled = !state.isSaving) { Text("Назад") }
            SectionHeader("Новый пользователь", "Аккаунт будет создан с подтвержденным email.")
        }
        item {
            OutlinedTextField(
                value = state.newUserEmail,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.newUserPassword,
                onValueChange = onPasswordChange,
                label = { Text("Временный пароль") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Роль", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(roles) { role ->
                    JiraFilterChip(
                        selected = state.newUserRole == role,
                        onClick = { onRoleChange(role) },
                        label = roleLabel(role)
                    )
                }
            }
        }
        item {
            Button(
                onClick = onCreate,
                enabled = !state.isSaving &&
                    state.newUserEmail.contains("@") &&
                    state.newUserPassword.length >= 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (state.isSaving) "Создание..." else "Создать пользователя")
            }
        }
    }
}

@Composable
private fun ManagerCreateTask(
    state: ManagerHomeUiState,
    onSelectEmployee: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCreateTask: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack, enabled = !state.isSaving) { Text("Назад") }
            SectionHeader(
                title = "Назначить задачу",
                supporting = "Выберите исполнителя и опишите ожидаемый результат."
            )
        }

        item {
            Text("Исполнитель", style = MaterialTheme.typography.titleMedium)
        }

        if (state.employees.isEmpty()) {
            item {
                EmptyState(
                    title = "Сотрудники не найдены",
                    body = "Для назначения задачи нужен профиль с ролью user."
                )
            }
        } else {
            items(state.employees, key = { it.id }) { employee ->
                EmployeeOption(
                    employee = employee,
                    selected = state.selectedEmployeeId == employee.id,
                    onSelect = { onSelectEmployee(employee.id) }
                )
            }
        }

        item {
            OutlinedTextField(
                value = state.newTaskTitle,
                onValueChange = onTitleChange,
                label = { Text("Название задачи") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.newTaskDescription,
                onValueChange = onDescriptionChange,
                label = { Text("Описание") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onCreateTask,
                enabled = !state.isSaving &&
                    state.selectedEmployeeId != null &&
                    state.newTaskTitle.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (state.isSaving) "Создание..." else "Создать и назначить")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun EmployeeOption(employee: Profile, selected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) JiraBlueLight else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, JiraBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = JiraTextSubtle
                )
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(employee.email, style = MaterialTheme.typography.titleMedium)
                Text("Сотрудник", style = MaterialTheme.typography.bodySmall, color = JiraTextSubtle)
            }
        }
    }
}

@Composable
private fun ManagerTaskDetails(
    state: ManagerHomeUiState,
    onBack: () -> Unit,
    onChangeTaskStatus: (String) -> Unit,
    onDeleteTask: () -> Unit,
    onCommentChange: (String) -> Unit,
    onAddComment: () -> Unit
) {
    val task = state.selectedTask ?: return
    var confirmDelete by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BackToTasksButton(onClick = onBack)
            Spacer(Modifier.height(8.dp))
            Text(task.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(state.employeeEmail(task.userId), color = JiraTextSubtle)
            Spacer(Modifier.height(8.dp))
            StatusBadge(task.status)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, JiraBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Описание", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        task.description?.takeIf { it.isNotBlank() } ?: "Описание не добавлено",
                        color = JiraTextSubtle,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        item {
            Text("Изменить статус", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TaskStatus.All) { status ->
                    JiraFilterChip(
                        selected = task.status == status,
                        onClick = { onChangeTaskStatus(status) },
                        enabled = !state.isSaving,
                        label = statusLabel(status)
                    )
                }
            }
        }

        item { Text("Комментарии", style = MaterialTheme.typography.titleMedium) }

        if (state.isLoadingComments) {
            item { Text("Загрузка комментариев...", color = JiraTextSubtle) }
        } else if (state.comments.isEmpty()) {
            item { Text("Комментариев пока нет", color = JiraTextSubtle) }
        } else {
            items(state.comments, key = { it.id }) { comment -> CommentCard(comment) }
        }

        item {
            OutlinedTextField(
                value = state.newCommentText,
                onValueChange = onCommentChange,
                label = { Text("Комментарий менеджера") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onAddComment,
                enabled = !state.isSaving && state.newCommentText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaving) "Отправка..." else "Отправить комментарий")
            }
            Spacer(Modifier.height(12.dp))
        }

        item {
            OutlinedButton(
                onClick = { confirmDelete = true },
                enabled = !state.isSaving,
                border = BorderStroke(1.dp, JiraRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Удалить задачу", color = JiraRed)
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "Удалить задачу?",
            body = "Задача и все ее комментарии будут удалены.",
            onConfirm = {
                confirmDelete = false
                onDeleteTask()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun BackToTasksButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.width(170.dp)
    ) {
        Text("←  Назад к задачам")
    }
}

private fun ManagerHomeUiState.employeeEmail(userId: String): String {
    return employees.firstOrNull { it.id == userId }?.email ?: userId
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Удалить", color = JiraRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun roleLabel(role: String): String = when (role) {
    ProfileRole.User -> "Сотрудник"
    ProfileRole.Manager -> "Менеджер"
    ProfileRole.Admin -> "Администратор"
    else -> role
}
