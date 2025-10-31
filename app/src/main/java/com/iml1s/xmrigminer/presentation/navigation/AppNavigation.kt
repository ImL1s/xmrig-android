package com.iml1s.xmrigminer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.iml1s.xmrigminer.presentation.config.ConfigScreen
import com.iml1s.xmrigminer.presentation.mining.MiningScreen

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Mining.route
    ) {
        composable(Screen.Mining.route) {
            MiningScreen(
                onNavigateToConfig = {
                    navController.navigate(Screen.Config.route)
                }
            )
        }

        composable(Screen.Config.route) {
            ConfigScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // TODO: Add Stats and Settings screens
    }
}
