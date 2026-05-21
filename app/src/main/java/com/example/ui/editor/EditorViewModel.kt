package com.example.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorViewModel(private val repository: FileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    fun loadFile(path: String?) {
        if (path == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "No file selected")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, currentPath = path, errorMessage = null)
            val result = repository.readFileContext(path)
            result.onSuccess { content ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    content = content,
                    fileName = path.substringAfterLast("/")
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message)
            }
        }
    }

    fun updateContent(newContent: String) {
        _uiState.value = _uiState.value.copy(content = newContent, hasUnsavedChanges = true)
    }

    fun saveFile() {
        val currentPath = _uiState.value.currentPath
        val content = _uiState.value.content
        if (currentPath == null) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val result = repository.saveFileContent(currentPath, content)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isSaving = false, hasUnsavedChanges = false)
            } else {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Failed to save")
            }
        }
    }
}

data class EditorUiState(
    val content: String = "",
    val fileName: String = "Untitled",
    val currentPath: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val errorMessage: String? = null
)
