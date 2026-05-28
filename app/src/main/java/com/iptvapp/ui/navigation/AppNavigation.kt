package com.iptvapp.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.iptvapp.ui.screens.favorites.FavoritesScreen
import com.iptvapp.ui.screens.home.HomeScreen
import com.iptvapp.ui.screens.player.PlayerScreen
import com.iptvapp.ui.screens.search.SearchScreen
import com.iptvapp.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home      : Screen("home")
    object Search    : Screen("search")
    object Favorites : Screen("favorites")
    object Settings  : Screen("settings")
    object Player    : Screen("player/{channelId}") {
        fun buildRoute(id: String) = "player/${Uri.encode(id)}"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

private val bottomItems = listOf(
    BottomNavItem(Screen.Home,      "Inicio",    Icons.Filled.Home,          Icons.Outlined.Home),
    BottomNavItem(Screen.Search,    "Buscar",    Icons.Filled.Search,        Icons.Outlined.Search),
    BottomNavItem(Screen.Favorites, "Favoritos", Icons.Filled.Favorite,      Icons.Outlined.FavoriteBorder),
    BottomNavItem(Screen.Settings,  "Ajustes",   Icons.Filled.Settings,      Icons.Outlined.Settings)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val showBottomBar = currentRoute?.startsWith("player/") == false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = com.iptvapp.ui.theme.SurfaceDark) {
                    bottomItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = com.iptvapp.ui.theme.OnDarkMuted,
                                unselectedTextColor = com.iptvapp.ui.theme.OnDarkMuted,
                                indicatorColor = com.iptvapp.ui.theme.SurfaceElevated
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = if (showBottomBar) Modifier.padding(innerPadding) else Modifier
        ) {
            composable(Screen.Home.route)      { HomeScreen(navController) }
            composable(Screen.Search.route)    { SearchScreen(navController) }
            composable(Screen.Favorites.route) { FavoritesScreen(navController) }
            composable(Screen.Settings.route)  { SettingsScreen() }
            composable(
                route = Screen.Player.route,
                arguments = listOf(navArgument("channelId") { type = NavType.StringType })
            ) { backStack ->
                val channelId = Uri.decode(backStack.arguments?.getString("channelId") ?: "")
                PlayerScreen(channelId = channelId, onBack = { navController.popBackStack() })
            }
        }
    }
}
