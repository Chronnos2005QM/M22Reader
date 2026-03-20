package com.m22reader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Library   : Screen("library",   "Biblioteca", Icons.Default.LibraryBooks)
    object Updates   : Screen("updates",   "Novidades",  Icons.Default.Update)
    object History   : Screen("history",   "Histórico",  Icons.Default.History)
    object Favorites : Screen("favorites", "Favoritos",  Icons.Default.Favorite)
}

val bottomNavItems = listOf(Screen.Library, Screen.Updates, Screen.History, Screen.Favorites)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M22AppScaffold(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onImportFile: () -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isReaderScreen     = currentRoute?.startsWith("reader/") == true
    val isChaptersScreen   = currentRoute?.startsWith("collection/") == true
    val isSettingsScreen   = currentRoute == "settings"
    val hideBottomAndTop   = isReaderScreen

    Scaffold(
        topBar = {
            if (!hideBottomAndTop) {
                TopAppBar(
                    title = {
                        Text("M22 Reader", fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary)
                    },
                    actions = {
                        IconButton(onClick = onImportFile) { Icon(Icons.Default.Add, "Importar") }
                        IconButton(onClick = onToggleTheme) {
                            Icon(if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, "Tema")
                        }
                        var menuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Default.MoreVert, "Mais") }
                            DropdownMenu(menuExpanded, { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Configurações") },
                                    leadingIcon = { Icon(Icons.Default.Settings, null) },
                                    onClick = { menuExpanded = false; navController.navigate("settings") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sobre") },
                                    leadingIcon = { Icon(Icons.Default.Info, null) },
                                    onClick = { menuExpanded = false }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        },
        bottomBar = {
            if (!hideBottomAndTop && !isSettingsScreen && !isChaptersScreen) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon  = { Icon(screen.icon, null) },
                            label = { Text(screen.label, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        M22NavHost(navController, Modifier.padding(paddingValues))
    }
}

@Composable
fun M22NavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController, startDestination = Screen.Library.route, modifier = modifier) {
        composable(Screen.Library.route) {
            com.m22reader.ui.library.LibraryScreen(
                onBookClick       = { id -> navController.navigate("reader/book/$id") },
                onCollectionClick = { id -> navController.navigate("collection/$id") }
            )
        }
        composable(Screen.Updates.route) {
            com.m22reader.ui.updates.UpdatesScreen(onBookClick = { id -> navController.navigate("reader/book/$id") })
        }
        composable(Screen.History.route) {
            com.m22reader.ui.history.HistoryScreen(onBookClick = { id -> navController.navigate("reader/book/$id") })
        }
        composable(Screen.Favorites.route) {
            com.m22reader.ui.favorites.FavoritesScreen(onBookClick = { id -> navController.navigate("reader/book/$id") })
        }
        composable("settings") {
            com.m22reader.ui.settings.SettingsScreen(onBack = { navController.popBackStack() })
        }
        // Ecrã de capítulos de uma coleção
        composable("collection/{collectionId}") { backStack ->
            val id = backStack.arguments?.getString("collectionId")?.toLongOrNull() ?: return@composable
            com.m22reader.ui.chapters.ChaptersScreen(
                collectionId  = id,
                onChapterClick = { chapId -> navController.navigate("reader/chapter/$chapId") },
                onBack        = { navController.popBackStack() }
            )
        }
        // Leitor de ficheiro avulso
        composable("reader/book/{bookId}") { backStack ->
            val id = backStack.arguments?.getString("bookId")?.toLongOrNull() ?: return@composable
            com.m22reader.ui.reader.ReaderScreen(bookId = id, onBack = { navController.popBackStack() })
        }
        // Leitor de capítulo de coleção
        composable("reader/chapter/{chapterId}") { backStack ->
            val id = backStack.arguments?.getString("chapterId")?.toLongOrNull() ?: return@composable
            com.m22reader.ui.reader.ReaderScreen(bookId = id, onBack = { navController.popBackStack() })
        }
    }
}
