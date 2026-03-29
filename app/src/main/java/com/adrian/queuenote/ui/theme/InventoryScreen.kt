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
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.adrian.queuenote.Articulo
import com.adrian.queuenote.InventoryRepository
import com.adrian.queuenote.NetworkUtils
import com.adrian.queuenote.PdfHelper
import com.adrian.queuenote.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onGoHome: () -> Unit,
    onGoProfile: () -> Unit,
    onGoSettings: () -> Unit,
    onLogout: () -> Unit,
    appStrings: AppStrings
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val repository = remember { InventoryRepository() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var searchQuery by remember { mutableStateOf("") }
    var articulos by remember { mutableStateOf<List<Articulo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    var isWifiOn by remember { mutableStateOf(NetworkUtils.isInternetAvailable(context)) }
    var isBluetoothOn by remember { mutableStateOf(false) }
    var isLocationOn by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isWifiOn = true }
            override fun onLost(network: Network) { isWifiOn = false }
        }
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        cm.registerNetworkCallback(request, networkCallback)
        
        isBluetoothOn = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isLocationOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when(intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> isBluetoothOn = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
                    LocationManager.PROVIDERS_CHANGED_ACTION -> isLocationOn = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, filter)

        onDispose { 
            cm.unregisterNetworkCallback(networkCallback)
            context.unregisterReceiver(receiver)
        }
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
                Text(appStrings.app_name, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(label = { Text(appStrings.home_title) }, selected = false, onClick = { scope.launch { drawerState.close() }; onGoHome() })
                NavigationDrawerItem(label = { Text(appStrings.inventory_title) }, selected = true, onClick = { scope.launch { drawerState.close() } })
                NavigationDrawerItem(label = { Text(appStrings.profile_title) }, selected = false, onClick = { scope.launch { drawerState.close() }; onGoProfile() })
                NavigationDrawerItem(label = { Text(appStrings.settings_title) }, selected = false, onClick = { scope.launch { drawerState.close() }; onGoSettings() })
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(label = { Text(appStrings.logout) }, selected = false, onClick = { scope.launch { drawerState.close() }; onLogout() })
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(appStrings.inventory_title) },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Menú") } },
                    actions = {
                        if (articulos.isNotEmpty()) {
                            IconButton(onClick = { 
                                val file = PdfHelper.generateInventoryPdf(context, articulos)
                                if (file != null) PdfHelper.exportPdfToDownloads(context, file)
                            }) { Icon(Icons.Default.FileDownload, contentDescription = "Exportar") }

                            IconButton(onClick = {
                                val file = PdfHelper.generateInventoryPdf(context, articulos)
                                if (file != null) PdfHelper.printPdf(context, file)
                            }) { Icon(Icons.Default.Print, contentDescription = "Imprimir") }

                            IconButton(onClick = {
                                val file = PdfHelper.generateInventoryPdf(context, articulos)
                                if (file != null) PdfHelper.sharePdf(context, file)
                            }) { Icon(Icons.Default.Share, contentDescription = "Compartir") }
                        }
                    }
                )
            },
            bottomBar = { if (articulos.isNotEmpty()) InventoryDashboard(articulos, appStrings) }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    InteractiveStatusCard(Icons.Default.Wifi, "WIFI", isWifiOn) {
                        context.startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
                    }
                    InteractiveStatusCard(Icons.Default.Bluetooth, "BT", isBluetoothOn) {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    }
                    InteractiveStatusCard(Icons.Default.LocationOn, "LOC", isLocationOn) {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text(appStrings.search_placeholder) },
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
                        items(articulos) { ArticuloCard(it, appStrings) }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveStatusCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isOn: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(90.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, label, tint = if (isOn) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(if (isOn) "ON" else "OFF", style = MaterialTheme.typography.labelSmall, color = if (isOn) MaterialTheme.colorScheme.primary else Color.Gray)
        }
    }
}

@Composable
fun ArticuloCard(articulo: Articulo, appStrings: AppStrings) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(80.dp), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                if (articulo.imagenUrl != null) {
                    AsyncImage(model = articulo.imagenUrl, contentDescription = articulo.nombre, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(articulo.nombre ?: "Sin nombre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${appStrings.cant_label}: ${articulo.unidadesInt}")
                        Text("${appStrings.cost}: $${String.format("%.2f", articulo.costoDouble)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${appStrings.price}: $${String.format("%.2f", articulo.precioDouble)}", color = MaterialTheme.colorScheme.primary)
                        Text("${appStrings.profit}: $${String.format("%.2f", articulo.beneficio)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if(articulo.beneficio >= 0) Color(0xFF4CAF50) else Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryDashboard(articulos: List<Articulo>, appStrings: AppStrings) {
    val totalCant = articulos.size
    val totalInv = articulos.sumOf { it.unidadesInt }
    val totalPrecio = articulos.sumOf { it.precioDouble * (if(it.unidadesInt > 0) it.unidadesInt else 1) }
    val totalCosto = articulos.sumOf { it.costoDouble * (if(it.unidadesInt > 0) it.unidadesInt else 1) }
    val totalBen = totalPrecio - totalCosto

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DashboardItem(appStrings.items_label, totalCant.toString(), Modifier.weight(1f))
                DashboardItem("${appStrings.stock} Total", totalInv.toString(), Modifier.weight(1f))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DashboardItem(appStrings.sell_value, "$${String.format("%.0f", totalPrecio)}", Modifier.weight(1f))
                DashboardItem(appStrings.investment, "$${String.format("%.0f", totalCosto)}", Modifier.weight(1f))
                DashboardItem(appStrings.profit, "$${String.format("%.0f", totalBen)}", Modifier.weight(1f), isHighlight = true)
            }
        }
    }
}

@Composable
fun DashboardItem(label: String, value: String, modifier: Modifier = Modifier, isHighlight: Boolean = false) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
        Text(
            value, 
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isHighlight) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center
        )
    }
}
