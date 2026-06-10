package com.morppad.operque.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morppad.operque.ui.theme.JiraBlueLight
import com.morppad.operque.ui.theme.JiraTextSubtle

@Composable
fun AuthRoute(onAuthorized: () -> Unit, viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.isAuthorized) { if (state.isAuthorized) onAuthorized() }

    AuthScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordRepeatChange = viewModel::onPasswordRepeatChange,
        onModeChange = viewModel::switchMode,
        onSubmit = viewModel::submit,
        onRegistrationMessageShown = viewModel::clearRegistrationCompleted
    )
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordRepeatChange: (String) -> Unit,
    onModeChange: (AuthMode) -> Unit,
    onSubmit: () -> Unit,
    onRegistrationMessageShown: () -> Unit
) {
    LaunchedEffect(state.registrationCompleted) {
        if (state.registrationCompleted) onRegistrationMessageShown()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "OPERQUE",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (state.mode == AuthMode.Login) "Вход в рабочее пространство" else "Создание аккаунта",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Управляйте задачами и оставайтесь в курсе работы команды.",
                style = MaterialTheme.typography.bodyLarge,
                color = JiraTextSubtle
            )
        }

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JiraBlueLight, MaterialTheme.shapes.medium)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            JiraFilterChip(
                selected = state.mode == AuthMode.Login,
                onClick = { onModeChange(AuthMode.Login) },
                label = "Вход",
                modifier = Modifier.weight(1f)
            )
            JiraFilterChip(
                selected = state.mode == AuthMode.Register,
                onClick = { onModeChange(AuthMode.Register) },
                label = "Регистрация",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        if (state.mode == AuthMode.Register) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.passwordRepeat,
                onValueChange = onPasswordRepeatChange,
                label = { Text("Повторите пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!state.isConfigured) {
            Spacer(Modifier.height(14.dp))
            ErrorBanner("Добавьте SUPABASE_URL и SUPABASE_ANON_KEY в local.properties.")
        } else {
            state.errorMessage?.let {
                Spacer(Modifier.height(14.dp))
                ErrorBanner(it)
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSubmit,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (state.mode == AuthMode.Login) "Войти" else "Зарегистрироваться")
            }
        }
    }
}
