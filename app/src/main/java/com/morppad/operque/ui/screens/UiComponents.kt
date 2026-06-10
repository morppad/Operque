package com.morppad.operque.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.morppad.operque.data.model.Task
import com.morppad.operque.data.model.TaskStatus
import com.morppad.operque.ui.theme.JiraBlueLight
import com.morppad.operque.ui.theme.JiraBorder
import com.morppad.operque.ui.theme.JiraGreen
import com.morppad.operque.ui.theme.JiraGreenLight
import com.morppad.operque.ui.theme.JiraRed
import com.morppad.operque.ui.theme.JiraRedLight
import com.morppad.operque.ui.theme.JiraTextSubtle
import com.morppad.operque.ui.theme.JiraYellow
import com.morppad.operque.ui.theme.JiraYellowLight

@Composable
fun JiraFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = { Text(label) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = JiraTextSubtle,
            selectedContainerColor = JiraBlueLight,
            selectedLabelColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledLabelColor = JiraTextSubtle.copy(alpha = 0.65f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = JiraBorder,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            disabledBorderColor = JiraBorder.copy(alpha = 0.6f),
            disabledSelectedBorderColor = JiraBorder.copy(alpha = 0.6f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperqueScaffold(
    title: String,
    subtitle: String? = null,
    onRefresh: (() -> Unit)? = null,
    refreshEnabled: Boolean = true,
    onLogout: (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    Column {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        subtitle?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    onRefresh?.let {
                        TextButton(onClick = it, enabled = refreshEnabled) { Text("Обновить") }
                    }
                    onLogout?.let {
                        TextButton(onClick = it) { Text("Выйти") }
                    }
                }
            )
        }
    ) { padding ->
        content(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(JiraRedLight, MaterialTheme.shapes.medium)
            .border(1.dp, JiraRed.copy(alpha = 0.35f), MaterialTheme.shapes.medium)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("!", color = JiraRed, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Text(message, color = JiraRed, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LoadingState(label: String = "Загрузка...") {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(strokeWidth = 3.dp)
            Spacer(Modifier.height(12.dp))
            Text(label, color = JiraTextSubtle)
        }
    }
}

@Composable
fun EmptyState(title: String, body: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, JiraBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = JiraTextSubtle)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, supporting: String? = null, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            supporting?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = JiraTextSubtle)
            }
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.width(12.dp))
            Button(onClick = onAction, contentPadding = ButtonDefaults.ContentPadding) { Text(actionLabel) }
        }
    }
}

@Composable
fun TaskCard(task: Task, assignee: String? = null, onClick: () -> Unit) {
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
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    task.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(10.dp))
                StatusBadge(task.status)
            }
            assignee?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = JiraTextSubtle)
            }
            task.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JiraTextSubtle,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (background, foreground) = when (status) {
        TaskStatus.InProgress -> JiraBlueLight to MaterialTheme.colorScheme.primary
        TaskStatus.Done -> JiraGreenLight to JiraGreen
        TaskStatus.Todo -> JiraYellowLight to JiraYellow
        else -> MaterialTheme.colorScheme.surfaceVariant to JiraTextSubtle
    }
    Text(
        text = statusLabel(status),
        modifier = Modifier
            .background(background, MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = foreground,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold
    )
}

fun statusLabel(status: String): String = when (status) {
    TaskStatus.Todo -> "К выполнению"
    TaskStatus.InProgress -> "В работе"
    TaskStatus.Done -> "Готово"
    else -> status
}
