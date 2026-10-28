package com.turbomaster.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Dashboard")
    object Apps : Screen("apps", "App Manager")
    object Actions : Screen("actions", "Turbo Tools")
    object Advanced : Screen("advanced", "System Tuning")
    object Shizuku : Screen("shizuku", "Shizuku Setup")
    object History : Screen("history", "Operation History")
    object About : Screen("about", "Diagnostics")
}
