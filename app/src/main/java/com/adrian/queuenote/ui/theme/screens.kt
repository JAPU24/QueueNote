package com.adrian.queuenote.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.adrian.queuenote.ProcessItem
import com.adrian.queuenote.ProcessStatus
import com.adrian.queuenote.SubTask
import com.adrian.queuenote.TaskGroup
import kotlinx.coroutines.launch

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun getStatusDisplayName(status: ProcessStatus): String {
    return when (status) {
        ProcessStatus.PENDIENTE -> "Pendiente"
        ProcessStatus.EN_ESPERA -> "En espera"
        ProcessStatus.COMPLETADO -> "Completado"
    }
}

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("QueueNote", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(10.dp))
            Text("Registro personal de esperas y procesos")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onContinue) { Text("Continuar") }
        }
    }
}

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
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

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
                isError = email.isNotBlank() && !emailOk,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                supportingText = {
                    if (email.isNotBlank() && !emailOk) Text("Correo no válido")
                }
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showPassword = !showPassword }) {
                        Text(if (showPassword) "Ocultar" else "Ver")
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
            TextButton(onClick = onForgot) { Text("¿Olvidaste tu contraseña?") }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onLogin()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canLogin
            ) { Text("Entrar") }

            Spacer(Modifier.height(10.dp))
            
            OutlinedButton(
                onClick = onLoginWithGitHub,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuar con GitHub (mock)")
            }

            Spacer(Modifier.height(10.dp))
            
            OutlinedButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) {
                Text("Crear cuenta")
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
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

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text("Registro", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() })
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth().focusRequester(emailFocus),
                isError = email.isNotBlank() && !emailOk,
                supportingText = {
                    if (email.isNotBlank() && !emailOk) Text("Correo no válido")
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
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth().focusRequester(passFocus),
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { showPass = !showPass }) {
                        Text(if (showPass) "Ocultar" else "Ver")
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
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth().focusRequester(pass2Focus),
                isError = pass2.isNotBlank() && !passMatch,
                supportingText = {
                    if (pass2.isNotBlank() && !passMatch) Text("Las contraseñas no coinciden")
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
                    Text("Tipo de cuenta", style = MaterialTheme.typography.titleMedium)
                    Text(if (isPrivate) "Privada" else "Pública")
                }
                Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onRegisterSuccess()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canRegister
            ) {
                Text("Crear cuenta")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Volver a Login")
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val emailOk = email.isNotBlank() && isValidEmail(email)
    val focusManager = LocalFocusManager.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Text("Escribe tu correo y te enviaremos un enlace (mock).")

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
                isError = email.isNotBlank() && !emailOk,
                supportingText = {
                    if (email.isNotBlank() && !emailOk) Text("Correo no válido")
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
                    // Mock: aquí luego se enviará el email real
                },
                enabled = emailOk,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar enlace")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
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
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("QueueNote", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Menú principal") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Mi perfil") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onGoProfile()
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Ajustes") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onGoSettings()
                    }
                )
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
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
                    title = { Text("Menú principal") },
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
                    onReopen = onReopen
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    selectedFilter: ProcessStatus?,
    onFilterChange: (ProcessStatus?) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterChange(null) },
            label = { Text("Todos") }
        )
        FilterChip(
            selected = selectedFilter == ProcessStatus.PENDIENTE,
            onClick = { onFilterChange(ProcessStatus.PENDIENTE) },
            label = { Text("Pendiente") }
        )
        FilterChip(
            selected = selectedFilter == ProcessStatus.EN_ESPERA,
            onClick = { onFilterChange(ProcessStatus.EN_ESPERA) },
            label = { Text("En espera") }
        )
        FilterChip(
            selected = selectedFilter == ProcessStatus.COMPLETADO,
            onClick = { onFilterChange(ProcessStatus.COMPLETADO) },
            label = { Text("Completado") }
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
    onReopen: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(16.dp))
        FilterSection(selectedFilter, onFilterChange)
        Spacer(Modifier.height(16.dp))

        if (processes.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No hay procesos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Toca el + para crear tu primer proceso.")
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
                        onReopen = { onReopen(item.id) }
                    )
                }
                Spacer(Modifier.height(80.dp)) // Espacio para el FAB
            }
        }
    }
}

@Composable
fun StatusBadge(status: ProcessStatus) {
    val label = getStatusDisplayName(status)
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
    onReopen: () -> Unit
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
                StatusBadge(item.status)
            }
            
            if (item.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(item.description, style = MaterialTheme.typography.bodyMedium)
            }
            
            val allSubtasks = item.groups.flatMap { it.subtasks }
            if (allSubtasks.isNotEmpty()) {
                val done = allSubtasks.count { it.done }
                val total = allSubtasks.size
                Spacer(Modifier.height(6.dp))
                Text("Progreso: $done/$total", style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = { done.toFloat() / total.toFloat() },
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
                        Text("Reabrir")
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
            title = { Text("Eliminar proceso") },
            text = { Text("¿Seguro que deseas eliminar el proceso \"${item.title}\"? Se perderán todas sus subtareas y grupos.") },
            confirmButton = {
                Button(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    process: ProcessItem,
    onBack: () -> Unit,
    onUpdateProcess: (ProcessItem) -> Unit
) {
    var current by remember { mutableStateOf(process) }
    val locked = current.status == ProcessStatus.COMPLETADO

    // helpers progreso
    val allSubtasks = current.groups.flatMap { it.subtasks }
    val total = allSubtasks.size
    val done = allSubtasks.count { it.done }
    val progress = if (total == 0) 0f else done.toFloat() / total.toFloat()

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
                title = { Text("Detalle del proceso") },
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
                AssistChip(onClick = {}, label = { Text("Estado: ${getStatusDisplayName(current.status)}") })
                Text(if (total == 0) "0/0" else "$done/$total")
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
                    Text("Reabrir tarea")
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
                    Text("➕ Agregar subtarea")
                }
            }

            current.groups.forEach { group ->
                GroupCard(
                    group = group,
                    locked = locked,
                    onAddSubtask = { showAddSubtaskForGroupId = group.id },
                    onToggleStatus = { /* No longer used here, keeping for structure if needed */ },
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
                    }
                )
            }
        }
    }

    if (showAddGroup) {
        AddGroupDialog(
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
    onToggleStatus: () -> Unit = {}
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
                    OutlinedButton(onClick = onAddSubtask) { Text("+ Subtarea") }
                }
            }

            if (group.subtasks.isEmpty()) {
                Text("Sin subtareas todavía.")
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
            title = { Text("Eliminar subtarea") },
            text = { Text("¿Seguro que deseas eliminar esta subtarea?") },
            confirmButton = {
                Button(onClick = {
                    onDeleteSubtask(pendingDeleteId!!)
                    pendingDeleteId = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingDeleteId = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun AddGroupDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar grupo") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del grupo") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name.trim().ifBlank { "Grupo" }) },
                enabled = name.trim().isNotBlank()
            ) { Text("Agregar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddSubtaskDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar subtarea") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Texto de la subtarea") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onAdd(text.trim()) },
                enabled = text.trim().isNotBlank()
            ) { Text("Agregar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ProfileScreen(
    onChangePassword: () -> Unit,
    onBack: () -> Unit
) {
    val name = "Adrián"
    val email = "adrian@email.com"
    val githubUser = "adrian-queuenote"

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text("Mi perfil", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = { /* mock */ }) {
                Text("Cambiar foto (mock)")
            }

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {},
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = githubUser,
                onValueChange = {},
                label = { Text("GitHub (mock)") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )

            Spacer(Modifier.height(20.dp))

            Button(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
                Text("Cambiar contraseña")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }

    val newOk = newPass.length >= 6
    val matchOk = confirm.isNotBlank() && confirm == newPass
    val canSave = current.isNotBlank() && newOk && matchOk

    val focusManager = LocalFocusManager.current
    val newFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text("Cambiar contraseña", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = current,
                onValueChange = { current = it },
                label = { Text("Contraseña actual") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { newFocus.requestFocus() }),
                trailingIcon = {
                    TextButton(onClick = { show = !show }) { Text(if (show) "Ocultar" else "Ver") }
                }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text("Nueva contraseña") },
                modifier = Modifier.fillMaxWidth().focusRequester(newFocus),
                isError = newPass.isNotBlank() && !newOk,
                supportingText = {
                    if (newPass.isNotBlank() && !newOk) Text("Mínimo 6 caracteres")
                },
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { confirmFocus.requestFocus() }),
                trailingIcon = {
                    TextButton(onClick = { show = !show }) { Text(if (show) "Ocultar" else "Ver") }
                }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirmar nueva contraseña") },
                modifier = Modifier.fillMaxWidth().focusRequester(confirmFocus),
                isError = confirm.isNotBlank() && !matchOk,
                supportingText = {
                    if (confirm.isNotBlank() && !matchOk) Text("No coincide")
                },
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                trailingIcon = {
                    TextButton(onClick = { show = !show }) { Text(if (show) "Ocultar" else "Ver") }
                }
            )

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar (mock)") }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var biometricEnabled by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Modo oscuro")
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode(it) }
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Biometría")
                    Text("Usar huella/rostro (mock)", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = biometricEnabled, onCheckedChange = { biometricEnabled = it })
            }

            Spacer(Modifier.height(30.dp))

            Button(onClick = onBack) { Text("Volver") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditProcessScreen(
    existing: ProcessItem?,
    onSave: (ProcessItem) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: ProcessStatus.PENDIENTE) }

    val canSave = title.trim().isNotBlank()

    val focusManager = LocalFocusManager.current
    val descFocusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Crear proceso" else "Editar proceso") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título *") },
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
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth().focusRequester(descFocusRequester),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(Modifier.height(16.dp))
            Text("Estado", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = status == ProcessStatus.PENDIENTE,
                    onClick = { status = ProcessStatus.PENDIENTE },
                    label = { Text("Pendiente") }
                )
                FilterChip(
                    selected = status == ProcessStatus.EN_ESPERA,
                    onClick = { status = ProcessStatus.EN_ESPERA },
                    label = { Text("En espera") }
                )
                FilterChip(
                    selected = status == ProcessStatus.COMPLETADO,
                    onClick = { status = ProcessStatus.COMPLETADO },
                    label = { Text("Completado") }
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
                        groups = existing?.groups ?: listOf(TaskGroup())
                    )
                    onSave(item)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave
            ) {
                Text("Guardar")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
