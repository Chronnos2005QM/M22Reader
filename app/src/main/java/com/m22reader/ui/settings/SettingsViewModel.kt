package com.m22reader.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m22reader.data.repository.CollectionRepository
import com.m22reader.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val collectionRepo: CollectionRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val libraryFolder = settings.libraryFolder.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val backupFolder  = settings.backupFolder.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val darkTheme     = settings.darkTheme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val autoScan      = settings.autoScan.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    fun setLibraryFolder(path: String) = viewModelScope.launch {
        // Resolver o path do URI do SAF para caminho real
        val realPath = resolveUriPath(path)
        settings.setLibraryFolder(realPath)
        collectionRepo.scanLibraryFolder(realPath)
    }
    fun setBackupFolder(path: String) = viewModelScope.launch {
        settings.setBackupFolder(resolveUriPath(path))
    }
    fun setDarkTheme(v: Boolean) = viewModelScope.launch { settings.setDarkTheme(v) }
    fun setAutoScan(v: Boolean)  = viewModelScope.launch { settings.setAutoScan(v) }

    fun scanLibraryFolder() = viewModelScope.launch {
        val folder = settings.libraryFolder.first()
        if (folder.isNotEmpty()) collectionRepo.scanLibraryFolder(folder)
    }

    fun backupLibrary() = viewModelScope.launch {
        val backupPath = settings.backupFolder.first()
        if (backupPath.isEmpty()) return@launch
        val backupDir = File(backupPath, "M22Reader_backup")
        backupDir.mkdirs()
        val dbFile = context.getDatabasePath("m22_reader.db")
        if (dbFile.exists()) dbFile.copyTo(File(backupDir, "m22_reader.db"), overwrite = true)
    }

    // Converte path do SAF URI para caminho acessível
    // Ex: /tree/primary:Komikku/local → /sdcard/Komikku/local
    private fun resolveUriPath(uriPath: String): String {
        return when {
            uriPath.startsWith("/tree/primary:") -> {
                val rel = uriPath.removePrefix("/tree/primary:")
                "/sdcard/$rel"
            }
            uriPath.startsWith("/tree/") -> {
                // SD card externo
                val parts = uriPath.removePrefix("/tree/").split(":")
                if (parts.size >= 2) "/storage/${parts[0]}/${parts[1]}" else uriPath
            }
            else -> uriPath
        }
    }
}
