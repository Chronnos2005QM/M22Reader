package com.m22reader.ui.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    // Função para resolver o URI e retornar um DocumentFile
    fun resolveUriPath(context: Context, uri: Uri): DocumentFile? {
        return DocumentFile.fromTreeUri(context, uri)?.takeIf { it.exists() }
    }

    // Função para salvar o URI da biblioteca
    fun saveLibraryUri(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences("M22ReaderPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("library_uri", uri.toString()).apply()
    }

    // Função para recuperar o URI da biblioteca
    fun getLibraryUri(context: Context): Uri? {
        val prefs = context.getSharedPreferences("M22ReaderPrefs", Context.MODE_PRIVATE)
        val uriString = prefs.getString("library_uri", null)
        return uriString?.let { Uri.parse(it) }
    }
}
