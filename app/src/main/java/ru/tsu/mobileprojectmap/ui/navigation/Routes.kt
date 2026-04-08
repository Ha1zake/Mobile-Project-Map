package ru.tsu.mobileprojectmap.ui.navigation

sealed class Routes(val route: String) {
    data object Map : Routes("map")
}
