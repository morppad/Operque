package com.morppad.operque.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morppad.operque.data.model.Comment
import com.morppad.operque.data.model.Task
import com.morppad.operque.data.model.TaskStatus

@Composable
fun UserHomeRoute(
    onLogout: () -> Unit,
    viewModel: UserHomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

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

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Operque") },
                actions = {
                    TextButton(onClick = onRefresh, enabled = !state.isLoading && !state.isSaving) { Text("Refresh") }
                    TextButton(onClick = onLogout) { Text("Logout") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            when (state.screen) {
                UserScreen.TaskList -> TaskListContent(
                    state = state,
                    onOpenTask = onOpenTask
                )

                UserScreen.TaskDetails -> TaskDetailsContent(
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

@Composable
private fun TaskListContent(
    state: UserHomeUiState,
    onOpenTask: (Task) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("My tasks", style = MaterialTheme.typography.headlineSmall)
        Text(
            state.profile?.email.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(Modifier.height(16.dp))

    if (state.isLoading) {
        CircularProgressIndicator()
        return
    }

    if (state.tasks.isEmpty()) {
        Text("No assigned tasks yet", style = MaterialTheme.typography.bodyLarge)
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(state.tasks, key = { it.id }) { task ->
            TaskListItem(task = task, onClick = { onOpenTask(task) })
        }
    }
}

@Composable
private fun TaskListItem(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                AssistChip(onClick = onClick, label = { Text(task.status.label()) })
            }
            task.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun TaskDetailsContent(
    state: UserHomeUiState,
    onBack: () -> Unit,
    onChangeTaskStatus: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onAddComment: () -> Unit
) {
    val task = state.selectedTask ?: return

    TextButton(onClick = onBack) { Text("Back") }
    Text(task.title, style = MaterialTheme.typography.headlineSmall)
    task.description?.takeIf { it.isNotBlank() }?.let {
        Spacer(Modifier.height(8.dp))
        Text(it, style = MaterialTheme.typography.bodyLarge)
    }

    Spacer(Modifier.height(16.dp))
    Text("Status", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskStatus.All.forEach { status ->
            FilterChip(
                selected = task.status == status,
                onClick = { onChangeTaskStatus(status) },
                enabled = !state.isSaving,
                label = { Text(status.label()) }
            )
        }
    }

    Spacer(Modifier.height(20.dp))
    Text("Comments", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))

    if (state.isLoadingComments) {
        CircularProgressIndicator()
    } else if (state.comments.isEmpty()) {
        Text("No comments yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.comments, key = { it.id }) { comment ->
                CommentItem(comment)
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = state.newCommentText,
        onValueChange = onCommentChange,
        label = { Text("Comment") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))
    Button(onClick = onAddComment, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
        Text(if (state.isSaving) "Sending..." else "Send comment")
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun String.label(): String = when (this) {
    TaskStatus.Todo -> "To do"
    TaskStatus.InProgress -> "In progress"
    TaskStatus.Done -> "Done"
    else -> this
}
