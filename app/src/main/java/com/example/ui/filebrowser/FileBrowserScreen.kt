package com.example.ui.filebrowser

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.FileItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel,
    onNavigateToEditor: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isRightSidebarOpen by remember { mutableStateOf(false) }
    var contextMenuFile by remember { mutableStateOf<FileItem?>(null) }
    var selectedFiles by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedFiles.isNotEmpty()
    
    // Auto-close selection when folder changes
    LaunchedEffect(uiState.currentPath) {
        selectedFiles = emptySet()
    }
    
    // Close sidebar on back press if open
    BackHandler(enabled = isRightSidebarOpen) {
        isRightSidebarOpen = false
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // MAIN CONTENT (File Manager)
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Custom Top Bar (Compact, VS Code style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    IconButton(onClick = { selectedFiles = emptySet() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("${selectedFiles.size} selected", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { /* Bulk */ }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurface)
                    }
                } else {
                    BreadcrumbPath(
                        breadcrumbs = uiState.breadcrumbs,
                        onUpClick = { viewModel.navigateUp() }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Quick Action Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { /* Search */ }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { /* New File/Folder */ }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.Add, contentDescription = "New", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        // Open Right Sidebar Toggle
                        IconButton(onClick = { isRightSidebarOpen = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.MenuOpen, contentDescription = "Sidebar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            // File List Area
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                } else if (!uiState.errorMessage.isNullOrEmpty()) {
                    Text(
                        text = uiState.errorMessage ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.files.isEmpty()) {
                    EmptyFolderView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(uiState.files, key = { it.path }) { fileItem ->
                            val isSelected = selectedFiles.contains(fileItem.path)
                            CompactFileRow(
                                fileItem = fileItem,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onClick = {
                                    if (isSelectionMode) {
                                        val newSel = selectedFiles.toMutableSet()
                                        if (newSel.contains(fileItem.path)) newSel.remove(fileItem.path) else newSel.add(fileItem.path)
                                        selectedFiles = newSel
                                    } else {
                                        if (fileItem.isDirectory) viewModel.loadDirectory(fileItem.path)
                                        else onNavigateToEditor(fileItem.path)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) contextMenuFile = fileItem
                                }
                            )
                        }
                    }
                }
            }
        }

        // RIGHT SIDEBAR OVERLAY
        AnimatedVisibility(
            visible = isRightSidebarOpen,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isRightSidebarOpen = false }
            )
        }

        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val sidebarWidth = minOf(300.dp, screenWidth * 0.8f)

        AnimatedVisibility(
            visible = isRightSidebarOpen,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            RightSidebar(
                width = sidebarWidth,
                onSettingsClick = { isRightSidebarOpen = false; onNavigateToSettings() },
                onAboutClick = { isRightSidebarOpen = false; onNavigateToAbout() },
                onClose = { isRightSidebarOpen = false }
            )
        }
        
    }

    // Context Menu Bottom Sheet
    contextMenuFile?.let { file ->
        ModalBottomSheet(
            onDismissRequest = { contextMenuFile = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            PremiumFileActionsSheet(file = file, onDismiss = { contextMenuFile = null }, onSelect = {
                selectedFiles = setOf(file.path)
                contextMenuFile = null
            })
        }
    }
}

@Composable
fun BreadcrumbPath(breadcrumbs: List<String>, onUpClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(36.dp)) {
        if (breadcrumbs.size > 1) {
            IconButton(onClick = onUpClick, modifier = Modifier.size(28.dp).padding(end = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Up", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
        val currentFolder = breadcrumbs.lastOrNull() ?: "Storage"
        Text(
            text = currentFolder.uppercase(), 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = 1.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactFileRow(
    fileItem: FileItem, 
    isSelected: Boolean, 
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f) 
                  else Color.Transparent

    val iconTriple = determinePremiumFileIcon(fileItem.name, fileItem.isDirectory)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(bgColor)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                modifier = Modifier.padding(end = 12.dp).size(20.dp)
            )
        }
        
        Icon(
            imageVector = iconTriple.first,
            contentDescription = null,
            tint = iconTriple.second,
            modifier = Modifier.size(22.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = fileItem.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        if (!fileItem.isDirectory) {
            Text(
                text = "${fileItem.size / 1024} KB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun determinePremiumFileIcon(name: String, isDir: Boolean): Triple<ImageVector, Color, Boolean> {
    if (isDir) return Triple(Icons.Filled.Folder, Color(0xFF007ACC), true) // VS Code Blue
    val ext = name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "kt", "java", "kts" -> Triple(Icons.Filled.Code, Color(0xFF8B5CF6), true)
        "xml", "html" -> Triple(Icons.Filled.Code, Color(0xFFE34F26), true)
        "json" -> Triple(Icons.Filled.DataObject, Color(0xFFFBC02D), true)
        "js", "ts" -> Triple(Icons.Filled.Javascript, Color(0xFFF7DF1E), true)
        "md", "txt" -> Triple(Icons.Filled.Description, Color(0xFFCCCCCC), false)
        "png", "jpg", "jpeg", "webp" -> Triple(Icons.Filled.Image, Color(0xFF10B981), true)
        "zip", "rar", "tar", "gz" -> Triple(Icons.Filled.FolderZip, Color(0xFFEF4444), true)
        else -> Triple(Icons.Filled.InsertDriveFile, Color(0xFF858585), false)
    }
}

@Composable
fun EmptyFolderView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center, 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No files found", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun RightSidebar(width: androidx.compose.ui.unit.Dp, onSettingsClick: () -> Unit, onAboutClick: () -> Unit, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("WORKSPACE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        
        LazyColumn(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            item { SidebarSectionTitle("QUICK ACCESS") }
            item { SidebarItem(Icons.Filled.Home, "Home", Color(0xFF007ACC)) }
            item { SidebarItem(Icons.Filled.Favorite, "Favorites", Color(0xFFE91E63)) }
            item { SidebarItem(Icons.Filled.Download, "Downloads", Color(0xFF4CAF50)) }
            item { SidebarItem(Icons.Filled.Schedule, "Recent", Color(0xFFFF9800)) }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { SidebarSectionTitle("SOURCE CONTROL") }
            item { SidebarItem(Icons.Filled.Source, "Repository Status", MaterialTheme.colorScheme.onSurfaceVariant) }
            item { SidebarItem(Icons.Filled.CloudUpload, "Commit & Push", MaterialTheme.colorScheme.onSurfaceVariant) }
            item { SidebarItem(Icons.Filled.CallSplit, "Branches", MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        
        // Footer Tools
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            SidebarItem(Icons.Filled.Settings, "Settings", MaterialTheme.colorScheme.onSurfaceVariant, onClick = onSettingsClick)
            SidebarItem(Icons.Filled.Info, "About CodeFlow", MaterialTheme.colorScheme.onSurfaceVariant, onClick = onAboutClick)
        }
    }
}

@Composable
fun SidebarSectionTitle(title: String) {
    Text(
        title, 
        style = MaterialTheme.typography.labelSmall, 
        fontWeight = FontWeight.Bold, 
        color = MaterialTheme.colorScheme.onSurfaceVariant, 
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        letterSpacing = 1.sp
    )
}

@Composable
fun SidebarItem(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun PremiumFileActionsSheet(file: FileItem, onDismiss: () -> Unit, onSelect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(file.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (file.isDirectory) "Folder" else "${file.size / 1024} KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        
        ListItem(
            headlineContent = { Text("Select for Bulk Action") },
            leadingContent = { Icon(Icons.Filled.CheckCircleOutline, contentDescription = null) },
            modifier = Modifier.clickable { onSelect() }
        )
        ListItem(
            headlineContent = { Text("Rename") },
            leadingContent = { Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = null) },
            modifier = Modifier.clickable { onDismiss() }
        )
        if (!file.isDirectory) {
            ListItem(
                headlineContent = { Text("Open with Editor") },
                leadingContent = { Icon(Icons.Filled.Code, contentDescription = null) },
                modifier = Modifier.clickable { onDismiss() }
            )
        }
        ListItem(
            headlineContent = { Text("Move or Copy") },
            leadingContent = { Icon(Icons.Filled.DriveFileMove, contentDescription = null) },
            modifier = Modifier.clickable { onDismiss() }
        )
         ListItem(
            headlineContent = { Text("Archive (ZIP)") },
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
