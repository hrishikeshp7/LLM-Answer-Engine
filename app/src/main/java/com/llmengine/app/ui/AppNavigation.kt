package com.llmengine.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.llmengine.app.ui.chat.ChatScreen
import com.llmengine.app.ui.models.ModelManagerScreen
import com.llmengine.app.ui.settings.SettingsScreen
import com.llmengine.app.ui.setup.SetupScreen

/**
 * Navigation routes for the app.
 */
object Routes {
    const val SETUP = "setup"
    const val CHAT = "chat"
    const val MODELS = "models"
    const val SETTINGS = "settings"
}

/**
 * Main navigation graph for the application.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SETUP
    ) {
        composable(Routes.SETUP) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CHAT) {
            ChatScreen(
                onNavigateToModels = { navController.navigate(Routes.MODELS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.MODELS) {
            ModelManagerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
