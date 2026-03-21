package com.m22reader.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import javax.inject.Inject

class CollectionRepository @Inject constructor() {

    // Função para escanear a pasta da biblioteca usando DocumentFile
    fun scanLibraryFolder(context: Context, uri: Uri) {
        val root = DocumentFile.fromTreeUri(context, uri) ?: return
        scanFolderRecursive(root)
    }

    // Função recursiva para escanear pastas e arquivos
    private fun scanFolderRecursive(folder: DocumentFile) {
        folder.listFiles().forEach { file ->
            if (file.isDirectory) {
                scanFolderRecursive(file)
            } else {
                // Processar arquivos (ex: CBZ, CBR, PDF, EPUB)
                println("File: ${file.name}, URI: ${file.uri}")
                // Adicione aqui a lógica para importar o arquivo
            }
        }
    }
}
