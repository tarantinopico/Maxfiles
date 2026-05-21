package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.AppContainer
import com.example.ui.navigation.Screen
import com.example.ui.filebrowser.FileBrowserScreen
import com.example.ui.editor.EditorScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.about.AboutScreen

@Composable
fun MaxFilesApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(appContainer)

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.FileBrowser.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.FileBrowser.route) { 
                FileBrowserScreen(
                    viewModel = viewModel(factory = factory), 
                    onNavigateToEditor = { path ->
                        val encodedPath = android.net.Uri.encode(path)
                        navController.navigate("${Screen.Editor.route}?path=$encodedPath")
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    }
                ) 
            }
            composable(
                route = "${Screen.Editor.route}?path={path}",
                arguments = listOf(androidx.navigation.navArgument("path") { 
                    type = androidx.navigation.NavType.StringType 
                    nullable = true 
                })
            ) { backStackEntry -> 
                val path = backStackEntry.arguments?.getString("path")
                EditorScreen(path, viewModel(factory = factory), onNavigateBack = { navController.popBackStack() }) 
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel(factory = factory), onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.About.route) {
                AboutScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
