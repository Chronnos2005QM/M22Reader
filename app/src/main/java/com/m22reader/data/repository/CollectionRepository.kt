package com.m22reader.data.repository

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CollectionRepository @Inject constructor() {

    fun scanLibraryFolder(context: Context, uri: Uri): Flow<List<String>> = flow {
        val root = DocumentFile.fromTreeUri(context, uri) ?: return@flow
        val files = mutableListOf<String>()
        scanFolderRecursive(root, files)
        emit(files)
    }

    private fun scanFolderRecursive(folder: DocumentFile, files: MutableList<String>) {
        folder.listFiles().forEach { file ->
            if (file.isDirectory) {
                scanFolderRecursive(file, files)
            } else {
                files.add(file.name ?: "")
            }
        }
    }
}
