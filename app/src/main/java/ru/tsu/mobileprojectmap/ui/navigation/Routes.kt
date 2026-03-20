package ru.tsu.mobileprojectmap.ui.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Map : Routes("map")
}
