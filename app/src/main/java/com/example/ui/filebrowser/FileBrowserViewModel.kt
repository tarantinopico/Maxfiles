package com.example.ui.filebrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileRepository
import com.example.domain.models.FileItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class FileBrowserViewModel(private val repository: FileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FileBrowserUiState(currentPath = repository.getRootPath()))
    val uiState: StateFlow<FileBrowserUiState> = _uiState.asStateFlow()

    init {
        loadDirectory(repository.getRootPath())
    }

    fun loadDirectory(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, currentPath = path, errorMessage = null)
            val result = repository.getFiles(path)
            result.onSuccess { files ->
                val breadcrumbs = generateBreadcrumbs(path)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    files = files,
                    breadcrumbs = breadcrumbs
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
            }
        }
    }

    private fun generateBreadcrumbs(path: String): List<String> {
        val root = repository.getRootPath()
        if (path == root) return listOf("Internal Storage")
        
        val relative = path.removePrefix(root).removePrefix("/")
        val parts = relative.split("/").filter { it.isNotEmpty() }
        return listOf("Internal Storage") + parts
    }

    fun navigateUp() {
        val currentPath = _uiState.value.currentPath
        if (currentPath != repository.getRootPath()) {
            val parentFile = File(currentPath).parentFile
            if (parentFile != null) {
                loadDirectory(parentFile.absolutePath)
            }
        }
    }
}

data class FileBrowserUiState(
    val files: List<FileItem> = emptyList(),
    val breadcrumbs: List<String> = emptyList(),
    val currentPath: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
