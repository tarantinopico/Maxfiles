package com.example.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigateBack: () -> Unit) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val useSystemTheme by viewModel.useSystemTheme.collectAsState()
    val showHiddenFiles by viewModel.showHiddenFiles.collectAsState()
    val highContrastCode by viewModel.highContrastCode.collectAsState()
    val gitToken by viewModel.gitToken.collectAsState()
    val gitUsername by viewModel.gitUsername.collectAsState()

    var showGitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { SettingsSectionHeader("Appearance") }
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.DarkMode,
                    title = "Use System Theme",
                    subtitle = "Follow system dark/light settings",
                    checked = useSystemTheme,
                    onCheckedChange = { viewModel.updateUseSystemTheme(it) }
                )
            }
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.Palette,
                    title = "Force Dark Theme",
                    subtitle = "Always use the sophisticated dark theme",
                    checked = isDarkTheme,
                    enabled = !useSystemTheme,
                    onCheckedChange = { viewModel.updateIsDarkTheme(it) }
                )
            }

            item { SettingsSectionHeader("File Manager", modifier = Modifier.padding(top = 16.dp)) }
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.Visibility,
                    title = "Show Hidden Files",
                    subtitle = "Display files starting with a dot",
                    checked = showHiddenFiles,
                    onCheckedChange = { viewModel.updateShowHiddenFiles(it) }
                )
            }
            
            item { SettingsSectionHeader("Editor & Code", modifier = Modifier.padding(top = 16.dp)) }
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.Code,
                    title = "High Contrast Syntax",
                    subtitle = "Use vivid colors for code highlighting",
                    checked = highContrastCode,
                    onCheckedChange = { viewModel.updateHighContrastCode(it) }
                )
            }
            item {
                val fontSize by viewModel.fontSize.collectAsState()
                var showFontSizeDialog by remember { mutableStateOf(false) }
                
                SettingsActionItem(
                    icon = Icons.Filled.TextFormat,
                    title = "Font Size",
                    subtitle = "$fontSize sp",
                    onClick = { showFontSizeDialog = true }
                )

                if (showFontSizeDialog) {
                    var currentSize by remember { mutableStateOf(fontSize.toFloat()) }
                    AlertDialog(
                        onDismissRequest = { showFontSizeDialog = false },
                        title = { Text("Font Size") },
                        text = {
                            Column {
                                Text("Select text size for the editor")
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("10", modifier = Modifier.padding(end = 8.dp))
                                    Slider(
                                        value = currentSize,
                                        onValueChange = { currentSize = it },
                                        valueRange = 10f..30f,
                                        steps = 20,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("30", modifier = Modifier.padding(start = 8.dp))
                                }
                                Text(
                                    text = "Preview Text",
                                    fontSize = currentSize.toInt().dp.value.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.updateFontSize(currentSize.toInt())
                                showFontSizeDialog = false
                            }) { Text("Save") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showFontSizeDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }

            item { SettingsSectionHeader("Git & Security", modifier = Modifier.padding(top = 16.dp)) }
            item {
                SettingsActionItem(
                    icon = Icons.Filled.AccountCircle,
                    title = "GitHub Account",
                    subtitle = if (gitUsername.isNotBlank()) "Connected as $gitUsername" else "Not connected",
                    onClick = { showGitDialog = true }
                )
            }
        }
    }

    if (showGitDialog) {
        var inputUsername by remember { mutableStateOf(gitUsername) }
        var inputToken by remember { mutableStateOf(gitToken) }

        AlertDialog(
            onDismissRequest = { showGitDialog = false },
            title = { Text("GitHub Authentication") },
            text = {
                Column {
                    Text("Enter your GitHub username and personal access token (PAT).")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputUsername,
                        onValueChange = { inputUsername = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputToken,
                        onValueChange = { inputToken = it },
                        label = { Text("Personal Access Token") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateGitUsername(inputUsername)
                    viewModel.updateGitToken(inputToken)
                    showGitDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = androidx.compose.ui.graphics.Color.Transparent,
        onClick = { if (enabled) onCheckedChange(!checked) },
        enabled = enabled
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=if(enabled) 1f else 0.5f))
                }
                Switch(
                    checked = checked, 
                    onCheckedChange = onCheckedChange, 
                    enabled = enabled,
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                )
            }
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), thickness = 1.dp)
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Surface(
        color = androidx.compose.ui.graphics.Color.Transparent,
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), thickness = 1.dp)
        }
    }
}
