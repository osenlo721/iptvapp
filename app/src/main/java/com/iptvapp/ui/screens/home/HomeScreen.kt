package com.iptvapp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.iptvapp.ui.components.CategoryTabs
import com.iptvapp.ui.components.ChannelCard
import com.iptvapp.ui.navigation.Screen
import com.iptvapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("") }

    // Show snackbar messages
    LaunchedEffect(state.snackbar) {
        state.snackbar?.let {
            snackbarHost.showSnackbar(it)
            vm.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "IPTV",
                        style = MaterialTheme.typography.headlineMedium.copy(color = NetflixRed)
                    )
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Filled.Add, "Cargar M3U", tint = OnDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = OnDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundDark)
        ) {
            // Category tabs
            if (state.categories.isNotEmpty()) {
                CategoryTabs(
                    categories = state.categories,
                    selected = state.selectedCategory,
                    onSelect = vm::selectCategory
                )
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = NetflixRed)
                            Spacer(Modifier.height(12.dp))
                            Text("Cargando canales…", color = OnDarkMuted)
                        }
                    }
                }

                state.channels.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No hay canales disponibles.\nCarga una lista M3U con el botón +",
                                color = OnDarkMuted,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { showDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
                            ) {
                                Icon(Icons.Filled.Add, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Cargar lista M3U")
                            }
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                "${state.channels.size} canales • ${state.selectedCategory}",
                                style = MaterialTheme.typography.labelLarge,
                                color = OnDarkMuted,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                        items(state.channels, key = { it.id }) { channel ->
                            ChannelCard(
                                channel = channel,
                                onClick = {
                                    navController.navigate(Screen.Player.buildRoute(channel.id))
                                },
                                onFavoriteToggle = { vm.toggleFavorite(channel) }
                            )
                        }
                    }
                }
            }
        }
    }

    // M3U URL dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = SurfaceCard,
            title = { Text("Cargar lista M3U", color = OnDark) },
            text = {
                Column {
                    Text("Ingresa la URL de tu lista M3U o M3U8:", color = OnDarkMuted,
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("https://...", color = OnDarkSubtle) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NetflixRed,
                            unfocusedBorderColor = SurfaceElevated,
                            focusedTextColor = OnDark,
                            unfocusedTextColor = OnDark,
                            cursorColor = NetflixRed
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            vm.loadM3u(urlInput.trim())
                            urlInput = ""
                            showDialog = false
                        }
                    }
                ) { Text("Cargar", color = NetflixRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; urlInput = "" }) {
                    Text("Cancelar", color = OnDarkMuted)
                }
            }
        )
    }
}
