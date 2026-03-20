package com.m22reader.ui.reader

import androidx.compose.runtime.*
import com.m22reader.data.model.Book
import kotlinx.coroutines.*
import java.io.File

private val IMAGE_EXTS_CBR = setOf("jpg", "jpeg", "png", "webp", "gif")

private fun loadCbrEntries(path: String): List<String> {
    return try {
        val archive = com.github.junrar.Archive(File(path))
        val names = archive.fileHeaders
            .map { it.fileName }
            .filter { it.substringAfterLast('.').lowercase() in IMAGE_EXTS_CBR }
            .sortedWith(compareBy({ it.length }, { it }))
        archive.close()
        names
    } catch (e: Exception) { emptyList() }
}

@Composable
fun CbrReaderContent(
    book: Book,
    state: ReaderUiState,
    onPageChanged: (Int, Int) -> Unit,
    onTap: () -> Unit,
) {
    var entries by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(book.filePath) {
        withContext(Dispatchers.IO) {
            entries = loadCbrEntries(book.filePath)
            if (entries.isNotEmpty()) onPageChanged(book.lastReadPage, entries.size)
        }
    }

    if (entries.isEmpty()) return

    // Reuse CBZ composables — only the page loading function differs (loadCbrPage)
    when (state.readingMode) {
        ReadingMode.HORIZONTAL_PAGED ->
            ComicHorizontalPager(book.filePath, entries, state, onPageChanged, onTap, isCbr = true)
        else ->
            ComicVerticalScroll(book.filePath, entries, state, onPageChanged, onTap, isCbr = true)
    }
}
