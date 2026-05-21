package com.example.ui.filebrowser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.models.FileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGitSheet by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    
    // Selection state
    var selectedFiles by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedFiles.isNotEmpty()
    
    // Bottom Sheet State for File Details/Actions
    var contextMenuFile by remember { mutableStateOf<FileItem?>(null) }
    
    val toggleSelection = { path: String ->
        val newSelection = selectedFiles.toMutableSet()
        if (newSelection.contains(path)) newSelection.remove(path) else newSelection.add(path)
        selectedFiles = newSelection
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedFiles.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { selectedFiles = emptySet() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO Bulk Zip */ }) { Icon(Icons.Filled.FolderZip, contentDescription = "Zip") }
                        IconButton(onClick = { /* TODO Bulk Delete */ }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                        IconButton(onClick = { /* TODO Bulk Ext Actions */ }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { 
                        BreadcrumbPath(
                            breadcrumbs = uiState.breadcrumbs,
                            onUpClick = { viewModel.navigateUp() }
                        ) 
                    },
                    actions = {
                        IconButton(onClick = { showGitSheet = true }) {
                            Icon(Icons.Filled.Source, contentDescription = "Git Actions")
                        }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Folder") },
                                onClick = { menuExpanded = false },
                                leadingIcon = { Icon(Icons.Filled.CreateNewFolder, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("New File") },
                                onClick = { menuExpanded = false },
                                leadingIcon = { Icon(Icons.Filled.NoteAdd, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { menuExpanded = false; onNavigateToSettings() },
                                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("About CodeFlow") },
                                onClick = { menuExpanded = false; onNavigateToAbout() },
                                leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { /* TODO: Contextual Add */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!uiState.errorMessage.isNullOrEmpty()) {
                Text(
                    text = uiState.errorMessage ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.files.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                    Icon(Icons.Filled.SnippetFolder, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Folder is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.files, key = { it.path }) { fileItem ->
                        val isSelected = selectedFiles.contains(fileItem.path)
                        FileRow(
                            fileItem = fileItem,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = {
                                if (isSelectionMode) {
                                    toggleSelection(fileItem.path)
                                } else {
                                    if (fileItem.isDirectory) {
                                        viewModel.loadDirectory(fileItem.path)
                                    } else {
                                        onNavigateToEditor(fileItem.path)
                                    }
                                }
                            },
                            onLongClick = {
                                if (!isSelectionMode) {
                                    contextMenuFile = fileItem
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showGitSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGitSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            GitActionsSheet(onDismiss = { showGitSheet = false })
        }
    }
    
    if (contextMenuFile != null) {
        ModalBottomSheet(
            onDismissRequest = { contextMenuFile = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            FileActionsSheet(file = contextMenuFile!!, onDismiss = { contextMenuFile = null }, onSelect = {
                selectedFiles = setOf(contextMenuFile!!.path)
                contextMenuFile = null
            })
        }
    }
}

@Composable
fun FileActionsSheet(file: FileItem, onDismiss: () -> Unit, onSelect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(file.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (file.isDirectory) "Folder" else "${file.size / 1024} KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        
        ListItem(
            headlineContent = { Text("Select") },
            leadingContent = { Icon(Icons.Filled.CheckCircleOutline, contentDescription = null) },
            modifier = Modifier.clickable { onSelect() }
        )
        ListItem(
            headlineContent = { Text("Rename") },
            leadingContent = { Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = null) },
            modifier = Modifier.clickable { onDismiss() }
        )
        ListItem(
            headlineContent = { Text("Copy") },
            leadingContent = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
            modifier = Modifier.clickable { onDismiss() }
        )
        ListItem(
            headlineContent = { Text("Move") },
            leadingContent = { Icon(Icons.Filled.DriveFileMove, contentDescription = null) },
            modifier = Modifier.clickable { onDismiss() }
        )
         ListItem(
            headlineContent = { Text("Zip / Compress") },
            leadingContent = { Icon(Icons.Filled.FolderZip, contentDescription = null) },
            modifier = Modifier.clickable { onDismiss() }
        )
        ListItem(
            headlineContent = { Text("Delete", color = MaterialTheme.colorScheme.error) },
            leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            modifier = Modifier.clickable { onDismiss() }
        )
    }
}

@Composable
fun BreadcrumbPath(breadcrumbs: List<String>, onUpClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (breadcrumbs.size > 1) {
            Text(
                text = "...",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { onUpClick() },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        val currentFolder = breadcrumbs.lastOrNull() ?: "Storage"
        Text(text = currentFolder, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileRow(
    fileItem: FileItem, 
    isSelected: Boolean, 
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    
    // Map icons playfully based on extension if not a directory
    val iconTriple = determineFileIcon(fileItem.name, fileItem.isDirectory)
    val iconVector = iconTriple.first
    val iconColor = iconTriple.second
    val isCustomColor = iconTriple.third
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            
            val activeIconTint = if (isCustomColor && !isSelected) iconColor else if (fileItem.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(activeIconTint.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = activeIconTint.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (!fileItem.isDirectory) {
                    Text(
                        text = "${fileItem.size / 1024} KB",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!isSelectionMode) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Utility to return (Icon, Color, HasCustomColor)
fun determineFileIcon(name: String, isDir: Boolean): Triple<androidx.compose.ui.graphics.vector.ImageVector, Color, Boolean> {
    if (isDir) return Triple(Icons.Filled.Folder, Color.Transparent, false)
    val ext = name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "kt", "java", "kts" -> Triple(Icons.Filled.Code, Color(0xFF8B5CF6), true)
        "xml", "html", "json" -> Triple(Icons.Filled.DataObject, Color(0xFFF59E0B), true)
        "md", "txt" -> Triple(Icons.Filled.Description, Color.Gray, false)
        "png", "jpg", "jpeg", "webp" -> Triple(Icons.Filled.Image, Color(0xFF10B981), true)
        "zip", "rar", "tar", "gz" -> Triple(Icons.Filled.FolderZip, Color(0xFFEF4444), true)
        else -> Triple(Icons.Filled.InsertDriveFile, Color.Gray, false)
    }
}

@Composable
fun GitActionsSheet(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Git Workspace", 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp, start = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GitActionButton(Icons.Filled.VerticalAlignBottom, "Pull", onClick = { onDismiss() })
            GitActionButton(Icons.Filled.Commit, "Commit", onClick = { onDismiss() })
            GitActionButton(Icons.Filled.VerticalAlignTop, "Push", onClick = { onDismiss() })
            GitActionButton(Icons.Filled.CallSplit, "Branch", onClick = { onDismiss() })
        }
    }
}

@Composable
fun GitActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium, 
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

