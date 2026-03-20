package com.m22reader.ui.reader

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import java.io.File
import java.util.zip.ZipFile

data class EpubChapter(val title: String, val htmlContent: String)

private fun loadEpubChapters(path: String): List<EpubChapter> {
    return try {
        val zip = ZipFile(path)
        val entries = zip.entries().asSequence()
            .filter { it.name.endsWith(".html") || it.name.endsWith(".xhtml") || it.name.endsWith(".htm") }
            .sortedBy { it.name }
            .toList()
        entries.mapIndexed { i, entry ->
            val html = zip.getInputStream(entry).bufferedReader().readText()
            EpubChapter("Capítulo ${i + 1}", html)
        }.also { zip.close() }
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
    var fontSize by remember { mutableStateOf(18) }
    var fontFamily by remember { mutableStateOf("serif") }
    var showEpubControls by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(state.currentPage) }

    LaunchedEffect(book.filePath) {
        withContext(Dispatchers.IO) {
            chapters = loadEpubChapters(book.filePath)
            if (chapters.isNotEmpty()) onPageChanged(state.currentPage, chapters.size)
        }
    }

    if (chapters.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF9333EA))
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        EpubChapterView(
            chapter    = chapters[currentIndex],
            isDark     = true,
            fontSize   = fontSize,
            fontFamily = fontFamily,
            onTap      = onTap,
        )

        // Navegação entre capítulos
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentIndex > 0) {
                FloatingActionButton(
                    onClick = { currentIndex--; onPageChanged(currentIndex, chapters.size) },
                    containerColor = Color(0xFF1A1A28), contentColor = Color(0xFF9333EA),
                    modifier = Modifier.size(40.dp)
                ) { Icon(Icons.Default.ArrowBack, null, Modifier.size(18.dp)) }
            }
            if (currentIndex < chapters.size - 1) {
                FloatingActionButton(
                    onClick = { currentIndex++; onPageChanged(currentIndex, chapters.size) },
                    containerColor = Color(0xFF1A1A28), contentColor = Color(0xFF9333EA),
                    modifier = Modifier.size(40.dp)
                ) { Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp)) }
            }
        }

        AnimatedVisibility(
            visible = state.showControls,
            enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 150.dp, end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = { showEpubControls = !showEpubControls },
                containerColor = Color(0xFF1A1A28), contentColor = Color(0xFF9333EA),
            ) { Icon(Icons.Default.TextFields, "Ajustes de texto") }
        }

        if (showEpubControls) {
            EpubTextSettingsSheet(
                fontSize = fontSize, fontFamily = fontFamily,
                onFontSize = { fontSize = it }, onFont = { fontFamily = it },
                onDismiss = { showEpubControls = false },
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun EpubChapterView(
    chapter: EpubChapter,
    isDark: Boolean,
    fontSize: Int,
    fontFamily: String,
    onTap: () -> Unit,
) {
    val bg = if (isDark) "#0d0d12" else "#f8f6f0"
    val fg = if (isDark) "#e8e6f0" else "#1a1824"

    val styledHtml = """
        <html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
          body { background:$bg; color:$fg; font-family:$fontFamily; font-size:${fontSize}px;
                 line-height:1.8; padding:24px 20px 120px; margin:0; word-break:break-word; }
          h1,h2,h3 { color:#9333ea; font-weight:800; }
          img { max-width:100%; height:auto; border-radius:8px; }
          p { margin-bottom:1.2em; }
        </style></head><body>${chapter.htmlContent}</body></html>
    """.trimIndent()

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false
                settings.loadWithOverviewMode = true
                setBackgroundColor(android.graphics.Color.parseColor(bg))
            }
        },
        update = { wv -> wv.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null) },
        modifier = Modifier.fillMaxSize().clickable { onTap() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpubTextSettingsSheet(
    fontSize: Int, fontFamily: String,
    onFontSize: (Int) -> Unit, onFont: (String) -> Unit, onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFF13131A)) {
        Column(Modifier.padding(24.dp).navigationBarsPadding()) {
            Text("Ajustes de leitura", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Spacer(Modifier.height(16.dp))
            Text("Tamanho: ${fontSize}sp", fontSize = 13.sp, color = Color.White.copy(0.7f))
            Slider(
                value = fontSize.toFloat(), onValueChange = { onFontSize(it.toInt()) },
                valueRange = 12f..28f, steps = 7,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF9333EA), activeTrackColor = Color(0xFF9333EA)),
            )
            Spacer(Modifier.height(12.dp))
            Text("Tipo de letra", fontSize = 13.sp, color = Color.White.copy(0.7f))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("serif" to "Serif", "sans-serif" to "Sans", "monospace" to "Mono").forEach { (key, label) ->
                    FilterChip(
                        selected = fontFamily == key, onClick = { onFont(key) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF9333EA), selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
