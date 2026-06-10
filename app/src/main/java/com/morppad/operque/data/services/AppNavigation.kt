package com.morppad.operque.data.services

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.morppad.operque.ui.screens.AuthRoute
import com.morppad.operque.ui.screens.AuthViewModel
import com.morppad.operque.ui.screens.ManagerHomeRoute
import com.morppad.operque.ui.screens.RoleGateRoute
import com.morppad.operque.ui.screens.UserHomeRoute

private object Routes {
    const val Auth = "auth"
    const val RoleGate = "role_gate"
    const val UserHome = "user_home"
    const val ManagerHome = "manager_home"
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Auth) {
        composable(Routes.Auth) {
            AuthRoute(viewModel = authViewModel, onAuthorized = {
                navController.navigate(Routes.RoleGate) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }
        composable(Routes.RoleGate) {
            RoleGateRoute(
                onUser = {
                    navController.navigate(Routes.UserHome) {
                        popUpTo(Routes.RoleGate) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onManager = {
                    navController.navigate(Routes.ManagerHome) {
                        popUpTo(Routes.RoleGate) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.UserHome) {
            UserHomeRoute(onLogout = {
                authViewModel.signOut {
                    navController.navigate(Routes.Auth) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            })
        }
        composable(Routes.ManagerHome) {
            ManagerHomeRoute(onLogout = {
                authViewModel.signOut {
                    navController.navigate(Routes.Auth) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            })
        }
    }
}
