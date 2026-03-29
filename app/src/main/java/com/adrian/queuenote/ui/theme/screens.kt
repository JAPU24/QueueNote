package com.adrian.queuenote.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.adrian.queuenote.ProcessItem
import com.adrian.queuenote.ProcessStatus
import com.adrian.queuenote.SubTask
import com.adrian.queuenote.TaskGroup
import com.adrian.queuenote.AppStrings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- HELPERS (STRICTLY NO LOGIC CHANGES) ---

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun getStatusDisplayName(status: ProcessStatus, appStrings: AppStrings): String {
    return when (status) {
        ProcessStatus.PENDIENTE -> appStrings.filter_pending
        ProcessStatus.EN_ESPERA -> appStrings.filter_waiting
        ProcessStatus.COMPLETADO -> appStrings.filter_completed
    }
}

fun formatLongDate(timestamp: Long?, appStrings: AppStrings): String {
    if (timestamp == null) return appStrings.no_due_date
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// --- SHARED DESIGN UI (INTERNAL TO SCREENS) ---

@Composable
fun LoadingOverlay(appStrings: AppStrings) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(appStrings.loading, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            isError = isError,
            enabled = enabled,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine
        )
    }
}

@Composable
fun StatusBadgePremium(status: ProcessStatus, appStrings: AppStrings) {
    val color = when(status) {
        ProcessStatus.PENDIENTE -> WarningOrange
        ProcessStatus.EN_ESPERA -> PrimaryBlue
        ProcessStatus.COMPLETADO -> SuccessGreen
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = CircleShape,
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = getStatusDisplayName(status, appStrings),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
            color = color
        )
    }
}

@Composable
fun PremiumFilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// --- SCREENS ---

@Composable
fun SplashScreen(appStrings: AppStrings, onContinue: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Queue, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.White)
                    }
                }
                Spacer(Modifier.height(32.dp))
                Text(appStrings.app_name, style = MaterialTheme.typography.displayLarge)
                Text(appStrings.splash_subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(appStrings.continue_btn, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
            }
        }
    }
}

@Composable
fun LoginScreen(
    appStrings: AppStrings,
    isLoading: Boolean = false,
    onLogin: (String, String) -> Unit,
    onGoRegister: () -> Unit,
    onForgot: () -> Unit,
    onLoginWithGitHub: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    if (isLoading) LoadingOverlay(appStrings)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(60.dp))
            Text(appStrings.login_title, style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(40.dp))

            PremiumTextField(
                value = email,
                onValueChange = { email = it },
                label = appStrings.email_label,
                isError = email.isNotBlank() && !isValidEmail(email),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
            )
            Spacer(Modifier.height(16.dp))

            PremiumTextField(
                value = password,
                onValueChange = { password = it },
                label = appStrings.password_label,
                modifier = Modifier.focusRequester(passwordFocusRequester),
                enabled = !isLoading,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onForgot, enabled = !isLoading) { 
                    Text(appStrings.forgot_pass_link, style = MaterialTheme.typography.labelMedium) 
                }
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { focusManager.clearFocus(); onLogin(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = email.isNotBlank() && password.length >= 6 && !isLoading,
                shape = MaterialTheme.shapes.medium
            ) { Text(appStrings.login_btn, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp)) }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onLoginWithGitHub,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(appStrings.login_github, style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp))
                }
            }

            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onGoRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(appStrings.create_account_btn, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun RegisterScreen(
    appStrings: AppStrings,
    isLoading: Boolean = false,
    onBackToLogin: () -> Unit,
    onRegisterSuccess: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var isPrivate by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val emailFocus = remember { FocusRequester() }
    val passFocus = remember { FocusRequester() }
    val pass2Focus = remember { FocusRequester() }

    if (isLoading) LoadingOverlay(appStrings)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(40.dp))
            Text(appStrings.register_title, style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(32.dp))

            PremiumTextField(
                value = name, onValueChange = { name = it }, label = appStrings.name_label,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() })
            )
            Spacer(Modifier.height(16.dp))
            PremiumTextField(
                value = email, onValueChange = { email = it }, label = appStrings.email_label,
                modifier = Modifier.focusRequester(emailFocus),
                isError = email.isNotBlank() && !isValidEmail(email),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passFocus.requestFocus() })
            )
            Spacer(Modifier.height(16.dp))
            PremiumTextField(
                value = pass, onValueChange = { pass = it }, label = appStrings.password_label,
                modifier = Modifier.focusRequester(passFocus),
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = { showPass = !showPass }) { Icon(if(showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { pass2Focus.requestFocus() })
            )
            Spacer(Modifier.height(16.dp))
            PremiumTextField(
                value = pass2, onValueChange = { pass2 = it }, label = appStrings.confirm_password_label,
                modifier = Modifier.focusRequester(pass2Focus),
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                isError = pass2.isNotBlank() && pass != pass2,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(Modifier.height(24.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(appStrings.account_type, style = MaterialTheme.typography.titleMedium)
                        Text(if (isPrivate) appStrings.private_label else appStrings.public_label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isPrivate, onCheckedChange = { isPrivate = it }, enabled = !isLoading)
                }
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { onRegisterSuccess(email, pass) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = name.isNotBlank() && isValidEmail(email) && pass.length >= 6 && pass == pass2,
                shape = MaterialTheme.shapes.medium
            ) { Text(appStrings.register_title, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp)) }
            
            TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) { Text(appStrings.back_to_login) }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    appStrings: AppStrings,
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onSendReset: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    if (isLoading) LoadingOverlay(appStrings)

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Spacer(Modifier.height(40.dp))
            Text(appStrings.forgot_title, style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text(appStrings.forgot_desc, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(48.dp))
            
            PremiumTextField(value = email, onValueChange = { email = it }, label = appStrings.email_label)
            
            Spacer(Modifier.height(40.dp))
            Button(onClick = { onSendReset(email) }, modifier = Modifier.fillMaxWidth().height(60.dp), shape = MaterialTheme.shapes.medium) { Text(appStrings.send_link) }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(appStrings.back) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenWithDrawer(
    processes: List<ProcessItem>,
    selectedFilter: ProcessStatus?,
    onFilterChange: (ProcessStatus?) -> Unit,
    onAdd: () -> Unit,
    onToggleStatus: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onReopen: (String) -> Unit,
    onGoProfile: () -> Unit,
    onGoSettings: () -> Unit,
    onGoInventory: () -> Unit,
    onLogout: () -> Unit,
    appStrings: AppStrings
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
                drawerShape = MaterialTheme.shapes.large
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Text(appStrings.app_name, style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp))
                }
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text(appStrings.home_title, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Home, null) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = MaterialTheme.shapes.medium
                )
                NavigationDrawerItem(
                    label = { Text(appStrings.inventory_title, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Inventory2, null) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onGoInventory() },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = MaterialTheme.shapes.medium
                )
                NavigationDrawerItem(
                    label = { Text(appStrings.profile_title, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Person, null) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onGoProfile() },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = MaterialTheme.shapes.medium
                )
                NavigationDrawerItem(
                    label = { Text(appStrings.settings_title, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Settings, null) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onGoSettings() },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text(appStrings.logout, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onLogout() },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(appStrings.home_title, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, null) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAdd, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White, shape = CircleShape, elevation = FloatingActionButtonDefaults.elevation(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                }
            }
        ) { padding ->
            HomeContent(processes, selectedFilter, onAdd, onToggleStatus, onDelete, onEdit, onOpenDetail, onFilterChange, onReopen, appStrings, Modifier.padding(padding))
        }
    }
}

@Composable
fun HomeContent(
    processes: List<ProcessItem>,
    selectedFilter: ProcessStatus?,
    onAdd: () -> Unit,
    onToggleStatus: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    onFilterChange: (ProcessStatus?) -> Unit,
    onReopen: (String) -> Unit,
    appStrings: AppStrings,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PremiumFilterChip(selectedFilter == null, appStrings.filter_all) { onFilterChange(null) }
            PremiumFilterChip(selectedFilter == ProcessStatus.PENDIENTE, appStrings.filter_pending) { onFilterChange(ProcessStatus.PENDIENTE) }
            PremiumFilterChip(selectedFilter == ProcessStatus.EN_ESPERA, appStrings.filter_waiting) { onFilterChange(ProcessStatus.EN_ESPERA) }
            PremiumFilterChip(selectedFilter == ProcessStatus.COMPLETADO, appStrings.filter_completed) { onFilterChange(ProcessStatus.COMPLETADO) }
        }
        
        if (processes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inbox, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))
                    Text(appStrings.no_processes, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(processes) { item ->
                    ProcessCard(
                        item = item,
                        onToggleStatus = { onToggleStatus(item.id) },
                        onDelete = { onDelete(item.id) },
                        onEdit = { onEdit(item.id) },
                        onOpenDetail = { onOpenDetail(item.id) },
                        onReopen = { onReopen(item.id) },
                        appStrings = appStrings
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ProcessCard(
    item: ProcessItem,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onOpenDetail: () -> Unit,
    onReopen: () -> Unit,
    appStrings: AppStrings
) {
    val locked = item.status == ProcessStatus.COMPLETADO
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        onClick = onOpenDetail, modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleLarge)
                    if (item.description.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(item.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
                StatusBadgePremium(item.status, appStrings)
            }

            Spacer(Modifier.height(16.dp))

            if (item.dueDate != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(formatLongDate(item.dueDate, appStrings), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(12.dp))
            }

            val allSubtasks = item.groups.flatMap { it.subtasks }
            if (allSubtasks.isNotEmpty()) {
                val done = allSubtasks.count { it.done }
                val progress = done.toFloat() / allSubtasks.size.toFloat()
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(appStrings.progress_label, style = MaterialTheme.typography.labelMedium)
                    Text("$done/${allSubtasks.size} (${(progress * 100).toInt()}%)", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (!locked) {
                    IconButton(onClick = onToggleStatus) { Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
                } else {
                    OutlinedButton(onClick = onReopen, shape = CircleShape, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                        Text(appStrings.reopen_btn, style = MaterialTheme.typography.labelMedium)
                    }
                }
                IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(appStrings.delete, style = MaterialTheme.typography.titleLarge) },
            text = { Text(appStrings.delete_process_confirm) },
            confirmButton = { Button(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(appStrings.delete) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(appStrings.cancel) } },
            shape = MaterialTheme.shapes.large
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    process: ProcessItem,
    onBack: () -> Unit,
    onUpdateProcess: (ProcessItem) -> Unit,
    appStrings: AppStrings
) {
    var current by remember { mutableStateOf(process) }
    val locked = current.status == ProcessStatus.COMPLETADO

    val allSubtasks = current.groups.flatMap { it.subtasks }
    val progress = if (allSubtasks.isEmpty()) 0f else allSubtasks.count { it.done }.toFloat() / allSubtasks.size.toFloat()

    fun applyAutoStatus(updated: ProcessItem): ProcessItem {
        val subtasks = updated.groups.flatMap { it.subtasks }
        if (subtasks.isEmpty()) return updated
        val allDone = subtasks.all { it.done }
        return updated.copy(status = if (allDone) ProcessStatus.COMPLETADO else ProcessStatus.PENDIENTE)
    }

    var showAddGroup by remember { mutableStateOf(false) }
    var showAddSubtaskForGroupId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(appStrings.detail_title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (!locked) {
                FloatingActionButton(onClick = { showAddGroup = true }, shape = CircleShape) { Icon(Icons.Default.Add, null) }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Column(Modifier.padding(24.dp)) {
                    Text(current.title, style = MaterialTheme.typography.headlineMedium)
                    if (current.description.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(current.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        StatusBadgePremium(current.status, appStrings)
                        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    if (current.dueDate != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(formatLongDate(current.dueDate, appStrings), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape))
                }
            }

            if (locked) {
                Button(onClick = { val updated = current.copy(status = ProcessStatus.PENDIENTE); current = updated; onUpdateProcess(updated) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = MaterialTheme.shapes.medium) { Text(appStrings.reopen_btn) }
            } else {
                OutlinedButton(
                    onClick = { current.groups.firstOrNull()?.id?.let { showAddSubtaskForGroupId = it } },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) { Text("+ ${appStrings.add_subtask}", fontWeight = FontWeight.Bold) }
            }

            current.groups.forEach { group ->
                GroupCard(
                    group = group, locked = locked, onAddSubtask = { showAddSubtaskForGroupId = group.id },
                    onToggleSubtask = { subId ->
                        val newGroups = current.groups.map { g -> if (g.id != group.id) g else g.copy(subtasks = g.subtasks.map { st -> if (st.id == subId) st.copy(done = !st.done) else st }) }
                        val updated = applyAutoStatus(current.copy(groups = newGroups)); current = updated; onUpdateProcess(updated)
                    },
                    onDeleteSubtask = { subId ->
                        val newGroups = current.groups.map { g -> if (g.id != group.id) g else g.copy(subtasks = g.subtasks.filterNot { it.id == subId }) }
                        val updated = applyAutoStatus(current.copy(groups = newGroups)); current = updated; onUpdateProcess(updated)
                    },
                    appStrings = appStrings
                )
            }
        }
    }

    if (showAddGroup) {
        AddGroupDialog(appStrings = appStrings, onDismiss = { showAddGroup = false }, onAdd = { name ->
            val updated = current.copy(groups = current.groups + TaskGroup(name = name)); current = updated; onUpdateProcess(updated); showAddGroup = false
        })
    }

    if (showAddSubtaskForGroupId != null) {
        val gid = showAddSubtaskForGroupId!!
        AddSubtaskDialog(appStrings = appStrings, onDismiss = { showAddSubtaskForGroupId = null }, onAdd = { text ->
            val newGroups = current.groups.map { g -> if (g.id != gid) g else g.copy(subtasks = g.subtasks + SubTask(text = text)) }
            val updated = applyAutoStatus(current.copy(groups = newGroups)); current = updated; onUpdateProcess(updated); showAddSubtaskForGroupId = null
        })
    }
}

@Composable
fun GroupCard(
    group: TaskGroup, locked: Boolean, onAddSubtask: () -> Unit, onToggleSubtask: (String) -> Unit,
    onDeleteSubtask: (String) -> Unit, onToggleStatus: () -> Unit = {}, appStrings: AppStrings
) {
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(group.name, style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
                if (!locked) { IconButton(onClick = onAddSubtask) { Icon(Icons.Default.AddCircleOutline, null, tint = MaterialTheme.colorScheme.primary) } }
            }
            if (group.subtasks.isEmpty()) {
                Text(appStrings.no_subtasks, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                group.subtasks.forEach { st ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(checked = st.done, onCheckedChange = { onToggleSubtask(st.id) }, enabled = !locked)
                            Text(st.text, style = if (st.done) MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough) else MaterialTheme.typography.bodyLarge, modifier = Modifier.alpha(if(st.done) 0.5f else 1f))
                        }
                        if (!locked) { IconButton(onClick = { pendingDeleteId = st.id }) { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) } }
                    }
                }
            }
        }
    }
    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null }, title = { Text(appStrings.delete, style = MaterialTheme.typography.titleLarge) },
            text = { Text(appStrings.delete_subtask_confirm) },
            confirmButton = { Button(onClick = { onDeleteSubtask(pendingDeleteId!!); pendingDeleteId = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(appStrings.delete) } },
            dismissButton = { TextButton(onClick = { pendingDeleteId = null }) { Text(appStrings.cancel) } }, shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
fun AddGroupDialog(appStrings: AppStrings, onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(appStrings.add_group, style = MaterialTheme.typography.titleLarge) },
        text = { PremiumTextField(value = name, onValueChange = { name = it }, label = appStrings.group_name_label) },
        confirmButton = { Button(onClick = { onAdd(name) }, enabled = name.isNotBlank()) { Text(appStrings.add_btn) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(appStrings.cancel) } }, shape = MaterialTheme.shapes.large
    )
}

@Composable
fun AddSubtaskDialog(appStrings: AppStrings, onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(appStrings.add_subtask, style = MaterialTheme.typography.titleLarge) },
        text = { PremiumTextField(value = text, onValueChange = { text = it }, label = appStrings.subtask_text_label) },
        confirmButton = { Button(onClick = { onAdd(text) }, enabled = text.isNotBlank()) { Text(appStrings.add_btn) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(appStrings.cancel) } }, shape = MaterialTheme.shapes.large
    )
}

@Composable
fun ProfileScreen(
    isLoading: Boolean = false, initialName: String, email: String, photoUrl: String?,
    onUpdateProfile: (String) -> Unit, onPhotoSelected: (Uri) -> Unit, onLogout: () -> Unit,
    onChangePassword: () -> Unit, onBack: () -> Unit, appStrings: AppStrings
) {
    var name by remember { mutableStateOf(initialName) }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { it?.let { onPhotoSelected(it) } }
    if (isLoading) LoadingOverlay(appStrings)
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text(appStrings.profile_title, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(48.dp))
            Box(modifier = Modifier.size(120.dp).align(Alignment.CenterHorizontally)) {
                Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, shadowElevation = 8.dp) {
                    if (!photoUrl.isNullOrBlank()) { AsyncImage(model = photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop) }
                    else { Box(contentAlignment = Alignment.Center) { Text("👤", style = MaterialTheme.typography.displayLarge) } }
                }
                SmallFloatingActionButton(onClick = { photoLauncher.launch("image/*") }, modifier = Modifier.align(Alignment.BottomEnd), containerColor = MaterialTheme.colorScheme.primary, shape = CircleShape) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp), tint = Color.White)
                }
            }
            Spacer(Modifier.height(48.dp))
            PremiumTextField(value = name, onValueChange = { name = it }, label = appStrings.name_label, trailingIcon = { if (name != initialName && name.isNotBlank()) { IconButton(onClick = { onUpdateProfile(name) }) { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50)) } } })
            Spacer(Modifier.height(16.dp))
            PremiumTextField(value = email, onValueChange = {}, label = appStrings.email_label, enabled = false)
            Spacer(Modifier.height(48.dp))
            Button(onClick = onChangePassword, modifier = Modifier.fillMaxWidth().height(56.dp), shape = MaterialTheme.shapes.medium) { Text(appStrings.change_pass_title) }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(56.dp), shape = MaterialTheme.shapes.medium, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))) { Text(appStrings.logout) }
        }
    }
}

@Composable
fun ChangePasswordScreen(
    isLoading: Boolean = false, onBack: () -> Unit, onUpdatePassword: (String, String) -> Unit, appStrings: AppStrings
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    if (isLoading) LoadingOverlay(appStrings)
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text(appStrings.change_pass_title, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(48.dp))
            PremiumTextField(value = currentPass, onValueChange = { currentPass = it }, label = appStrings.current_pass_label, visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { show = !show }) { Icon(if(show) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary) } })
            Spacer(Modifier.height(16.dp))
            PremiumTextField(value = newPass, onValueChange = { newPass = it }, label = appStrings.new_pass_label, visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation())
            Spacer(Modifier.height(16.dp))
            PremiumTextField(value = confirm, onValueChange = { confirm = it }, label = appStrings.confirm_pass_label, visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation())
            Spacer(Modifier.height(48.dp))
            Button(onClick = { onUpdatePassword(currentPass, newPass) }, enabled = currentPass.isNotBlank() && newPass.length >= 6 && newPass == confirm && !isLoading, modifier = Modifier.fillMaxWidth().height(56.dp), shape = MaterialTheme.shapes.medium) { Text(appStrings.update_pass_btn) }
        }
    }
}

@Composable
fun SettingsScreen(
    themeMode: String, onSetThemeMode: (String) -> Unit, language: String, onSetLanguage: (String) -> Unit, onBack: () -> Unit, appStrings: AppStrings
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text(appStrings.settings_title, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(40.dp))
            Text(appStrings.appearance, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            PremiumSelectionCard(appStrings.light_mode, themeMode == "light") { onSetThemeMode("light") }
            PremiumSelectionCard(appStrings.dark_mode, themeMode == "dark") { onSetThemeMode("dark") }
            PremiumSelectionCard(appStrings.auto_system, themeMode == "auto") { onSetThemeMode("auto") }
            Spacer(Modifier.height(32.dp))
            Text(appStrings.language_label, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            PremiumSelectionCard(appStrings.spanish, language == "es") { onSetLanguage("es") }
            PremiumSelectionCard(appStrings.english, language == "en") { onSetLanguage("en") }
        }
    }
}

@Composable
fun PremiumSelectionCard(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditProcessScreen(existing: ProcessItem?, onSave: (ProcessItem) -> Unit, onBack: () -> Unit, appStrings: AppStrings) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: ProcessStatus.PENDIENTE) }
    var dueDate by remember { mutableStateOf(existing?.dueDate) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate ?: System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text(if (existing == null) appStrings.create_process_title else appStrings.edit_process_title, style = MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(48.dp))
            PremiumTextField(value = title, onValueChange = { title = it }, label = appStrings.title_label)
            Spacer(Modifier.height(16.dp))
            PremiumTextField(value = description, onValueChange = { description = it }, label = appStrings.description_label, singleLine = false, modifier = Modifier.height(120.dp))
            Spacer(Modifier.height(32.dp))
            Text(appStrings.due_date_selection, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Surface(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(formatLongDate(dueDate, appStrings))
                    }
                    if (dueDate != null) IconButton(onClick = { dueDate = null }) { Icon(Icons.Default.Clear, null) }
                }
            }
            Spacer(Modifier.height(32.dp))
            Text(appStrings.status_label, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumFilterChip(status == ProcessStatus.PENDIENTE, appStrings.filter_pending) { status = ProcessStatus.PENDIENTE }
                PremiumFilterChip(status == ProcessStatus.EN_ESPERA, appStrings.filter_waiting) { status = ProcessStatus.EN_ESPERA }
                PremiumFilterChip(status == ProcessStatus.COMPLETADO, appStrings.filter_completed) { status = ProcessStatus.COMPLETADO }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { onSave(existing?.copy(title = title.trim(), description = description.trim(), status = status, dueDate = dueDate) ?: ProcessItem(title = title.trim(), description = description.trim(), status = status, dueDate = dueDate)) }, modifier = Modifier.fillMaxWidth().height(60.dp), enabled = title.isNotBlank(), shape = MaterialTheme.shapes.medium) { Text(appStrings.save) }
        }
    }
    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { dueDate = datePickerState.selectedDateMillis; showDatePicker = false }) { Text(appStrings.select_btn) } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(appStrings.cancel) } }) {
            DatePicker(state = datePickerState)
        }
    }
}
