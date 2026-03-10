package com.adrian.queuenote.ui.theme

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.adrian.queuenote.AuthRepository
import com.adrian.queuenote.FirestoreRepository
import com.adrian.queuenote.StorageRepository
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
    val context = LocalContext.current
    
    // Repositorios
    val authRepository = remember { AuthRepository() }
    val firestoreRepository = remember { FirestoreRepository() }
    val storageRepository = remember { StorageRepository() }
    
    // Estados de la app
    var isLoading by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(authRepository.currentUser) }
    
    // Escuchar cambios de autenticación
    DisposableEffect(authRepository) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }
    
    // Observamos los procesos
    val processes by remember(currentUser) {
        if (currentUser != null) {
            firestoreRepository.getProcessesFlow(currentUser!!.uid)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())
    
    var selectedFilter by remember { mutableStateOf<ProcessStatus?>(null) }

    NavHost(
        navController = navController,
        startDestination = if (authRepository.currentUser != null) Routes.HOME else Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onContinue = { navController.navigate(Routes.LOGIN) })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                isLoading = isLoading,
                onLogin = { emailInput, passInput ->
                    scope.launch {
                        isLoading = true
                        val result = authRepository.login(emailInput, passInput)
                        isLoading = false
                        if (result.isSuccess) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onGoRegister = { navController.navigate(Routes.REGISTER) },
                onForgot = { navController.navigate(Routes.FORGOT) },
                onLoginWithGitHub = {
                    scope.launch {
                        val activity = context as? Activity
                        if (activity != null) {
                            isLoading = true
                            val result = authRepository.loginWithGitHub(activity)
                            isLoading = false
                            if (result.isSuccess) {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                Toast.makeText(context, "Error GitHub: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                isLoading = isLoading,
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = { email, pass ->
                    scope.launch {
                        isLoading = true
                        val result = authRepository.register(email, pass)
                        isLoading = false
                        if (result.isSuccess) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        composable(Routes.FORGOT) {
            ForgotPasswordScreen(
                isLoading = isLoading,
                onBack = { navController.popBackStack() },
                onSendReset = { email ->
                    scope.launch {
                        isLoading = true
                        val result = authRepository.sendPasswordResetEmail(email)
                        isLoading = false
                        if (result.isSuccess) {
                            Toast.makeText(context, "Correo enviado. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            if (currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Routes.LOGIN) }
            } else {
                HomeScreenWithDrawer(
                    processes = if (selectedFilter == null) processes else processes.filter { it.status == selectedFilter },
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    onAdd = { navController.navigate(Routes.CREATE_EDIT) },
                    onToggleStatus = { id ->
                        processes.find { it.id == id }?.let { item ->
                            val nextStatus = when (item.status) {
                                ProcessStatus.PENDIENTE -> ProcessStatus.EN_ESPERA
                                ProcessStatus.EN_ESPERA -> ProcessStatus.COMPLETADO
                                ProcessStatus.COMPLETADO -> ProcessStatus.PENDIENTE
                            }
                            scope.launch { firestoreRepository.saveProcess(item.copy(status = nextStatus)) }
                        }
                    },
                    onDelete = { id -> scope.launch { firestoreRepository.deleteProcess(id) } },
                    onEdit = { id -> navController.navigate("${Routes.CREATE_EDIT}?id=$id") },
                    onOpenDetail = { id -> navController.navigate("${Routes.DETAIL}/$id") },
                    onReopen = { id ->
                        processes.find { it.id == id }?.let { item ->
                            scope.launch { firestoreRepository.saveProcess(item.copy(status = ProcessStatus.PENDIENTE)) }
                        }
                    },
                    onGoProfile = { navController.navigate(Routes.PROFILE) },
                    onGoSettings = { navController.navigate(Routes.SETTINGS) },
                    onLogout = { 
                        authRepository.logout()
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                isLoading = isLoading,
                email = currentUser?.email ?: "Sin correo",
                photoUrl = currentUser?.photoUrl?.toString(),
                onPhotoSelected = { uri ->
                    scope.launch {
                        isLoading = true
                        val uploadResult = storageRepository.uploadProfilePicture(currentUser!!.uid, uri)
                        if (uploadResult.isSuccess) {
                            val url = uploadResult.getOrNull()!!
                            authRepository.updateProfilePicture(url)
                            Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error al subir foto", Toast.LENGTH_SHORT).show()
                        }
                        isLoading = false
                    }
                },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                isLoading = isLoading,
                onBack = { navController.popBackStack() },
                onUpdatePassword = { newPass ->
                    scope.launch {
                        isLoading = true
                        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                        user?.updatePassword(newPass)?.addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
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
                    if (currentUser != null) {
                        scope.launch {
                            firestoreRepository.saveProcess(newItem.copy(userId = currentUser!!.uid))
                        }
                        navController.popBackStack()
                    }
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
                            firestoreRepository.saveProcess(updated)
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
