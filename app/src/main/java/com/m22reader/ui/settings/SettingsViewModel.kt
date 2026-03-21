package com.m22reader.ui.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    fun resolveUriPath(context: Context, uri: Uri): DocumentFile? {
        return DocumentFile.fromTreeUri(context, uri)?.takeIf { it.exists() }
    }

    fun saveLibraryUri(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences("M22ReaderPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("library_uri", uri.toString()).apply()
    }

    fun getLibraryUri(context: Context): Uri? {
        return context.getSharedPreferences("M22ReaderPrefs", Context.MODE_PRIVATE)
            .getString("library_uri", null)?.let { Uri.parse(it) }
    }
}
