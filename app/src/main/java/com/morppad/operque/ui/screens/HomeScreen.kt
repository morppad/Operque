package com.morppad.operque.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Авторизация выполнена", style = MaterialTheme.typography.headlineMedium)
        Text("Следующий этап: экран задач и подключение таблиц из Supabase.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        Button(onClick = onLogout) { Text("Выйти") }
    }
}
