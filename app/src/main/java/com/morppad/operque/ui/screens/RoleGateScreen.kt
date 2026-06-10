package com.morppad.operque.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morppad.operque.ui.theme.JiraTextSubtle

@Composable
fun RoleGateRoute(
    onUser: () -> Unit,
    onManager: () -> Unit,
    viewModel: RoleGateViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.destination) {
        when (state.destination) {
            RoleDestination.User -> onUser()
            RoleDestination.Manager -> onManager()
            null -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "OPERQUE",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        if (state.isLoading) {
            CircularProgressIndicator(strokeWidth = 3.dp)
            Spacer(Modifier.height(14.dp))
            Text("Подготавливаем рабочее пространство...", color = JiraTextSubtle)
        }
        state.errorMessage?.let {
            ErrorBanner(it)
            Spacer(Modifier.height(14.dp))
            Button(onClick = viewModel::loadRole, modifier = Modifier.fillMaxWidth()) {
                Text("Повторить")
            }
        }
    }
}
