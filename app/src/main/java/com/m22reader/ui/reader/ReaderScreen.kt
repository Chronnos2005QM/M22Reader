package com.m22reader.ui.reader

import android.app.Activity
import android.os.Build
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.m22reader.data.model.Book
import com.m22reader.data.model.BookFormat

@Composable
fun ReaderScreen(
    bookId: Long,
    onBack: () -> Unit,
    vm: ReaderViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(bookId) { vm.loadBook(bookId) }

    // Fullscreen imersivo
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window?.decorView?.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window?.insetsController?.show(
                    WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
                )
            } else {
                @Suppress("DEPRECATION")
                window?.decorView?.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0F))) {
        when {
            state.isLoading -> ReaderLoadingScreen()
            state.error != null -> ReaderErrorScreen(state.error!!, onBack, vm::dismissError)
            state.book != null -> {
                val book = state.book!!
                when (book.format) {
                    BookFormat.PDF  -> PdfReaderContent(book, state, vm::onPageChanged, vm::toggleControls)
                    BookFormat.EPUB -> EpubReaderContent(book, state, vm::onPageChanged, vm::toggleControls)
                    BookFormat.CBZ  -> CbzReaderContent(book, state, vm::onPageChanged, vm::toggleControls)
                    BookFormat.CBR  -> CbrReaderContent(book, state, vm::onPageChanged, vm::toggleControls)
                }
                ReaderControls(
                    book         = book,
                    state        = state,
                    onBack       = onBack,
                    onModeChange = vm::setReadingMode,
                    onBrightness = vm::setBrightness,
                    onTap        = vm::toggleControls,
                )
            }
        }
    }
}

@Composable
fun ReaderControls(
    book: Book,
    state: ReaderUiState,
    onBack: () -> Unit,
    onModeChange: (ReadingMode) -> Unit,
    onBrightness: (Float) -> Unit,
    onTap: () -> Unit,
) {
    val accent = Color(0xFF9333EA)

    AnimatedVisibility(
        visible = state.showControls,
        enter   = fadeIn() + slideInVertically { -it },
        exit    = fadeOut() + slideOutVertically { -it },
    ) {
        Box(
            Modifier.fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(Color.Black.copy(0.85f), Color.Transparent)
                ))
                .statusBarsPadding()
                .padding(start = 8.dp, end = 8.dp, bottom = 48.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) {
                    Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                }
                Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    Text(book.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                    Text(
                        "Pág. ${state.currentPage + 1} / ${state.totalPages}",
                        color = Color.White.copy(0.6f), fontSize = 11.sp
                    )
                }
                var menuOpen by remember { mutableStateOf(false) }
                Box {
                    IconButton({ menuOpen = true }) {
                        Icon(Icons.Default.Tune, "Modo de leitura", tint = Color.White)
                    }
                    DropdownMenu(menuOpen, { menuOpen = false },
                        Modifier.background(Color(0xFF1A1A24))) {
                        ReadingMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label, color = Color.White, fontSize = 13.sp) },
                                onClick = { onModeChange(mode); menuOpen = false },
                                leadingIcon = {
                                    if (state.readingMode == mode)
                                        Icon(Icons.Default.Check, null, tint = accent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = state.showControls,
            enter   = fadeIn() + slideInVertically { it },
            exit    = fadeOut() + slideOutVertically { it },
        ) {
            Box(
                Modifier.fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(0.8f))
                    ))
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.BrightnessLow, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(16.dp))
                        Slider(
                            value = state.brightness,
                            onValueChange = onBrightness,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
                            valueRange = 0.1f..1f,
                        )
                        Icon(Icons.Default.BrightnessHigh, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    if (state.totalPages > 0) {
                        LinearProgressIndicator(
                            progress = { (state.currentPage + 1f) / state.totalPages },
                            Modifier.fillMaxWidth().height(3.dp),
                            color = accent,
                            trackColor = Color.White.copy(0.15f),
                        )
                        Text(
                            "${state.currentPage + 1} / ${state.totalPages}",
                            color = Color.White.copy(0.5f), fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReaderLoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF9333EA))
            Spacer(Modifier.height(16.dp))
            Text("A carregar…", color = Color.White.copy(0.6f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun ReaderErrorScreen(error: String, onBack: () -> Unit, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            Modifier.padding(32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color(0xFF1A1A24))
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFDB2777), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("Erro ao abrir ficheiro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text(error, color = Color.White.copy(0.6f), fontSize = 13.sp)
                Spacer(Modifier.height(20.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(Color(0xFF9333EA))) {
                    Text("Voltar à biblioteca")
                }
            }
        }
    }
}

private val ReadingMode.label get() = when (this) {
    ReadingMode.VERTICAL_SCROLL  -> "Scroll vertical (Manhwa)"
    ReadingMode.HORIZONTAL_PAGED -> "Páginas horizontais (Manga)"
    ReadingMode.WEBTOON          -> "Webtoon contínuo"
}
