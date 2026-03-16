package com.m22reader.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m22reader.data.repository.BookRepository
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
    private val books: BookRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val libraryFolder = settings.libraryFolder.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val backupFolder  = settings.backupFolder.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")
    val darkTheme     = settings.darkTheme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)
    val autoScan      = settings.autoScan.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    fun setLibraryFolder(path: String) = viewModelScope.launch { settings.setLibraryFolder(path) }
    fun setBackupFolder(path: String)  = viewModelScope.launch { settings.setBackupFolder(path) }
    fun setDarkTheme(v: Boolean)       = viewModelScope.launch { settings.setDarkTheme(v) }
    fun setAutoScan(v: Boolean)        = viewModelScope.launch { settings.setAutoScan(v) }

    fun scanLibraryFolder() = viewModelScope.launch {
        val folder = settings.libraryFolder.first()
        if (folder.isEmpty()) return@launch
        val dir = File(folder)
        if (!dir.exists() || !dir.isDirectory) return@launch
        val supported = setOf("pdf", "epub", "cbz", "cbr")
        dir.walkTopDown().forEach { file ->
            if (file.extension.lowercase() in supported) {
                // Import will be handled by FileImporter
            }
        }
    }

    fun backupLibrary() = viewModelScope.launch {
        val backupPath = settings.backupFolder.first()
        if (backupPath.isEmpty()) return@launch
        val backupDir = File(backupPath, "M22Reader_backup")
        backupDir.mkdirs()
        // Copy database
        val dbFile = context.getDatabasePath("m22_reader.db")
        if (dbFile.exists()) {
            dbFile.copyTo(File(backupDir, "m22_reader.db"), overwrite = true)
        }
    }
}
