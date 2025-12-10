// ==========================================
// 📄 ARCHIVO: NavGraph.kt
// 📁 UBICACIÓN: presentation/ui/navigation/
// 🔷 TIPO: Object (Navigation)
// ==========================================

package com.example.newsapp.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.newsapp.presentation.ui.screens.chat.ChatScreen
import com.example.newsapp.presentation.ui.screens.config.ConfigScreen
import com.example.newsapp.presentation.ui.screens.home.HomeScreen

/**
 * Rutas de navegación de la app
 */
sealed class Screen(val route: String) {
    object Chat : Screen("chat")
    object Home : Screen("home")
    object Config : Screen("config")
}

/**
 * Grafo de navegación principal
 * Define todas las pantallas y las conexiones entre ellas
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route // Inicia en el chat
    ) {
        // ===== PANTALLA: CHAT =====
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        // Evita múltiples instancias de Home en el stack
                        launchSingleTop = true
                    }
                },
                onNavigateToConfig = {
                    navController.navigate(Screen.Config.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ===== PANTALLA: HOME =====
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToConfig = {
                    navController.navigate(Screen.Config.route) {
                        launchSingleTop = true
                    }
                },
                onNoticiaClick = { noticia ->
                    // TODO: Implementar pantalla de detalle si es necesario
                    // Por ahora, las noticias se expanden en su lugar
                }
            )
        }

        // ===== PANTALLA: CONFIGURACIÓN =====
        composable(Screen.Config.route) {
            ConfigScreen(
                onNavigateBack = {
                    // Vuelve a la pantalla anterior
                    navController.popBackStack()
                }
            )
        }
    }
}

// ==========================================
// FIN DE ARCHIVO NavGraph.kt
// ==========================================