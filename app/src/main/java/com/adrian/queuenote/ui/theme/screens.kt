package com.adrian.queuenote.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
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
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun LoadingOverlay(appStrings: AppStrings) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text(appStrings.loading, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun SplashScreen(appStrings: AppStrings, onContinue: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(appStrings.app_name, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(10.dp))
            Text(appStrings.splash_subtitle)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onContinue) { Text(appStrings.continue_btn) }
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

    val emailOk = email.isNotBlank() && isValidEmail(email)
    val passwordOk = password.length >= 6
    val canLogin = emailOk && passwordOk

    if (isLoading) LoadingOverlay(appStrings)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text(appStrings.login_title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(appStrings.email_label) },
                modifier = Modifier.fillMaxWidth(),
                isError = email.isNotBlank() && !emailOk,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                supportingText = {
                    if (email.isNotBlank() && !emailOk) Text(appStrings.invalid_email)
                }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(appStrings.password_label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                enabled = !isLoading,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(if (showPassword) appStrings.hide_pass else appStrings.show_pass)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onForgot, enabled = !isLoading) { Text(appStrings.forgot_pass_link) }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onLogin(email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canLogin && !isLoading
            ) { Text(appStrings.login_btn) }

            Spacer(Modifier.height(10.dp))
            
            OutlinedButton(
                onClick = onLoginWithGitHub,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(appStrings.login_github)
            }

            Spacer(Modifier.height(10.dp))
            
            OutlinedButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                Text(appStrings.create_account_btn)
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

    val emailOk = email.isNotBlank() && isValidEmail(email)
    val passOk = pass.length >= 6
    val passMatch = pass == pass2 && pass2.isNotBlank()
    val canRegister = name.isNotBlank() && emailOk && passOk && passMatch

    val focusManager = LocalFocusManager.current
    val emailFocus = remember { FocusRequester() }
    val passFocus = remember { FocusRequester() }
    val pass2Focus = remember { FocusRequester() }

    if (isLoading) LoadingOverlay(appStrings)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text(appStrings.register_title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(appStrings.name_label) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() })
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(appStrings.email_label) },
                modifier = Modifier.fillMaxWidth().focusRequester(emailFocus),
                enabled = !isLoading,
                isError = email.isNotBlank() && !emailOk,
                supportingText = {
                    if (email.isNotBlank() && !emailOk) Text(appStrings.invalid_email)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { passFocus.requestFocus() })
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text(appStrings.password_label) },
                modifier = Modifier.fillMaxWidth().focusRequester(passFocus),
                enabled = !isLoading,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showPass = !showPass }) {
                        Text(if (showPass) appStrings.hide_pass else appStrings.show_pass)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { pass2Focus.requestFocus() })
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = pass2,
                onValueChange = { pass2 = it },
                label = { Text(appStrings.confirm_password_label) },
                modifier = Modifier.fillMaxWidth().focusRequester(pass2Focus),
                enabled = !isLoading,
                isError = pass2.isNotBlank() && !passMatch,
                supportingText = {
                    if (pass2.isNotBlank() && !passMatch) Text(appStrings.pass_mismatch)
                },
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(appStrings.account_type, style = MaterialTheme.typography.titleMedium)
                    Text(if (isPrivate) appStrings.private_label else appStrings.public_label)
                }
                Switch(checked = isPrivate, onCheckedChange = { isPrivate = it }, enabled = !isLoading)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onRegisterSuccess(email, pass)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canRegister && !isLoading
            ) {
                Text(appStrings.register_title)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                Text(appStrings.back_to_login)
            }
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
    val emailOk = email.isNotBlank() && isValidEmail(email)
    val focusManager = LocalFocusManager.current

    if (isLoading) LoadingOverlay(appStrings)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text(appStrings.forgot_title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Text(appStrings.forgot_desc)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(appStrings.email_label) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = email.isNotBlank() && !emailOk,
                supportingText = {
                    if (email.isNotBlank() && !emailOk) Text(appStrings.invalid_email)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSendReset(email)
                },
                enabled = emailOk && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(appStrings.send_link)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                Text(appStrings.back)
            }
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
            ModalDrawerSheet {
                Text(appStrings.app_name, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(appStrings.home_title) },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text(appStrings.inventory_title) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onGoInventory()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(appStrings.profile_title) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onGoProfile()
                    }
                )
                NavigationDrawerItem(
                    label = { Text(appStrings.settings_title) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onGoSettings()
                    }
                )
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text(appStrings.logout) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(appStrings.home_title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Crear")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                HomeContent(
                    processes = processes,
                    selectedFilter = selectedFilter,
                    onFilterChange = onFilterChange,
                    onAdd = onAdd,
                    onToggleStatus = onToggleStatus,
                    onDelete = onDelete,
                    onEdit = onEdit,
                    onOpenDetail = onOpenDetail,
                    onReopen = onReopen,
                    appStrings = appStrings
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    selectedFilter: ProcessStatus?,
    onFilterChange: (ProcessStatus?) -> Unit,
    appStrings: AppStrings
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterChange(null) },
            label = { Text(appStrings.filter_all) }
        )
        FilterChip(
            selected = ProcessStatus.PENDIENTE == selectedFilter,
            onClick = { onFilterChange(ProcessStatus.PENDIENTE) },
            label = { Text(appStrings.filter_pending) }
        )
        FilterChip(
            selected = ProcessStatus.EN_ESPERA == selectedFilter,
            onClick = { onFilterChange(ProcessStatus.EN_ESPERA) },
            label = { Text(appStrings.filter_waiting) }
        )
        FilterChip(
            selected = ProcessStatus.COMPLETADO == selectedFilter,
            onClick = { onFilterChange(ProcessStatus.COMPLETADO) },
            label = { Text(appStrings.filter_completed) }
        )
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
    appStrings: AppStrings
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(16.dp))
        FilterSection(selectedFilter, onFilterChange, appStrings)
        Spacer(Modifier.height(16.dp))

        if (processes.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(appStrings.no_processes, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(appStrings.add_first_process)
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                processes.forEach { item ->
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
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun StatusBadge(status: ProcessStatus, appStrings: AppStrings) {
    val label = getStatusDisplayName(status, appStrings)
    AssistChip(
        onClick = {},
        label = { Text(label) }
    )
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

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpenDetail() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                StatusBadge(item.status, appStrings)
            }

            if (item.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(item.description, style = MaterialTheme.typography.bodyMedium)
            }

            if (item.dueDate != null) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${appStrings.due_date_label} ${formatLongDate(item.dueDate, appStrings)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            val allSubtasks = item.groups.flatMap { it.subtasks }
            if (allSubtasks.isNotEmpty()) {
                val done = allSubtasks.count { it.done }
                val total = allSubtasks.size
                val percentage = if (total == 0) 0 else (done * 100) / total
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${appStrings.progress_label} $done/$total ($percentage%)", style = MaterialTheme.typography.bodySmall)
                }
                LinearProgressIndicator(
                    progress = { if (total == 0) 0f else done.toFloat() / total.toFloat() },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!locked) {
                    IconButton(onClick = onToggleStatus) {
                        Icon(Icons.Default.Refresh, contentDescription = "Cambiar estado")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                } else {
                    OutlinedButton(onClick = onReopen) {
                        Text(appStrings.reopen_btn)
                    }
                }

                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(appStrings.delete) },
            text = { Text(appStrings.delete_process_confirm) },
            confirmButton = {
                Button(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) { Text(appStrings.delete) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text(appStrings.cancel) }
            }
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
    val total = allSubtasks.size
    val done = allSubtasks.count { it.done }
    val progress = if (total == 0) 0f else done.toFloat() / total.toFloat()
    val percentage = (progress * 100).toInt()

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
            TopAppBar(
                title = { Text(appStrings.detail_title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!locked) {
                FloatingActionButton(onClick = { showAddGroup = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar grupo")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(current.title, style = MaterialTheme.typography.headlineSmall)
            if (current.description.isNotBlank()) Text(current.description)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(onClick = {}, label = { Text("${appStrings.status_label}: ${getStatusDisplayName(current.status, appStrings)}") })
                Text("$done/$total ($percentage%)")
            }

            if (current.dueDate != null) {
                Text(
                    text = "${appStrings.due_date_label} ${formatLongDate(current.dueDate, appStrings)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())

            if (locked) {
                OutlinedButton(
                    onClick = {
                        val updated = current.copy(status = ProcessStatus.PENDIENTE)
                        current = updated
                        onUpdateProcess(updated)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(appStrings.reopen_btn)
                }
            }

            if (!locked) {
                Button(
                    onClick = {
                        val defaultGroupId = current.groups.firstOrNull()?.id
                        if (defaultGroupId != null) showAddSubtaskForGroupId = defaultGroupId
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("➕ ${appStrings.add_subtask}")
                }
            }

            current.groups.forEach { group ->
                GroupCard(
                    group = group,
                    locked = locked,
                    onAddSubtask = { showAddSubtaskForGroupId = group.id },
                    onToggleStatus = { },
                    onToggleSubtask = { subId ->
                        val newGroups = current.groups.map { g ->
                            if (g.id != group.id) g
                            else g.copy(
                                subtasks = g.subtasks.map { st ->
                                    if (st.id == subId) st.copy(done = !st.done) else st
                                }
                            )
                        }
                        val updated = applyAutoStatus(current.copy(groups = newGroups))
                        current = updated
                        onUpdateProcess(updated)
                    },
                    onDeleteSubtask = { subId ->
                        val newGroups = current.groups.map { g ->
                            if (g.id != group.id) g
                            else g.copy(subtasks = g.subtasks.filterNot { it.id == subId })
                        }
                        val updated = applyAutoStatus(current.copy(groups = newGroups))
                        current = updated
                        onUpdateProcess(updated)
                    },
                    appStrings = appStrings
                )
            }
        }
    }

    if (showAddGroup) {
        AddGroupDialog(
            appStrings = appStrings,
            onDismiss = { showAddGroup = false },
            onAdd = { name ->
                val updated = current.copy(groups = current.groups + TaskGroup(name = name))
                current = updated
                onUpdateProcess(updated)
                showAddGroup = false
            }
        )
    }

    val gid = showAddSubtaskForGroupId
    if (gid != null) {
        AddSubtaskDialog(
            appStrings = appStrings,
            onDismiss = { showAddSubtaskForGroupId = null },
            onAdd = { text ->
                val newGroups = current.groups.map { g ->
                    if (g.id != gid) g
                    else g.copy(subtasks = g.subtasks + SubTask(text = text))
                }
                val updated = applyAutoStatus(current.copy(groups = newGroups))
                current = updated
                onUpdateProcess(updated)
                showAddSubtaskForGroupId = null
            }
        )
    }
}

@Composable
fun GroupCard(
    group: TaskGroup,
    locked: Boolean,
    onAddSubtask: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onDeleteSubtask: (String) -> Unit,
    onToggleStatus: () -> Unit = {},
    appStrings: AppStrings
) {
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(group.name, style = MaterialTheme.typography.titleMedium)
                if (!locked) {
                    OutlinedButton(onClick = onAddSubtask) { Text("+ ${appStrings.add_subtask}") }
                }
            }

            if (group.subtasks.isEmpty()) {
                Text(appStrings.no_subtasks)
            } else {
                group.subtasks.forEach { st ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = st.done,
                                onCheckedChange = { onToggleSubtask(st.id) },
                                enabled = !locked
                            )
                            val style = if (st.done) {
                                MaterialTheme.typography.bodyLarge.copy(
                                    textDecoration = TextDecoration.LineThrough
                                )
                            } else MaterialTheme.typography.bodyLarge
                            val alpha = if (st.done) 0.6f else 1f
                            Text(st.text, style = style, modifier = Modifier.alpha(alpha))
                        }
                        if (!locked) {
                            IconButton(onClick = { pendingDeleteId = st.id }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar subtarea", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(appStrings.delete) },
            text = { Text(appStrings.delete_subtask_confirm) },
            confirmButton = {
                Button(onClick = {
                    onDeleteSubtask(pendingDeleteId!!)
                    pendingDeleteId = null
                }) { Text(appStrings.delete) }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingDeleteId = null }) { Text(appStrings.cancel) }
            }
        )
    }
}

@Composable
fun AddGroupDialog(appStrings: AppStrings, onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(appStrings.add_group) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(appStrings.group_name_label) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name.trim().ifBlank { "Group" }) },
                enabled = name.trim().isNotBlank()
            ) { Text(appStrings.add_btn) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(appStrings.cancel) }
        }
    )
}

@Composable
fun AddSubtaskDialog(appStrings: AppStrings, onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(appStrings.add_subtask) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(appStrings.subtask_text_label) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onAdd(text.trim()) },
                enabled = text.trim().isNotBlank()
            ) { Text(appStrings.add_btn) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(appStrings.cancel) }
        }
    )
}

@Composable
fun ProfileScreen(
    isLoading: Boolean = false,
    initialName: String,
    email: String,
    photoUrl: String?,
    onUpdateProfile: (String) -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    onBack: () -> Unit,
    appStrings: AppStrings
) {
    var name by remember { mutableStateOf(initialName) }
    val githubUser = "GitHub no conectado"

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPhotoSelected(it) }
    }

    if (isLoading) LoadingOverlay(appStrings)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text(appStrings.profile_title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(110.dp).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    tonalElevation = 2.dp
                ) {
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👤", style = MaterialTheme.typography.headlineLarge)
                        }
                    }
                }

                IconButton(
                    onClick = { photoLauncher.launch("image/*") },
                    modifier = Modifier.align(Alignment.BottomEnd),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Cambiar foto", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(appStrings.name_label) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                trailingIcon = {
                    if (name != initialName && name.isNotBlank()) {
                        IconButton(onClick = { onUpdateProfile(name) }) {
                            Icon(Icons.Default.Check, contentDescription = "Guardar nombre", tint = Color(0xFF4CAF50))
                        }
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text(appStrings.email_label) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = githubUser,
                onValueChange = {},
                label = { Text("GitHub") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(Modifier.height(20.dp))

            Button(onClick = onChangePassword, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                Text(appStrings.change_pass_title)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(appStrings.logout)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                Text(appStrings.back)
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(
    isLoading: Boolean = false,
    onBack: () -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    appStrings: AppStrings
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }

    val currentOk = currentPass.length >= 6
    val newOk = newPass.length >= 6
    val matchOk = confirm.isNotBlank() && confirm == newPass
    val canSave = currentOk && newOk && matchOk

    val focusManager = LocalFocusManager.current
    val newPassFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }

    if (isLoading) LoadingOverlay(appStrings)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text(appStrings.change_pass_title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = currentPass,
                onValueChange = { currentPass = it },
                label = { Text(appStrings.current_pass_label) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { newPassFocus.requestFocus() }),
                trailingIcon = {
                    TextButton(onClick = { show = !show }) { 
                        Text(if (show) appStrings.hide_pass else appStrings.show_pass) 
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text(appStrings.new_pass_label) },
                modifier = Modifier.fillMaxWidth().focusRequester(newPassFocus),
                enabled = !isLoading,
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { confirmFocus.requestFocus() }),
                trailingIcon = {
                    TextButton(onClick = { show = !show }) { 
                        Text(if (show) appStrings.hide_pass else appStrings.show_pass) 
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text(appStrings.confirm_pass_label) },
                modifier = Modifier.fillMaxWidth().focusRequester(confirmFocus),
                enabled = !isLoading,
                isError = confirm.isNotBlank() && !matchOk,
                supportingText = {
                    if (confirm.isNotBlank() && !matchOk) Text(appStrings.pass_mismatch)
                },
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                trailingIcon = {
                    TextButton(onClick = { show = !show }) { 
                        Text(if (show) appStrings.hide_pass else appStrings.show_pass) 
                    }
                }
            )

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onUpdatePassword(currentPass, newPass)
                },
                enabled = canSave && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(appStrings.update_pass_btn) }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                Text(appStrings.back)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    themeMode: String,
    onSetThemeMode: (String) -> Unit,
    language: String,
    onSetLanguage: (String) -> Unit,
    onBack: () -> Unit,
    appStrings: AppStrings
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text(appStrings.settings_title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            Text(appStrings.appearance, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeOption(appStrings.auto_system, "auto", themeMode, onSetThemeMode)
                ThemeOption(appStrings.light_mode, "light", themeMode, onSetThemeMode)
                ThemeOption(appStrings.dark_mode, "dark", themeMode, onSetThemeMode)
            }

            Spacer(Modifier.height(32.dp))

            Text(appStrings.language_label, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LanguageOption(appStrings.auto_system, "auto", language, onSetLanguage)
                LanguageOption(appStrings.spanish, "es", language, onSetLanguage)
                LanguageOption(appStrings.english, "en", language, onSetLanguage)
            }

            Spacer(Modifier.height(48.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(appStrings.back) }
        }
    }
}

@Composable
fun ThemeOption(text: String, mode: String, currentMode: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = (mode == currentMode), onClick = { onSelect(mode) })
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun LanguageOption(text: String, lang: String, currentLang: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(lang) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = (lang == currentLang), onClick = { onSelect(lang) })
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditProcessScreen(
    existing: ProcessItem?,
    onSave: (ProcessItem) -> Unit,
    onBack: () -> Unit,
    appStrings: AppStrings
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: ProcessStatus.PENDIENTE) }
    var dueDate by remember { mutableStateOf(existing?.dueDate) }

    val canSave = title.trim().isNotBlank()

    val focusManager = LocalFocusManager.current
    val descFocusRequester = remember { FocusRequester() }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate ?: System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) appStrings.create_process_title else appStrings.edit_process_title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(appStrings.title_label) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { descFocusRequester.requestFocus() }
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(appStrings.description_label) },
                modifier = Modifier.fillMaxWidth().focusRequester(descFocusRequester),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(Modifier.height(16.dp))

            Text(appStrings.due_date_selection, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(formatLongDate(dueDate, appStrings))
                    }
                    if (dueDate != null) {
                        IconButton(onClick = { dueDate = null }) {
                            Icon(Icons.Default.Clear, contentDescription = appStrings.clear_date)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(appStrings.status_label, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = status == ProcessStatus.PENDIENTE,
                    onClick = { status = ProcessStatus.PENDIENTE },
                    label = { Text(appStrings.filter_pending) }
                )
                FilterChip(
                    selected = status == ProcessStatus.EN_ESPERA,
                    onClick = { status = ProcessStatus.EN_ESPERA },
                    label = { Text(appStrings.filter_waiting) }
                )
                FilterChip(
                    selected = status == ProcessStatus.COMPLETADO,
                    onClick = { status = ProcessStatus.COMPLETADO },
                    label = { Text(appStrings.filter_completed) }
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val item = ProcessItem(
                        id = existing?.id ?: ProcessItem(title = "temp").id,
                        title = title.trim(),
                        description = description.trim(),
                        status = status,
                        groups = existing?.groups ?: listOf(TaskGroup()),
                        dueDate = dueDate
                    )
                    onSave(item)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave
            ) {
                Text(appStrings.save)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(appStrings.cancel)
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text(appStrings.select_btn) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(appStrings.cancel) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
