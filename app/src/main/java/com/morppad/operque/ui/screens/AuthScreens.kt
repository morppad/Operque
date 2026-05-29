package com.morppad.operque.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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
    LaunchedEffect(state.registrationCompleted) { if (state.registrationCompleted) onRegistrationMessageShown() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).imePadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(if (state.mode == AuthMode.Login) "Вход" else "Регистрация", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text("Operque: авторизация через Supabase", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onModeChange(AuthMode.Login) }, enabled = state.mode != AuthMode.Login, modifier = Modifier.weight(1f)) { Text("Вход") }
                    OutlinedButton(onClick = { onModeChange(AuthMode.Register) }, enabled = state.mode != AuthMode.Register, modifier = Modifier.weight(1f)) { Text("Регистрация") }
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(state.email, onEmailChange, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(state.password, onPasswordChange, label = { Text("Пароль") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

                if (state.mode == AuthMode.Register) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(state.passwordRepeat, onPasswordRepeatChange, label = { Text("Повторите пароль") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                }

                state.errorMessage?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                if (!state.isConfigured) {
                    Spacer(Modifier.height(12.dp))
                    Text("Добавьте SUPABASE_URL и SUPABASE_ANON_KEY в local.properties.", color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(20.dp))
                Button(onClick = onSubmit, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
                    if (state.isLoading) CircularProgressIndicator() else Text(if (state.mode == AuthMode.Login) "Войти" else "Зарегистрироваться")
                }
                TextButton(onClick = { onModeChange(if (state.mode == AuthMode.Login) AuthMode.Register else AuthMode.Login) }) {
                    Text(if (state.mode == AuthMode.Login) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти")
                }
            }
        }
    }
}
