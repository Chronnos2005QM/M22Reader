package com.m22reader.ui.settings

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    context: android.content.Context
) {
    val viewModel: SettingsViewModel = hiltViewModel()

    // Exemplo: Após selecionar a pasta (simulado)
    val selectedUri = remember { Uri.parse("content://com.android.externalstorage.documents/tree/primary:Manhwas") }
    viewModel.saveLibraryUri(context, selectedUri)
    viewModel.scanLibraryFolder(context, selectedUri)
}
