package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer
import com.example.ui.filebrowser.FileBrowserViewModel
import com.example.ui.editor.EditorViewModel

class AppViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileBrowserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FileBrowserViewModel(appContainer.fileRepository, appContainer.settingsRepository) as T
        }
        if (modelClass.isAssignableFrom(EditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditorViewModel(appContainer.fileRepository, appContainer.settingsRepository) as T
        }
        if (modelClass.isAssignableFrom(com.example.ui.settings.SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return com.example.ui.settings.SettingsViewModel(appContainer.settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
