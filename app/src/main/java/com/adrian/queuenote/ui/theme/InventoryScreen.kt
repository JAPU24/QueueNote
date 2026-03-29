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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Text(appStrings.app_name, style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp))
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(label = { Text(appStrings.home_title) }, selected = false, onClick = { scope.launch { drawerState.close() }; onGoHome() }, modifier = Modifier.padding(horizontal = 12.dp), shape = MaterialTheme.shapes.medium)
                NavigationDrawerItem(label = { Text(appStrings.inventory_title) }, selected = true, onClick = { scope.launch { drawerState.close() } }, modifier = Modifier.padding(horizontal = 12.dp), shape = MaterialTheme.shapes.medium)
                NavigationDrawerItem(label = { Text(appStrings.profile_title) }, selected = false, onClick = { scope.launch { drawerState.close() }; onGoProfile() }, modifier = Modifier.padding(horizontal = 12.dp), shape = MaterialTheme.shapes.medium)
                NavigationDrawerItem(label = { Text(appStrings.settings_title) }, selected = false, onClick = { scope.launch { drawerState.close() }; onGoSettings() }, modifier = Modifier.padding(horizontal = 12.dp), shape = MaterialTheme.shapes.medium)
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(label = { Text(appStrings.logout, color = MaterialTheme.colorScheme.error) }, selected = false, onClick = { scope.launch { drawerState.close() }; onLogout() }, modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp), shape = MaterialTheme.shapes.medium)
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(appStrings.inventory_title) },
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = null) } },
                    actions = {
                        if (articulos.isNotEmpty()) {
                            IconButton(onClick = { 
                                val file = PdfHelper.generateInventoryPdf(context, articulos)
                                if (file != null) PdfHelper.exportPdfToDownloads(context, file)
                            }) { Icon(Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }

                            IconButton(onClick = { 
                                val file = PdfHelper.generateInventoryPdf(context, articulos)
                                if (file != null) PdfHelper.sharePdf(context, file)
                            }) { Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumStatusCard(Icons.Default.Wifi, "WIFI", isWifiOn, Modifier.weight(1f)) {
                        context.startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
                    }
                    PremiumStatusCard(Icons.Default.Bluetooth, "BT", isBluetoothOn, Modifier.weight(1f)) {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    }
                    PremiumStatusCard(Icons.Default.LocationOn, "LOC", isLocationOn, Modifier.weight(1f)) {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                if (articulos.isNotEmpty()) {
                    InventoryDashboardPremium(articulos, appStrings)
                    Spacer(Modifier.height(16.dp))
                }

                PremiumTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    trailingIcon = { 
                        IconButton(onClick = { performSearch() }) { 
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) 
                        } 
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { performSearch() })
                )
                
                Spacer(Modifier.height(16.dp))
                
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(articulos) { PremiumArticuloCard(it, appStrings) }
                        item { Spacer(Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumStatusCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isOn: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (isOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PremiumArticuloCard(articulo: Articulo, appStrings: AppStrings) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(70.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                if (articulo.imagenUrl != null) {
                    AsyncImage(model = articulo.imagenUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(32.dp)) 
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(articulo.nombre ?: "Unnamed Item", style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp), maxLines = 1)
                Spacer(Modifier.height(4.dp))
                
                // RESTORED: Article details from API (Professor's logic)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${appStrings.cant_label}: ${articulo.unidadesInt}", style = MaterialTheme.typography.bodyLarge)
                        Text("${appStrings.cost}: $${String.format("%.2f", articulo.costoDouble)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${appStrings.price}: $${String.format("%.2f", articulo.precioDouble)}", style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, color = MaterialTheme.colorScheme.primary))
                        Text("${appStrings.profit}: $${String.format("%.2f", articulo.beneficio)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if(articulo.beneficio >= 0) SuccessGreen else ErrorRed)
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryDashboardPremium(articulos: List<Articulo>, appStrings: AppStrings) {
    val totalInv = articulos.sumOf { it.unidadesInt }
    val totalPrecio = articulos.sumOf { it.precioDouble * (if(it.unidadesInt > 0) it.unidadesInt else 1) }
    val totalCosto = articulos.sumOf { it.costoDouble * (if(it.unidadesInt > 0) it.unidadesInt else 1) }
    val totalBen = totalPrecio - totalCosto

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(appStrings.dashboard_title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(appStrings.total_stock, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(totalInv.toString(), style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp))
                }
                Column {
                    Text(appStrings.profit, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%.0f", totalBen)}", style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp, color = if(totalBen >= 0) SuccessGreen else ErrorRed))
                }
            }
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(appStrings.investment, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%.0f", totalCosto)}", style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
                }
                Column {
                    Text(appStrings.sell_value, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%.0f", totalPrecio)}", style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
                }
            }
        }
    }
}
