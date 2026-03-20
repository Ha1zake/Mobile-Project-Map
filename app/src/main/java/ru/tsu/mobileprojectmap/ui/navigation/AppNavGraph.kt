package ru.tsu.mobileprojectmap.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.tsu.mobileprojectmap.ui.screens.home.HomeScreen
import ru.tsu.mobileprojectmap.ui.screens.map.MapScreen

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                onOpenMap = {
                    navController.navigate(Routes.Map.route)
                }
            )
        }

        composable(Routes.Map.route) {
            MapScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}