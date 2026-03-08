package com.adrian.queuenote.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adrian.queuenote.FirestoreRepository
import com.adrian.queuenote.ProcessItem
import com.adrian.queuenote.ProcessStatus
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"
    const val HOME = "home"
    const val CREATE_EDIT = "create_edit"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val CHANGE_PASSWORD = "change_password"
    const val DETAIL = "detail"
}

@Composable
fun AppNav(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    
    // Instancia del repositorio de Firestore
    val repository = remember { FirestoreRepository() }
    
    // Observamos los procesos desde Firebase en tiempo real
    val processes by repository.getProcessesFlow().collectAsState(initial = emptyList())
    
    var selectedFilter by remember { mutableStateOf<ProcessStatus?>(null) }

    fun filteredList(): List<ProcessItem> {
        return if (selectedFilter == null) processes else processes.filter { it.status == selectedFilter }
    }

    fun nextStatus(status: ProcessStatus): ProcessStatus {
        return when (status) {
            ProcessStatus.PENDIENTE -> ProcessStatus.EN_ESPERA
            ProcessStatus.EN_ESPERA -> ProcessStatus.COMPLETADO
            ProcessStatus.COMPLETADO -> ProcessStatus.PENDIENTE
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onContinue = { navController.navigate(Routes.LOGIN) })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { 
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate(Routes.REGISTER) },
                onForgot = { navController.navigate(Routes.FORGOT) },
                onLoginWithGitHub = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.HOME) {
            HomeScreenWithDrawer(
                processes = filteredList(),
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                onAdd = { navController.navigate(Routes.CREATE_EDIT) },
                onToggleStatus = { id ->
                    processes.find { it.id == id }?.let { item ->
                        scope.launch {
                            repository.saveProcess(item.copy(status = nextStatus(item.status)))
                        }
                    }
                },
                onDelete = { id ->
                    scope.launch {
                        repository.deleteProcess(id)
                    }
                },
                onEdit = { id ->
                    navController.navigate("${Routes.CREATE_EDIT}?id=$id")
                },
                onOpenDetail = { id ->
                    navController.navigate("${Routes.DETAIL}/$id")
                },
                onReopen = { id ->
                    processes.find { it.id == id }?.let { item ->
                        scope.launch {
                            repository.saveProcess(item.copy(status = ProcessStatus.PENDIENTE))
                        }
                    }
                },
                onGoProfile = { navController.navigate(Routes.PROFILE) },
                onGoSettings = { navController.navigate(Routes.SETTINGS) },
                onLogout = { 
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.CREATE_EDIT}?id={id}",
            arguments = listOf(navArgument("id") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null 
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val existing = processes.find { it.id == id }

            CreateEditProcessScreen(
                existing = existing,
                onSave = { newItem ->
                    scope.launch {
                        repository.saveProcess(newItem)
                    }
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("${Routes.DETAIL}/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val item = processes.find { it.id == id }

            if (item != null) {
                TaskDetailScreen(
                    process = item,
                    onBack = { navController.popBackStack() },
                    onUpdateProcess = { updated ->
                        scope.launch {
                            repository.saveProcess(updated)
                        }
                    }
                )
            } else {
                SimpleNotFoundScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun SimpleNotFoundScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Proceso no encontrado")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBack) { Text("Volver") }
    }
}
