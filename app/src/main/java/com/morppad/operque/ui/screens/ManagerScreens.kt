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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morppad.operque.data.model.Profile
import com.morppad.operque.data.model.Task
import com.morppad.operque.data.model.TaskStatus
import com.morppad.operque.ui.theme.JiraBlueLight
import com.morppad.operque.ui.theme.JiraBorder
import com.morppad.operque.ui.theme.JiraTextSubtle

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
            ManagerScreen.TaskList -> Unit
        }
    }

    ManagerHomeScreen(
        state = state,
        onLogout = onLogout,
        onRefresh = viewModel::refresh,
        onOpenCreateTask = viewModel::openCreateTask,
        onCloseCreateTask = viewModel::closeCreateTask,
        onSelectEmployee = viewModel::selectEmployee,
        onNewTaskTitleChange = viewModel::onNewTaskTitleChange,
        onNewTaskDescriptionChange = viewModel::onNewTaskDescriptionChange,
        onCreateTask = viewModel::createTask,
        onOpenTask = viewModel::openTask,
        onCloseTaskDetails = viewModel::closeTaskDetails,
        onChangeTaskStatus = viewModel::changeSelectedTaskStatus,
        onNewCommentChange = viewModel::onNewCommentChange,
        onAddComment = viewModel::addComment
    )
}

@Composable
fun ManagerHomeScreen(
    state: ManagerHomeUiState,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onOpenCreateTask: () -> Unit,
    onCloseCreateTask: () -> Unit,
    onSelectEmployee: (String) -> Unit,
    onNewTaskTitleChange: (String) -> Unit,
    onNewTaskDescriptionChange: (String) -> Unit,
    onCreateTask: () -> Unit,
    onOpenTask: (Task) -> Unit,
    onCloseTaskDetails: () -> Unit,
    onChangeTaskStatus: (String) -> Unit,
    onNewCommentChange: (String) -> Unit,
    onAddComment: () -> Unit
) {
    val title = when (state.screen) {
        ManagerScreen.TaskList -> "Управление задачами"
        ManagerScreen.CreateTask -> "Новая задача"
        ManagerScreen.TaskDetails -> "Детали задачи"
    }

    OperqueScaffold(
        title = title,
        subtitle = state.managerProfile?.email,
        onRefresh = if (state.screen == ManagerScreen.TaskList) onRefresh else null,
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
                    ManagerScreen.TaskList -> ManagerTaskList(state, onRefresh, onOpenCreateTask, onOpenTask)
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
            Spacer(Modifier.height(12.dp))
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
                TaskCard(
                    task = task,
                    assignee = state.employeeEmail(task.userId),
                    onClick = { onOpenTask(task) }
                )
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
    onCommentChange: (String) -> Unit,
    onAddComment: () -> Unit
) {
    val task = state.selectedTask ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onBack) { Text("Назад к задачам") }
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
    }
}

private fun ManagerHomeUiState.employeeEmail(userId: String): String {
    return employees.firstOrNull { it.id == userId }?.email ?: userId
}
