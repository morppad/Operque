package com.morppad.operque.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morppad.operque.data.model.Comment
import com.morppad.operque.data.model.Task
import com.morppad.operque.data.model.TaskStatus
import com.morppad.operque.ui.theme.JiraTextSubtle
import com.morppad.operque.ui.theme.JiraBorder

@Composable
fun UserHomeRoute(
    onLogout: () -> Unit,
    viewModel: UserHomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    BackHandler(enabled = state.screen == UserScreen.TaskDetails) {
        viewModel.closeTaskDetails()
    }

    UserHomeScreen(
        state = state,
        onLogout = onLogout,
        onRefresh = viewModel::refresh,
        onOpenTask = viewModel::openTask,
        onCloseTaskDetails = viewModel::closeTaskDetails,
        onChangeTaskStatus = viewModel::changeSelectedTaskStatus,
        onNewCommentChange = viewModel::onNewCommentChange,
        onAddComment = viewModel::addComment
    )
}

@Composable
fun UserHomeScreen(
    state: UserHomeUiState,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onOpenTask: (Task) -> Unit,
    onCloseTaskDetails: () -> Unit,
    onChangeTaskStatus: (String) -> Unit,
    onNewCommentChange: (String) -> Unit,
    onAddComment: () -> Unit
) {
    OperqueScaffold(
        title = if (state.screen == UserScreen.TaskList) "Мои задачи" else "Задача",
        subtitle = state.profile?.email,
        onRefresh = if (state.screen == UserScreen.TaskList) onRefresh else null,
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
                    UserScreen.TaskList -> UserTaskList(state, onRefresh, onOpenTask)
                    UserScreen.TaskDetails -> UserTaskDetails(
                        state = state,
                        onBack = onCloseTaskDetails,
                        onChangeTaskStatus = onChangeTaskStatus,
                        onCommentChange = onNewCommentChange,
                        onAddComment = onAddComment
                    )
                }
            }
        }
    }
}

@Composable
private fun UserTaskList(state: UserHomeUiState, onRefresh: () -> Unit, onOpenTask: (Task) -> Unit) {
    if (state.isLoading) {
        LoadingState("Загружаем назначенные задачи...")
        return
    }

    if (state.tasks.isEmpty()) {
        EmptyState(
            title = "Назначенных задач пока нет",
            body = "Новые задачи, назначенные менеджером, появятся здесь.",
            actionLabel = "Обновить",
            onAction = onRefresh
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionHeader(
                title = "Текущая работа",
                supporting = "Задач: ${state.tasks.size}"
            )
            Spacer(Modifier.height(12.dp))
        }
        items(state.tasks, key = { it.id }) { task ->
            TaskCard(task = task, onClick = { onOpenTask(task) })
        }
    }
}

@Composable
private fun UserTaskDetails(
    state: UserHomeUiState,
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
            Spacer(Modifier.height(6.dp))
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

        item {
            Text("Комментарии", style = MaterialTheme.typography.titleMedium)
        }

        if (state.isLoadingComments) {
            item { Text("Загрузка комментариев...", color = JiraTextSubtle) }
        } else if (state.comments.isEmpty()) {
            item { Text("Комментариев пока нет", color = JiraTextSubtle) }
        } else {
            itemsIndexed(state.comments, key = { _, comment -> comment.id }) { _, comment ->
                CommentCard(comment)
            }
        }

        item {
            OutlinedTextField(
                value = state.newCommentText,
                onValueChange = onCommentChange,
                label = { Text("Новый комментарий") },
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

@Composable
fun CommentCard(comment: Comment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, JiraBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
