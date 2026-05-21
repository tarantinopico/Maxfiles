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
import androidx.compose.runtime.mutableStateListOf

class FileBrowserViewModel(private val repository: FileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FileBrowserUiState(currentPath = repository.getRootPath()))
    val uiState: StateFlow<FileBrowserUiState> = _uiState.asStateFlow()

    private val _favorites = MutableStateFlow(repository.getFavorites())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

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

    fun reloadCurrentDirectory() {
        if (_uiState.value.currentPath == "Favorites") {
            loadFavorites()
        } else {
            loadDirectory(_uiState.value.currentPath)
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
        if (currentPath == "Favorites") {
            loadHome()
            return
        }
        if (currentPath != repository.getRootPath()) {
            val parentFile = File(currentPath).parentFile
            if (parentFile != null) {
                loadDirectory(parentFile.absolutePath)
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            repository.createFile(_uiState.value.currentPath, name, true)
            reloadCurrentDirectory()
        }
    }

    fun createFile(name: String) {
        viewModelScope.launch {
            repository.createFile(_uiState.value.currentPath, name, false)
            reloadCurrentDirectory()
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            repository.deleteFile(path)
            reloadCurrentDirectory()
        }
    }

    fun renameFile(path: String, newName: String) {
        viewModelScope.launch {
            val file = File(path)
            val newFile = File(file.parentFile, newName)
            if (!newFile.exists()) {
               file.renameTo(newFile)
            }
            reloadCurrentDirectory()
        }
    }

    private var clipboardFiles = setOf<String>()
    private var isMoveOperation = false
    
    val hasClipboard: Boolean get() = clipboardFiles.isNotEmpty()

    fun copyFiles(paths: Set<String>) {
        clipboardFiles = paths
        isMoveOperation = false
    }

    fun cutFiles(paths: Set<String>) {
        clipboardFiles = paths
        isMoveOperation = true
    }

    fun pasteFiles() {
        if (clipboardFiles.isNotEmpty()) {
            viewModelScope.launch {
                repository.pasteFiles(clipboardFiles, _uiState.value.currentPath, isMoveOperation)
                if (isMoveOperation) {
                    clipboardFiles = emptySet()
                    isMoveOperation = false
                }
                reloadCurrentDirectory()
            }
        }
    }

    fun duplicateFile(path: String) {
        viewModelScope.launch {
            repository.duplicateFile(path)
            reloadCurrentDirectory()
        }
    }

    fun zipFiles(paths: Set<String>) {
        viewModelScope.launch {
            repository.zipFiles(paths, _uiState.value.currentPath)
            reloadCurrentDirectory()
        }
    }

    fun toggleFavorite(path: String) {
        repository.toggleFavorite(path)
        _favorites.value = repository.getFavorites()
    }

    fun loadFavorites() {
        val rootPath = "Favorites"
        val favFiles = repository.getFavorites().mapNotNull { 
            val file = File(it)
            if (file.exists()) FileItem(file) else null
        }.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name.lowercase() })
        
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            currentPath = rootPath,
            files = favFiles,
            breadcrumbs = listOf("Favorites"),
            errorMessage = null
        )
    }

    fun loadHome() {
        loadDirectory(repository.getRootPath())
    }

    fun loadDownloads() {
        loadDirectory(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS).absolutePath)
    }
}

data class FileBrowserUiState(
    val files: List<FileItem> = emptyList(),
    val breadcrumbs: List<String> = emptyList(),
    val currentPath: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
