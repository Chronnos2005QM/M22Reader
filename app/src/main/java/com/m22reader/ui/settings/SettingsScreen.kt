package com.m22reader.ui.settings

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = hiltViewModel(),
    statsVm: StatsViewModel = hiltViewModel()
) {
    val libraryFolder by vm.libraryFolder.collectAsState()
    val backupFolder  by vm.backupFolder.collectAsState()
    val darkTheme     by vm.darkTheme.collectAsState()
    val autoScan      by vm.autoScan.collectAsState()
    val stats         by statsVm.stats.collectAsState(initial = ReadingStats())
    var showBackupDone by remember { mutableStateOf(false) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { vm.setLibraryFolder(it.path ?: it.toString()) }
    }
    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { vm.setBackupFolder(it.path ?: it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Voltar") } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Biblioteca ─────────────────────────────────────────
            item { SectionTitle("📚 Biblioteca") }
            item {
                SettingItem(Icons.Default.Folder, "Pasta da biblioteca",
                    if (libraryFolder.isEmpty()) "Nenhuma pasta selecionada" else libraryFolder
                ) { folderPicker.launch(null) }
            }
            item {
                SettingItem(Icons.Default.Refresh, "Sincronizar agora",
                    "Verificar novos ficheiros na pasta"
                ) { vm.scanLibraryFolder() }
            }
            item {
                SettingToggle(Icons.Default.AutoMode, "Sincronização automática",
                    "Detectar ficheiros ao abrir", autoScan, vm::setAutoScan)
            }

            // ── Backup ─────────────────────────────────────────────
            item { SectionTitle("💾 Backup") }
            item {
                SettingItem(Icons.Default.FolderOpen, "Pasta de backup",
                    if (backupFolder.isEmpty()) "Nenhuma pasta selecionada" else backupFolder
                ) { backupPicker.launch(null) }
            }
            item {
                SettingItem(Icons.Default.Backup, "Fazer backup agora",
                    "Guardar base de dados"
                ) { vm.backupLibrary(); showBackupDone = true }
            }

            // ── Aparência ───────────────────────────────────────────
            item { SectionTitle("🎨 Aparência") }
            item {
                SettingToggle(Icons.Default.DarkMode, "Tema escuro",
                    "Interface com fundo preto", darkTheme, vm::setDarkTheme)
            }

            // ── Estatísticas ────────────────────────────────────────
            item { SectionTitle("📊 Estatísticas de Leitura") }
            item {
                Card(shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // Linha 1: Coleções e Ficheiros
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatBox("Coleções", "${stats.totalCollections}", Modifier.weight(1f))
                            StatBox("Ficheiros", "${stats.totalBooks}", Modifier.weight(1f))
                            StatBox("Caps. lidos", "${stats.totalChaptersRead}", Modifier.weight(1f))
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.3f))

                        // Linha 2: Tempo
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatBox("Tempo total", formatDuration(stats.totalReadingSeconds), Modifier.weight(1f))
                            StatBox("Páginas lidas", "${stats.totalPagesRead}", Modifier.weight(1f))
                        }

                        // Estimativa de tempo restante
                        if (stats.avgSecondsPerPage > 0 && stats.totalCollections > 0) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.3f))
                            val remainingPages = stats.totalPagesRead * 2 // estimativa
                            val remainingSecs = (remainingPages * stats.avgSecondsPerPage).toLong()
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text("Estimativa restante: ${formatDuration(remainingSecs)}",
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                            }
                        }
                    }
                }
            }

            // ── Sobre ────────────────────────────────────────────
            item { SectionTitle("ℹ️ Sobre") }
            item {
                SettingItem(Icons.Default.Info, "M22 Reader",
                    "Versão 1.0.0 · Feito com ❤️") {}
            }
        }
    }

    if (showBackupDone) {
        AlertDialog(
            onDismissRequest = { showBackupDone = false },
            title = { Text("Backup concluído") },
            text = { Text("Biblioteca guardada com sucesso!") },
            confirmButton = { TextButton(onClick = { showBackupDone = false }) { Text("OK") } }
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary.copy(0.08f))) {
        Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f), fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return "0m"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return when {
        h > 0  -> "${h}h ${m}m"
        else   -> "${m}m"
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
}

@Composable
private fun SettingItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
private fun SettingToggle(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
