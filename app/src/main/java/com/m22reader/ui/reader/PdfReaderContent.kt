package com.m22reader.ui.reader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.m22reader.data.model.Book
import kotlinx.coroutines.*
import java.io.File

@Composable
fun PdfReaderContent(
    book: Book,
    state: ReaderUiState,
    onPageChanged: (Int, Int) -> Unit,
    onTap: () -> Unit,
) {
    val context = LocalContext.current
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pageCount by remember { mutableStateOf(0) }

    // Open renderer
    LaunchedEffect(book.filePath) {
        withContext(Dispatchers.IO) {
            try {
                val fd = ParcelFileDescriptor.open(File(book.filePath), ParcelFileDescriptor.MODE_READ_ONLY)
                renderer = PdfRenderer(fd)
                pageCount = renderer!!.pageCount
                onPageChanged(book.lastReadPage, pageCount)
            } catch (e: Exception) { /* handled by ReaderScreen error state */ }
        }
    }

    DisposableEffect(Unit) { onDispose { renderer?.close() } }

    if (renderer == null || pageCount == 0) return

    when (state.readingMode) {
        ReadingMode.HORIZONTAL_PAGED -> PdfHorizontalPager(renderer!!, pageCount, state, onPageChanged, onTap)
        else                         -> PdfVerticalScroll(renderer!!, pageCount, state, onPageChanged, onTap)
    }
}

// ── Vertical scroll (Manhwa / Webtoon) ───────────────────────────────────────
@Composable
private fun PdfVerticalScroll(
    renderer: PdfRenderer,
    pageCount: Int,
    state: ReaderUiState,
    onPageChanged: (Int, Int) -> Unit,
    onTap: () -> Unit,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = state.currentPage)
    val gap = if (state.readingMode == ReadingMode.WEBTOON) 0.dp else 4.dp

    // Track current page
    LaunchedEffect(listState.firstVisibleItemIndex) {
        onPageChanged(listState.firstVisibleItemIndex, pageCount)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { onTap() } },
        verticalArrangement = Arrangement.spacedBy(gap),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        items(pageCount) { index ->
            PdfPageImage(renderer, index, Modifier.fillMaxWidth())
        }
    }
}

// ── Horizontal pager (Manga) ──────────────────────────────────────────────────
@Composable
private fun PdfHorizontalPager(
    renderer: PdfRenderer,
    pageCount: Int,
    state: ReaderUiState,
    onPageChanged: (Int, Int) -> Unit,
    onTap: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = state.currentPage) { pageCount }

    LaunchedEffect(pagerState.currentPage) { onPageChanged(pagerState.currentPage, pageCount) }

    HorizontalPager(
        state = pagerState,
        reverseLayout = true, // Right-to-left for manga
        modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { onTap() } },
    ) { index ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PdfPageImage(renderer, index, Modifier.fillMaxSize())
        }
    }
}

// ── Single PDF page rendered to Bitmap ───────────────────────────────────────
@Composable
private fun PdfPageImage(renderer: PdfRenderer, pageIndex: Int, modifier: Modifier = Modifier) {
    var bitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pageIndex) {
        withContext(Dispatchers.Default) {
            try {
                val page = renderer.openPage(pageIndex)
                val scale = 2f // 2x for sharpness on high-density screens
                val bmp = Bitmap.createBitmap(
                    (page.width * scale).toInt(),
                    (page.height * scale).toInt(),
                    Bitmap.Config.ARGB_8888
                )
                bmp.eraseColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                bitmap = bmp
            } catch (_: Exception) {}
        }
    }

    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "Página ${pageIndex + 1}",
            modifier = modifier,
            contentScale = ContentScale.FillWidth,
        )
    } ?: Box(modifier.aspectRatio(0.7f).background(Color(0xFF111118)))
}
