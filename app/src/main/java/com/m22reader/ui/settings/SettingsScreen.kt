package com.m22reader.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel()) {
    val libraryFolder by vm.libraryFolder.collectAsState()
    val backupFolder  by vm.backupFolder.collectAsState()
    val darkTheme     by vm.darkTheme.collectAsState()
    val autoScan      by vm.autoScan.collectAsState()

    val libraryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> uri?.let { vm.setLibraryFolder(it.toString()) } }

    val backupPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri -> uri?.let { vm.setBackupFolder(it.toString()) } }

    var expandedSection by remember { mutableStateOf<String?>("library") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Configurações") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSection(
                title = "Biblioteca",
                icon = Icons.Default.LibraryBooks,
                expanded = expandedSection == "library",
                onToggle = { expandedSection = if (expandedSection == "library") null else "library" }
            ) {
                SettingsItem(
                    title = "Pasta da biblioteca",
                    subtitle = libraryFolder.ifEmpty { "Toca para definir" },
                    icon = Icons.Default.Folder,
                    onClick = { libraryPicker.launch(null) }
                )
                SettingsSwitchItem(
                    title = "Scan automático",
                    subtitle = "Procurar ficheiros ao abrir o app",
                    icon = Icons.Default.Refresh,
                    checked = autoScan,
                    onCheckedChange = { vm.setAutoScan(it) }
                )
                SettingsItem(
                    title = "Scan manual",
                    subtitle = "Procurar novos ficheiros agora",
                    icon = Icons.Default.Search,
                    onClick = { vm.scanLibraryFolder() }
                )
            }

            SettingsSection(
                title = "Leitura",
                icon = Icons.Default.MenuBook,
                expanded = expandedSection == "reading",
                onToggle = { expandedSection = if (expandedSection == "reading") null else "reading" }
            ) {
                SettingsSwitchItem(
                    title = "Tema escuro",
                    subtitle = "Interface com fundo escuro",
                    icon = Icons.Default.DarkMode,
                    checked = darkTheme,
                    onCheckedChange = { vm.setDarkTheme(it) }
                )
            }

            SettingsSection(
                title = "Backup",
                icon = Icons.Default.Backup,
                expanded = expandedSection == "backup",
                onToggle = { expandedSection = if (expandedSection == "backup") null else "backup" }
            ) {
                SettingsItem(
                    title = "Pasta de backup",
                    subtitle = backupFolder.ifEmpty { "Toca para definir" },
                    icon = Icons.Default.FolderOpen,
                    onClick = { backupPicker.launch(null) }
                )
                SettingsItem(
                    title = "Fazer backup agora",
                    subtitle = "Guardar base de dados na pasta",
                    icon = Icons.Default.Save,
                    onClick = { vm.backupLibrary() }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(icon, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Text(title, style = MaterialTheme.typography.titleMedium)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            if (expanded) {
                HorizontalDivider()
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
