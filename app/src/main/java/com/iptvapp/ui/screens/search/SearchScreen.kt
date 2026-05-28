package com.iptvapp.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.iptvapp.ui.components.ChannelCard
import com.iptvapp.ui.navigation.Screen
import com.iptvapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    vm: SearchViewModel = hiltViewModel()
) {
    val query by vm.query.collectAsStateWithLifecycle()
    val results by vm.results.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Column(
        Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = vm::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Buscar canales, categorías…", color = OnDarkSubtle) },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = OnDarkMuted) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { vm.onQueryChange("") }) {
                        Icon(Icons.Filled.Clear, "Limpiar", tint = OnDarkMuted)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NetflixRed,
                unfocusedBorderColor = SurfaceElevated,
                focusedTextColor = OnDark,
                unfocusedTextColor = OnDark,
                cursorColor = NetflixRed,
                unfocusedContainerColor = SurfaceCard,
                focusedContainerColor = SurfaceCard
            ),
            shape = MaterialTheme.shapes.large
        )

        when {
            query.isBlank() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Escribe para buscar canales",
                        color = OnDarkMuted,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            results.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Sin resultados para \"$query\"",
                        color = OnDarkMuted,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "${results.size} resultado${if (results.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelLarge,
                            color = OnDarkMuted,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    items(results, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            onClick = { navController.navigate(Screen.Player.buildRoute(channel.id)) },
                            onFavoriteToggle = { vm.toggleFavorite(channel) }
                        )
                    }
                }
            }
        }
    }
}
