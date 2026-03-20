package com.m22reader.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("m22_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val LIBRARY_FOLDER = stringPreferencesKey("library_folder")
        val BACKUP_FOLDER  = stringPreferencesKey("backup_folder")
        val DARK_THEME     = booleanPreferencesKey("dark_theme")
        val READING_MODE   = stringPreferencesKey("reading_mode")
        val AUTO_SCAN      = booleanPreferencesKey("auto_scan")
    }

    val libraryFolder: Flow<String> = context.dataStore.data.map { it[LIBRARY_FOLDER] ?: "" }
    val backupFolder: Flow<String>  = context.dataStore.data.map { it[BACKUP_FOLDER] ?: "" }
    val darkTheme: Flow<Boolean>    = context.dataStore.data.map { it[DARK_THEME] ?: true }
    val readingMode: Flow<String>   = context.dataStore.data.map { it[READING_MODE] ?: "VERTICAL_SCROLL" }
    val autoScan: Flow<Boolean>     = context.dataStore.data.map { it[AUTO_SCAN] ?: true }

    suspend fun setLibraryFolder(path: String) {
        context.dataStore.edit { it[LIBRARY_FOLDER] = path }
    }

    suspend fun setBackupFolder(path: String) {
        context.dataStore.edit { it[BACKUP_FOLDER] = path }
    }

    suspend fun setDarkTheme(value: Boolean) {
        context.dataStore.edit { it[DARK_THEME] = value }
    }

    suspend fun setReadingMode(mode: String) {
        context.dataStore.edit { it[READING_MODE] = mode }
    }

    suspend fun setAutoScan(value: Boolean) {
        context.dataStore.edit { it[AUTO_SCAN] = value }
    }
}
