package com.adrian.queuenote.ui.theme

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adrian.queuenote.Articulo
import com.adrian.queuenote.InventoryRepository
import com.adrian.queuenote.NetworkUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onGoHome: () -> Unit,
    onGoProfile: () -> Unit,
    onGoSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val repository = remember { InventoryRepository() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var searchQuery by remember { mutableStateOf("") }
    var articulos by remember { mutableStateOf<List<Articulo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Estados Native
    var isWifiOn by remember { mutableStateOf(NetworkUtils.isInternetAvailable(context)) }
    var isBluetoothOn by remember { mutableStateOf(false) }
    var isLocationOn by remember { mutableStateOf(false) }

    // --- LÓGICA DE ACTUALIZACIÓN EN TIEMPO REAL ---
    
    // 1. WiFi / Internet
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isWifiOn = true }
            override fun onLost(network: Network) { isWifiOn = false }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, networkCallback)
        onDispose { cm.unregisterNetworkCallback(networkCallback) }
    }

    // 2. Bluetooth
    DisposableEffect(context) {
        isBluetoothOn = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    isBluetoothOn = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    // 3. Ubicación
    DisposableEffect(context) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isLocationOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                       lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    isLocationOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
                                   lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        onDispose { context.unregisterReceiver(receiver) }
    }

    fun performSearch() {
        if (!NetworkUtils.isInternetAvailable(context)) {
            Toast.makeText(context, "No hay conexión a internet.", Toast.LENGTH_LONG).show()
            return
        }
        if (searchQuery.length < 3) {
            Toast.makeText(context, "Escribe al menos 3 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            isLoading = true
            val result = repository.getArticulos(searchQuery)
            isLoading = false
            if (result.isSuccess) {
                articulos = result.getOrDefault(emptyList())
            } else {
                Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
        focusManager.clearFocus()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("QueueNote", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Menú principal") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onGoHome() }
                )
                NavigationDrawerItem(
                    label = { Text("Inventario") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Mi perfil") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onGoProfile() }
                )
                NavigationDrawerItem(
                    label = { Text("Ajustes") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onGoSettings() }
                )
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onLogout() }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Inventario") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            },
            bottomBar = { if (articulos.isNotEmpty()) InventorySummaryBar(articulos) }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    NativeStatusIcon(Icons.Default.Wifi, "WIFI", isWifiOn)
                    NativeStatusIcon(Icons.Default.Bluetooth, "BT", isBluetoothOn)
                    NativeStatusIcon(Icons.Default.LocationOn, "LOC", isLocationOn)
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Buscar artículo...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = { IconButton(onClick = { performSearch() }) { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.primary) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performSearch() })
                )
                Spacer(Modifier.height(8.dp))
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(articulos) { ArticuloCard(it) }
                    }
                }
            }
        }
    }
}

@Composable
fun NativeStatusIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isOn: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isOn) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
        Text(label, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
        Text(if (isOn) "ON" else "OFF", style = MaterialTheme.typography.labelSmall, color = if (isOn) Color(0xFF4CAF50) else Color.Gray)
    }
}

@Composable
fun ArticuloCard(articulo: Articulo) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(80.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                if (articulo.imagenUrl != null) {
                    AsyncImage(
                        model = articulo.imagenUrl, 
                        contentDescription = articulo.nombre, 
                        contentScale = ContentScale.Crop, 
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(articulo.nombre ?: "Sin nombre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Cant: ${articulo.unidadesInt}")
                        Text("Costo: $${String.format("%.2f", articulo.costoDouble)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Precio: $${String.format("%.2f", articulo.precioDouble)}", color = MaterialTheme.colorScheme.primary)
                        Text("Ben: $${String.format("%.2f", articulo.beneficio)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventorySummaryBar(articulos: List<Articulo>) {
    val totalCant = articulos.size
    val totalInv = articulos.sumOf { it.unidadesInt }
    val totalPrecio = articulos.sumOf { it.precioDouble * (if(it.unidadesInt > 0) it.unidadesInt else 1) }
    val totalCosto = articulos.sumOf { it.costoDouble * (if(it.unidadesInt > 0) it.unidadesInt else 1) }
    val totalBen = totalPrecio - totalCosto

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Resumen de Consulta", 
                style = MaterialTheme.typography.titleSmall, 
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SummaryItem("Cantidad", totalCant.toString())
                SummaryItem("Inventario", totalInv.toString())
                SummaryItem("Precio", "$${String.format("%.2f", totalPrecio)}")
                SummaryItem("Costo", "$${String.format("%.2f", totalCosto)}")
                SummaryItem("Beneficio", "$${String.format("%.2f", totalBen)}", isHighlight = true)
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, isHighlight: Boolean = false) {
    Column(modifier = Modifier.padding(end = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    }
}
