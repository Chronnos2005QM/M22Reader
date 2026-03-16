package com.m22reader.ui.library

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.m22reader.data.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBookClick: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val books       by viewModel.books.collectAsState()
    val query       by viewModel.searchQuery.collectAsState()
    val viewMode    by viewModel.viewMode.collectAsState()
    val sortOrder   by viewModel.sortOrder.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Search + controls row
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Pesquisar título ou autor…", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // Sort button
            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { sortMenuExpanded = true }) {
                    Icon(Icons.Default.Sort, "Ordenar", tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(sortMenuExpanded, { sortMenuExpanded = false }) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = { Text(order.label) },
                            onClick = { viewModel.setSortOrder(order); sortMenuExpanded = false },
                            trailingIcon = { if (sortOrder == order) Icon(Icons.Default.Check, null) }
                        )
                    }
                }
            }

            // View toggle
            IconButton(onClick = viewModel::toggleViewMode) {
                Icon(if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView, "Mudar vista")
            }
        }

        if (books.isEmpty()) {
            EmptyLibrary(Modifier.fillMaxSize())
        } else {
            AnimatedContent(viewMode, label = "viewMode") { mode ->
                when (mode) {
                    ViewMode.GRID -> GridView(books, onBookClick, viewModel::toggleFavorite)
                    ViewMode.LIST -> ListView(books, onBookClick, viewModel::toggleFavorite)
                }
            }
        }
    }
}

@Composable
private fun GridView(books: List<Book>, onBook: (Long) -> Unit, onFav: (Book) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books, key = { it.id }) { book ->
            BookGridCard(book, { onBook(book.id) }, { onFav(book) })
        }
    }
}

@Composable
private fun ListView(books: List<Book>, onBook: (Long) -> Unit, onFav: (Book) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(books, key = { it.id }) { book ->
            BookListCard(book, { onBook(book.id) }, { onFav(book) })
        }
    }
}

@Composable
fun BookGridCard(book: Book, onClick: () -> Unit, onFavorite: () -> Unit) {
    Column(Modifier.clickable { onClick() }) {
        Box(Modifier.aspectRatio(0.72f).clip(RoundedCornerShape(12.dp))) {
            BookCover(book, Modifier.fillMaxSize())
            // Favorite button overlay
            IconButton(
                onClick = onFavorite,
                Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp)
                    .background(Color.Black.copy(0.5f), RoundedCornerShape(50))
            ) {
                Icon(
                    if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null, tint = if (book.isFavorite) Color(0xFFDB2777) else Color.White, modifier = Modifier.size(14.dp)
                )
            }
            if (book.progressPercent == 100) {
                Surface(
                    Modifier.align(Alignment.BottomStart).padding(6.dp),
                    color = Color(0xFF22C55E).copy(0.9f), shape = RoundedCornerShape(4.dp)
                ) { Text("LIDO", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White) }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        LinearProgressIndicator(
            progress = { book.progressPercent / 100f },
            Modifier.fillMaxWidth().height(2.dp).padding(top = 3.dp).clip(RoundedCornerShape(1.dp)),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun BookListCard(book: Book, onClick: () -> Unit, onFavorite: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BookCover(book, Modifier.size(width = 60.dp, height = 85.dp).clip(RoundedCornerShape(8.dp)))
            Column(Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Spacer(Modifier.height(6.dp))
                FormatBadge(book.format.name)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { book.progressPercent / 100f },
                    Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Cap. ${book.lastReadChapter} / ${book.totalChapters}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.45f), modifier = Modifier.padding(top = 3.dp))
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null, tint = if (book.isFavorite) Color(0xFFDB2777) else MaterialTheme.colorScheme.onSurface.copy(0.4f)
                )
            }
        }
    }
}

@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier) {
    if (book.coverPath != null) {
        AsyncImage(model = book.coverPath, contentDescription = book.title, modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        // Fallback gradient cover
        Box(modifier.background(Brush.linearGradient(listOf(Color(0xFF1A1A2E), Color(0xFF9333EA)))), contentAlignment = Alignment.Center) {
            Text(book.format.name, color = Color.White.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun FormatBadge(format: String) {
    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary.copy(0.15f)) {
        Text(format, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyLibrary(modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.LibraryBooks, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.2f))
        Spacer(Modifier.height(16.dp))
        Text("Biblioteca vazia", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
        Text("Adiciona ficheiros PDF, EPUB, CBZ ou CBR", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
    }
}

private val SortOrder.label get() = when (this) {
    SortOrder.DATE_ADDED -> "Data de adição"
    SortOrder.TITLE      -> "Título (A-Z)"
    SortOrder.LAST_READ  -> "Lido recentemente"
    SortOrder.PROGRESS   -> "Progresso"
}
