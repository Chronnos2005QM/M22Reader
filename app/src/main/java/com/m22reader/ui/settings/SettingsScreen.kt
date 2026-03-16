package com.m22reader.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel()
) {
    val libraryFolder by vm.libraryFolder.collectAsState()
    val backupFolder  by vm.backupFolder.collectAsState()
    val darkTheme     by vm.darkTheme.collectAsState()
    val autoScan      by vm.autoScan.collectAsState()
    var showBackupDone by remember { mutableStateOf(false) }

    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { vm.setLibraryFolder(it.path ?: it.toString()) }
    }

    val backupPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { vm.setBackupFolder(it.path ?: it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Biblioteca ────────────────────────────────────────────
            item { SectionTitle("📚 Biblioteca") }

            item {
                SettingItem(
                    icon = Icons.Default.Folder,
                    title = "Pasta da biblioteca",
                    subtitle = if (libraryFolder.isEmpty()) "Nenhuma pasta selecionada" else libraryFolder,
                    onClick = { folderPicker.launch(null) }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Refresh,
                    title = "Sincronizar biblioteca",
                    subtitle = "Verificar novos ficheiros na pasta",
                    onClick = { vm.scanLibraryFolder() }
                )
            }

            item {
                SettingToggle(
                    icon = Icons.Default.AutoMode,
                    title = "Sincronização automática",
                    subtitle = "Detectar ficheiros novos ao abrir",
                    checked = autoScan,
                    onChecked = vm::setAutoScan
                )
            }

            // ── Backup ────────────────────────────────────────────────
            item { SectionTitle("💾 Backup") }

            item {
                SettingItem(
                    icon = Icons.Default.FolderOpen,
                    title = "Pasta de backup",
                    subtitle = if (backupFolder.isEmpty()) "Nenhuma pasta selecionada" else backupFolder,
                    onClick = { backupPicker.launch(null) }
                )
            }

            item {
                SettingItem(
                    icon = Icons.Default.Backup,
                    title = "Fazer backup agora",
                    subtitle = "Guardar base de dados na pasta de backup",
                    onClick = {
                        vm.backupLibrary()
                        showBackupDone = true
                    }
                )
            }

            // ── Aparência ─────────────────────────────────────────────
            item { SectionTitle("🎨 Aparência") }

            item {
                SettingToggle(
                    icon = Icons.Default.DarkMode,
                    title = "Tema escuro",
                    subtitle = "Interface com fundo preto",
                    checked = darkTheme,
                    onChecked = vm::setDarkTheme
                )
            }

            // ── Sobre ─────────────────────────────────────────────────
            item { SectionTitle("ℹ️ Sobre") }

            item {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "M22 Reader",
                    subtitle = "Versão 1.0.0 • Feito com ❤️",
                    onClick = {}
                )
            }
        }
    }

    if (showBackupDone) {
        AlertDialog(
            onDismissRequest = { showBackupDone = false },
            title = { Text("Backup concluído") },
            text = { Text("A tua biblioteca foi guardada com sucesso!") },
            confirmButton = {
                TextButton(onClick = { showBackupDone = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, fontSize = 11.sp, fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), maxLines = 1)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
        }
    }
}

@Composable
private fun SettingToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
            Switch(checked = checked, onCheckedChange = onChecked,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
        }
    }
}
