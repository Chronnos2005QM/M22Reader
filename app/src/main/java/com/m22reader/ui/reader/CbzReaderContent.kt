package com.m22reader.ui.reader

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.m22reader.data.model.Book
import kotlinx.coroutines.*
import java.io.File
import java.util.zip.ZipFile

private val IMAGE_EXTS = setOf("jpg", "jpeg", "png", "webp", "gif")

/** Loads all image entry names from CBZ sorted naturally (001.jpg, 002.jpg …) */
private fun loadCbzEntries(path: String): List<String> {
    return try {
        ZipFile(path).use { zip ->
            zip.entries().asSequence()
                .map { it.name }
                .filter { it.substringAfterLast('.').lowercase() in IMAGE_EXTS }
                .sortedWith(compareBy({ it.length }, { it })) // natural order
                .toList()
        }
    } catch (e: Exception) { emptyList() }
}

private fun loadCbzPage(path: String, entryName: String): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        ZipFile(path).use { zip ->
            val entry = zip.getEntry(entryName) ?: return null
            zip.getInputStream(entry).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }
    } catch (e: Exception) { null }
}

@Composable
fun CbzReaderContent(
    book: Book,
    state: ReaderUiState,
    onPageChanged: (Int, Int) -> Unit,
    onTap: () -> Unit,
) {
    var entries by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(book.filePath) {
        withContext(Dispatchers.IO) {
            entries = loadCbzEntries(book.filePath)
            if (entries.isNotEmpty()) onPageChanged(book.lastReadPage, entries.size)
        }
    }

    if (entries.isEmpty()) return

    when (state.readingMode) {
        ReadingMode.HORIZONTAL_PAGED ->
            ComicHorizontalPager(book.filePath, entries, state, onPageChanged, onTap, isCbr = false)
        else ->
            ComicVerticalScroll(book.filePath, entries, state, onPageChanged, onTap, isCbr = false)
    }
}

// ── Vertical scroll ───────────────────────────────────────────────────────────
@Composable
fun ComicVerticalScroll(
    filePath: String,
    entries: List<String>,
    state: ReaderUiState,
    onPageChanged: (Int, Int) -> Unit,
    onTap: () -> Unit,
    isCbr: Boolean,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = state.currentPage)
    val gap = if (state.readingMode == ReadingMode.WEBTOON) 0.dp else 4.dp

    LaunchedEffect(listState.firstVisibleItemIndex) {
        onPageChanged(listState.firstVisibleItemIndex, entries.size)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { onTap() } },
        verticalArrangement = Arrangement.spacedBy(gap),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        items(entries.size) { index ->
            ComicPageImage(filePath, entries[index], index, isCbr, Modifier.fillMaxWidth())
        }
    }
}

// ── Horizontal pager ──────────────────────────────────────────────────────────
@Composable
fun ComicHorizontalPager(
    filePath: String,
    entries: List<String>,
    state: ReaderUiState,
    onPageChanged: (Int, Int) -> Unit,
    onTap: () -> Unit,
    isCbr: Boolean,
) {
    val pagerState = rememberPagerState(initialPage = state.currentPage) { entries.size }

    LaunchedEffect(pagerState.currentPage) { onPageChanged(pagerState.currentPage, entries.size) }

    HorizontalPager(
        state = pagerState,
        reverseLayout = true,
        modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { onTap() } },
    ) { index ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ComicPageImage(filePath, entries[index], index, isCbr, Modifier.fillMaxSize())
        }
    }
}

// ── Single page image (lazy-loaded) ──────────────────────────────────────────
@Composable
fun ComicPageImage(
    filePath: String,
    entryName: String,
    index: Int,
    isCbr: Boolean,
    modifier: Modifier = Modifier,
) {
    var bmp by remember(entryName) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(entryName) {
        withContext(Dispatchers.IO) {
            bmp = if (isCbr) loadCbrPage(filePath, entryName)
                  else       loadCbzPage(filePath, entryName)
        }
    }

    bmp?.let {
        Image(
            bitmap = it,
            contentDescription = "Página ${index + 1}",
            modifier = modifier,
            contentScale = ContentScale.FillWidth,
        )
    } ?: Box(modifier.aspectRatio(0.7f).background(Color(0xFF111118)))
}

// Exposed for CbrReaderContent to reuse
internal fun loadCbrPage(path: String, entryName: String): ImageBitmap? {
    return try {
        val archive = com.github.junrar.Archive(File(path))
        val header = archive.fileHeaders.firstOrNull { it.fileName == entryName }
        if (header == null) { archive.close(); return null }
        val bytes = archive.getInputStream(header).readBytes()
        archive.close()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Exception) { null }
}
