package com.iptvapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptvapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var urlInput by remember(state.savedUrl) { mutableStateOf(state.savedUrl) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let {
            snackbar.showSnackbar(it)
            vm.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", color = OnDark) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundDark)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // M3U Source section
            SectionTitle("Fuente M3U")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("URL de la lista M3U", style = MaterialTheme.typography.titleSmall, color = OnDark)
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("https://lista.m3u8", color = OnDarkSubtle) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NetflixRed,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark,
                            cursorColor = NetflixRed
                        )
                    )
                    if (state.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = NetflixRed,
                            trackColor = SurfaceElevated
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { if (urlInput.isNotBlank()) vm.loadM3u(urlInput.trim()) },
                            enabled = urlInput.isNotBlank() && !state.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = NetflixRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cargar lista")
                        }
                    }
                }
            }

            // Stats section
            SectionTitle("Estadísticas")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                SettingsRow(
                    icon = Icons.Filled.Tv,
                    label = "Canales en caché",
                    value = "${state.channelCount}"
                )
            }

            // Data management
            SectionTitle("Datos")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                SettingsAction(
                    icon = Icons.Outlined.DeleteOutline,
                    label = "Borrar todos los datos",
                    description = "Elimina canales y lista guardada",
                    isDestructive = true,
                    onClick = { showClearDialog = true }
                )
            }

            // About
            SectionTitle("Acerca de")
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    SettingsRow(icon = Icons.Filled.Info, label = "Versión", value = "1.0.0")
                    HorizontalDivider(color = SurfaceElevated, thickness = 0.5.dp)
                    SettingsRow(
                        icon = Icons.Filled.Stream,
                        label = "Formatos soportados",
                        value = "HLS · DASH · MP4"
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = SurfaceCard,
            title = { Text("Borrar datos", color = OnDark) },
            text = { Text("Se eliminarán todos los canales y favoritos. Esta acción no se puede deshacer.", color = OnDarkMuted) },
            confirmButton = {
                TextButton(onClick = { vm.clearData(); showClearDialog = false }) {
                    Text("Borrar", color = LiveRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancelar", color = OnDarkMuted) }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = NetflixRed,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = OnDarkMuted, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnDark, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = OnDarkMuted)
    }
}

@Composable
private fun SettingsAction(
    icon: ImageVector,
    label: String,
    description: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .run { if (isDestructive) this else this }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null,
            tint = if (isDestructive) LiveRed else OnDarkMuted,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = if (isDestructive) LiveRed else OnDark)
            Text(description, style = MaterialTheme.typography.bodySmall, color = OnDarkMuted)
        }
        TextButton(onClick = onClick) {
            Text(if (isDestructive) "Borrar" else "Acción",
                color = if (isDestructive) LiveRed else NetflixRed)
        }
    }
}
