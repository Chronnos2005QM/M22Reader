package com.m22reader.ui.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
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

    fun setLibraryFolder(uriString: String) = viewModelScope.launch {
        val realPath = resolveUriPath(uriString)
        settings.setLibraryFolder(realPath)
        collectionRepo.scanLibraryFolder(realPath)
    }

    fun setBackupFolder(uriString: String) = viewModelScope.launch {
        val realPath = resolveUriPath(uriString)
        settings.setBackupFolder(realPath)
    }

    fun setDarkTheme(v: Boolean) = viewModelScope.launch { settings.setDarkTheme(v) }
    fun setAutoScan(v: Boolean)  = viewModelScope.launch { settings.setAutoScan(v) }

    fun scanLibraryFolder() = viewModelScope.launch {
        val folder = settings.libraryFolder.first()
        if (folder.isNotEmpty()) collectionRepo.scanLibraryFolder(folder)
    }

    fun backupLibrary() = viewModelScope.launch {
        try {
            val backupPath = settings.backupFolder.first()
            if (backupPath.isEmpty()) return@launch
            val backupDir = File(backupPath, "M22Reader_backup")
            backupDir.mkdirs()
            val dbFile = context.getDatabasePath("m22_reader.db")
            if (dbFile.exists()) {
                dbFile.copyTo(File(backupDir, "m22_reader.db"), overwrite = true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resolveUriPath(uriString: String): String {
        return try {
            val uri = Uri.parse(uriString)
            val docFile = DocumentFile.fromTreeUri(context, uri)
            val lastSegment = uri.lastPathSegment ?: return uriString
            when {
                lastSegment.startsWith("primary:") ->
                    "/sdcard/${lastSegment.removePrefix("primary:")}"
                lastSegment.contains(":") -> {
                    val parts = lastSegment.split(":")
                    if (parts.size >= 2) "/storage/${parts[0]}/${parts[1]}" else uriString
                }
                else -> uriString
            }
        } catch (_: Exception) { uriString }
    }
}
