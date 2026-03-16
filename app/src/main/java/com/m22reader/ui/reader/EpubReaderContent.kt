package com.m22reader.ui.reader

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import com.m22reader.data.model.Book
import kotlinx.coroutines.*
import nl.siegmann.epublib.epub.EpubReader
import java.io.File

data class EpubChapter(val title: String, val htmlContent: String)

private fun loadEpubChapters(path: String): List<EpubChapter> {
    return try {
        val book = EpubReader().readEpub(File(path).inputStream())
        book.spine.spineReferences.mapIndexed { i, ref ->
            val title = ref.resource.title
                ?: book.tableOfContents.tocReferences.getOrNull(i)?.title
                ?: "Capítulo ${i + 1}"
            val html = ref.resource.data.toString(Charsets.UTF_8)
            EpubChapter(title, html)
        }
    } catch (e: Exception) { emptyList() }
}

@Composable
fun EpubReaderContent(
    book: Book,
    state: ReaderUiState,
    onPageChanged: (Int, Int) -> Unit,
    onTap: () -> Unit,
) {
    var chapters by remember { mutableStateOf<List<EpubChapter>>(emptyList()) }
    var fontSize by remember { mutableStateOf(18) }          // sp
    var fontFamily by remember { mutableStateOf("serif") }   // serif / sans-serif / monospace
    var showEpubControls by remember { mutableStateOf(false) }

    LaunchedEffect(book.filePath) {
        withContext(Dispatchers.IO) {
            chapters = loadEpubChapters(book.filePath)
            if (chapters.isNotEmpty()) onPageChanged(book.lastReadPage, chapters.size)
        }
    }

    if (chapters.isEmpty()) return

    val pagerState = rememberPagerState(initialPage = state.currentPage) { chapters.size }

    LaunchedEffect(pagerState.currentPage) { onPageChanged(pagerState.currentPage, chapters.size) }

    Box(Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            EpubChapterView(
                chapter    = chapters[index],
                isDark     = true, // always dark in reader
                fontSize   = fontSize,
                fontFamily = fontFamily,
                onTap      = onTap,
            )
        }

        // Epub-specific floating controls (font size, family)
        AnimatedVisibility(
            visible = state.showControls,
            enter   = fadeIn(),
            exit    = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 100.dp, end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { showEpubControls = !showEpubControls },
                containerColor = Color(0xFF1A1A28),
                contentColor = Color(0xFF9333EA),
            ) {
                Icon(Icons.Default.TextFields, "Ajustes de texto")
            }
        }

        if (showEpubControls) {
            EpubTextSettingsSheet(
                fontSize   = fontSize,
                fontFamily = fontFamily,
                onFontSize = { fontSize = it },
                onFont     = { fontFamily = it },
                onDismiss  = { showEpubControls = false },
            )
        }
    }
}

// ── Chapter WebView ───────────────────────────────────────────────────────────
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun EpubChapterView(
    chapter: EpubChapter,
    isDark: Boolean,
    fontSize: Int,
    fontFamily: String,
    onTap: () -> Unit,
) {
    val bg   = if (isDark) "#0d0d12" else "#f8f6f0"
    val fg   = if (isDark) "#e8e6f0" else "#1a1824"
    val link = if (isDark) "#9333ea" else "#7c3aed"

    val styledHtml = """
        <html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          body {
            background: $bg; color: $fg;
            font-family: $fontFamily;
            font-size: ${fontSize}px;
            line-height: 1.8;
            padding: 24px 20px 120px;
            margin: 0;
            word-break: break-word;
          }
          h1,h2,h3,h4 { color: #9333ea; font-weight: 800; }
          a { color: $link; }
          img { max-width: 100%; height: auto; border-radius: 8px; }
          p { margin-bottom: 1.2em; }
        </style>
        </head><body>
        ${chapter.htmlContent}
        </body></html>
    """.trimIndent()

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                setBackgroundColor(android.graphics.Color.parseColor(bg))
            }
        },
        update = { wv -> wv.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null) },
        modifier = Modifier.fillMaxSize().clickable { onTap() },
    )
}

// ── Font settings bottom sheet ────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpubTextSettingsSheet(
    fontSize: Int,
    fontFamily: String,
    onFontSize: (Int) -> Unit,
    onFont: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF13131A),
        contentColor = Color.White,
    ) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp).navigationBarsPadding()) {
            Text("Ajustes de leitura", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            // Font size
            Text("Tamanho do texto: ${fontSize}sp", fontSize = 13.sp, color = Color.White.copy(0.7f))
            Slider(
                value = fontSize.toFloat(),
                onValueChange = { onFontSize(it.toInt()) },
                valueRange = 12f..28f,
                steps = 7,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF9333EA), activeTrackColor = Color(0xFF9333EA)),
            )

            Spacer(Modifier.height(12.dp))

            // Font family
            Text("Tipo de letra", fontSize = 13.sp, color = Color.White.copy(0.7f))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("serif" to "Serif", "sans-serif" to "Sans", "monospace" to "Mono").forEach { (key, label) ->
                    val selected = fontFamily == key
                    FilterChip(
                        selected = selected,
                        onClick = { onFont(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF9333EA),
                            selectedLabelColor = Color.White,
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
