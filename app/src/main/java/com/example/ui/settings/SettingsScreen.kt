package com.example.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    var isDarkTheme by remember { mutableStateOf(true) }
    var useSystemTheme by remember { mutableStateOf(true) }
    var showHiddenFiles by remember { mutableStateOf(false) }
    var highContrastCode by remember { mutableStateOf(true) }

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
                    onCheckedChange = { useSystemTheme = it }
                )
            }
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.Palette,
                    title = "Force Dark Theme",
                    subtitle = "Always use the sophisticated dark theme",
                    checked = isDarkTheme,
                    enabled = !useSystemTheme,
                    onCheckedChange = { isDarkTheme = it }
                )
            }

            item { SettingsSectionHeader("File Manager", modifier = Modifier.padding(top = 16.dp)) }
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.Visibility,
                    title = "Show Hidden Files",
                    subtitle = "Display files starting with a dot",
                    checked = showHiddenFiles,
                    onCheckedChange = { showHiddenFiles = it }
                )
            }
            item {
                SettingsActionItem(
                    icon = Icons.Filled.Sort,
                    title = "Default Sorting",
                    subtitle = "Folders first, alphabetical"
                )
            }
            
            item { SettingsSectionHeader("Editor & Code", modifier = Modifier.padding(top = 16.dp)) }
            item {
                SettingsSwitchItem(
                    icon = Icons.Filled.Code,
                    title = "High Contrast Syntax",
                    subtitle = "Use vivid colors for code highlighting",
                    checked = highContrastCode,
                    onCheckedChange = { highContrastCode = it }
                )
            }
            item {
                SettingsActionItem(
                    icon = Icons.Filled.TextFormat,
                    title = "Font Size",
                    subtitle = "14 sp"
                )
            }

            item { SettingsSectionHeader("Git & Security", modifier = Modifier.padding(top = 16.dp)) }
            item {
                SettingsActionItem(
                    icon = Icons.Filled.AccountCircle,
                    title = "GitHub Account",
                    subtitle = "Not connected"
                )
            }
            item {
                SettingsActionItem(
                    icon = Icons.Filled.Lock,
                    title = "Manage Encryption Keys",
                    subtitle = "Used for encrypting files and archives"
                )
            }
        }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
