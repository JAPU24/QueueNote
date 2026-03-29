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
import com.adrian.queuenote.AppStrings
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
    const val INVENTORY = "inventory"
}

@Composable
fun AppNav(
    themeMode: String,
    onSetThemeMode: (String) -> Unit,
    language: String,
    onSetLanguage: (String) -> Unit,
    appStrings: AppStrings
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val authRepository = remember { AuthRepository() }
    val firestoreRepository = remember { FirestoreRepository() }
    val storageRepository = remember { StorageRepository() }
    
    var isLoading by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(authRepository.currentUser) }
    
    DisposableEffect(authRepository) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    
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
            SplashScreen(appStrings = appStrings, onContinue = { navController.navigate(Routes.LOGIN) })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                appStrings = appStrings,
                isLoading = isLoading,
                onLogin = { e, p ->
                    scope.launch {
                        isLoading = true
                        val res = authRepository.login(e, p)
                        isLoading = false
                        if (res.isSuccess) {
                            navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                        } else {
                            Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
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
                            val res = authRepository.loginWithGitHub(activity)
                            isLoading = false
                            if (res.isSuccess) {
                                navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                            }
                        }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                appStrings = appStrings,
                isLoading = isLoading,
                onBackToLogin = { navController.popBackStack() },
                onRegisterSuccess = { e, p ->
                    scope.launch {
                        isLoading = true
                        val res = authRepository.register(e, p)
                        isLoading = false
                        if (res.isSuccess) {
                            navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                        }
                    }
                }
            )
        }

        composable(Routes.FORGOT) {
            ForgotPasswordScreen(
                appStrings = appStrings,
                isLoading = isLoading,
                onBack = { navController.popBackStack() },
                onSendReset = { e ->
                    scope.launch {
                        isLoading = true
                        val res = authRepository.sendPasswordResetEmail(e)
                        isLoading = false
                        if (res.isSuccess) {
                            Toast.makeText(context, "Correo enviado.", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
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
                            val next = when (item.status) {
                                ProcessStatus.PENDIENTE -> ProcessStatus.EN_ESPERA
                                ProcessStatus.EN_ESPERA -> ProcessStatus.COMPLETADO
                                ProcessStatus.COMPLETADO -> ProcessStatus.PENDIENTE
                            }
                            scope.launch { firestoreRepository.saveProcess(item.copy(status = next)) }
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
                    onGoInventory = { navController.navigate(Routes.INVENTORY) },
                    onLogout = { 
                        authRepository.logout()
                        navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                    },
                    appStrings = appStrings
                )
            }
        }

        composable(Routes.INVENTORY) {
            InventoryScreen(
                onGoHome = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                onGoProfile = { navController.navigate(Routes.PROFILE) },
                onGoSettings = { navController.navigate(Routes.SETTINGS) },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                appStrings = appStrings
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                isLoading = isLoading,
                initialName = currentUser?.displayName ?: "Usuario",
                email = currentUser?.email ?: "Sin correo",
                photoUrl = currentUser?.photoUrl?.toString(),
                onUpdateProfile = { n ->
                    scope.launch {
                        isLoading = true
                        authRepository.updateProfile(displayName = n)
                        isLoading = false
                    }
                },
                onPhotoSelected = { uri ->
                    scope.launch {
                        isLoading = true
                        val res = storageRepository.uploadProfilePicture(currentUser!!.uid, uri)
                        if (res.isSuccess) {
                            authRepository.updateProfile(photoUrl = res.getOrNull()!!)
                        }
                        isLoading = false
                    }
                },
                onLogout = {
                    authRepository.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                },
                onChangePassword = { navController.navigate(Routes.CHANGE_PASSWORD) },
                onBack = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                appStrings = appStrings
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                isLoading = isLoading,
                onBack = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                onUpdatePassword = { current, new ->
                    scope.launch {
                        isLoading = true
                        val res = authRepository.changePassword(current, new)
                        isLoading = false
                        if (res.isSuccess) {
                            Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                            navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                        } else {
                            Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                appStrings = appStrings
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                themeMode = themeMode,
                onSetThemeMode = onSetThemeMode,
                language = language,
                onSetLanguage = onSetLanguage,
                onBack = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } },
                appStrings = appStrings
            )
        }

        composable(
            route = "${Routes.CREATE_EDIT}?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val existing = processes.find { it.id == id }
            CreateEditProcessScreen(
                existing = existing,
                onSave = { newItem ->
                    if (currentUser != null) {
                        scope.launch { firestoreRepository.saveProcess(newItem.copy(userId = currentUser!!.uid)) }
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() },
                appStrings = appStrings
            )
        }

        composable("${Routes.DETAIL}/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val item = processes.find { it.id == id }
            if (item != null) {
                TaskDetailScreen(
                    process = item,
                    onBack = { navController.popBackStack() },
                    onUpdateProcess = { updated -> scope.launch { firestoreRepository.saveProcess(updated) } },
                    appStrings = appStrings
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
