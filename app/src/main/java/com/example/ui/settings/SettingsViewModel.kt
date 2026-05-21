package com.example.ui.settings

import androidx.lifecycle.ViewModel
import com.example.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    private val _useSystemTheme = MutableStateFlow(repository.useSystemTheme)
    val useSystemTheme: StateFlow<Boolean> = _useSystemTheme.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(repository.isDarkTheme)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _showHiddenFiles = MutableStateFlow(repository.showHiddenFiles)
    val showHiddenFiles: StateFlow<Boolean> = _showHiddenFiles.asStateFlow()

    private val _highContrastCode = MutableStateFlow(repository.highContrastCode)
    val highContrastCode: StateFlow<Boolean> = _highContrastCode.asStateFlow()

    private val _gitToken = MutableStateFlow(repository.gitToken)
    val gitToken: StateFlow<String> = _gitToken.asStateFlow()

    private val _gitUsername = MutableStateFlow(repository.gitUsername)
    val gitUsername: StateFlow<String> = _gitUsername.asStateFlow()

    private val _fontSize = MutableStateFlow(repository.fontSize)
    val fontSize: StateFlow<Int> = _fontSize.asStateFlow()

    fun updateUseSystemTheme(value: Boolean) {
        repository.useSystemTheme = value
        _useSystemTheme.value = value
    }

    fun updateIsDarkTheme(value: Boolean) {
        repository.isDarkTheme = value
        _isDarkTheme.value = value
    }

    fun updateShowHiddenFiles(value: Boolean) {
        repository.showHiddenFiles = value
        _showHiddenFiles.value = value
    }

    fun updateHighContrastCode(value: Boolean) {
        repository.highContrastCode = value
        _highContrastCode.value = value
    }

    fun updateGitToken(value: String) {
        repository.gitToken = value
        _gitToken.value = value
    }

    fun updateGitUsername(value: String) {
        repository.gitUsername = value
        _gitUsername.value = value
    }

    fun updateFontSize(value: Int) {
        repository.fontSize = value
        _fontSize.value = value
    }
}
