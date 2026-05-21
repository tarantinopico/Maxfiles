package com.example.ui.navigation

sealed class Screen(val route: String) {
    object FileBrowser : Screen("file_browser")
    object Editor : Screen("editor")
    object Settings : Screen("settings")
    object About : Screen("about")
}
