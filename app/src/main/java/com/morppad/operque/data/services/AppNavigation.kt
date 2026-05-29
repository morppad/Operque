package com.morppad.operque.data.services

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.morppad.operque.ui.screens.AuthRoute
import com.morppad.operque.ui.screens.AuthViewModel
import com.morppad.operque.ui.screens.HomeScreen

private object Routes {
    const val Auth = "auth"
    const val Home = "home"
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Auth) {
        composable(Routes.Auth) {
            AuthRoute(viewModel = authViewModel, onAuthorized = {
                navController.navigate(Routes.Home) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
        composable(Routes.Home) {
            HomeScreen(onLogout = {
                authViewModel.signOut()
                navController.navigate(Routes.Auth) {
                    popUpTo(Routes.Home) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
    }
}
