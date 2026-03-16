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
import com.m22reader.data.model.Collection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBookClick: (Long) -> Unit,
    onCollectionClick: (Long) -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val books       by viewModel.books.collectAsState()
    val collections by viewModel.collections.collectAsState()
    val query       by viewModel.searchQuery.collectAsState()
    val viewMode    by viewModel.viewMode.collectAsState()
    val sortOrder   by viewModel.sortOrder.collectAsState()
    val activeTab   by viewModel.activeTab.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TabRow(
            selectedTabIndex = activeTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Tab(
                selected = activeTab == LibraryTab.COLLECTIONS,
                onClick = { viewModel.setActiveTab(LibraryTab.COLLECTIONS) },
                text = { Text("Coleções (${collections.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.FolderOpen, null, Modifier.size(16.dp)) }
            )
            Tab(
                selected = activeTab == LibraryTab.FILES,
                onClick = { viewModel.setActiveTab(LibraryTab.FILES) },
                text = { Text("Ficheiros (${books.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.InsertDriveFile, null, Modifier.size(16.dp)) }
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Pesquisar…", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            var sortMenuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton({ sortMenuExpanded = true }) { Icon(Icons.Default.Sort, "Ordenar") }
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
            IconButton(onClick = viewModel::toggleViewMode) {
                Icon(if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView, null)
            }
        }

        AnimatedContent(activeTab, label = "tab") { tab ->
            when (tab) {
                LibraryTab.COLLECTIONS -> {
                    if (collections.isEmpty()) {
                        EmptyLibrary(Icons.Default.FolderOpen, "Sem coleções", "Configura a pasta da biblioteca nas ⋮ Configurações")
                    } else {
                        if (viewMode == ViewMode.GRID) CollectionGrid(collections, onCollectionClick, viewModel::toggleFavoriteCollection)
                        else CollectionList(collections, onCollectionClick, viewModel::toggleFavoriteCollection)
                    }
                }
                LibraryTab.FILES -> {
                    if (books.isEmpty()) {
                        EmptyLibrary(Icons.Default.InsertDriveFile, "Sem ficheiros", "Importa ficheiros com o botão +")
                    } else {
                        if (viewMode == ViewMode.GRID) BookGrid(books, onBookClick, viewModel::toggleFavoriteBook)
                        else BookList(books, onBookClick, viewModel::toggleFavoriteBook)
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionGrid(collections: List<Collection>, onClick: (Long) -> Unit, onFav: (Collection) -> Unit) {
    LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(collections, key = { it.id }) { col ->
            CollectionGridCard(col, { onClick(col.id) }, { onFav(col) })
        }
    }
}

@Composable
fun CollectionGridCard(col: Collection, onClick: () -> Unit, onFav: () -> Unit) {
    Column(Modifier.clickable { onClick() }) {
        Box(Modifier.aspectRatio(0.72f).clip(RoundedCornerShape(12.dp))) {
            CollectionCover(col, Modifier.fillMaxSize())
            Surface(Modifier.align(Alignment.BottomStart).padding(6.dp),
                color = Color.Black.copy(0.7f), shape = RoundedCornerShape(4.dp)) {
                Text("${col.chapterCount} caps", Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            IconButton(onClick = onFav, Modifier.align(Alignment.TopEnd).padding(2.dp).size(28.dp)
                .background(Color.Black.copy(0.5f), RoundedCornerShape(50))) {
                Icon(if (col.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null,
                    tint = if (col.isFavorite) Color(0xFFDB2777) else Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(col.name, maxLines = 2, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        LinearProgressIndicator(progress = { col.progressPercent / 100f },
            Modifier.fillMaxWidth().height(2.dp).padding(top = 3.dp).clip(RoundedCornerShape(1.dp)),
            color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun CollectionList(collections: List<Collection>, onClick: (Long) -> Unit, onFav: (Collection) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(collections, key = { it.id }) { col ->
            CollectionListCard(col, { onClick(col.id) }, { onFav(col) })
        }
    }
}

@Composable
fun CollectionListCard(col: Collection, onClick: () -> Unit, onFav: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CollectionCover(col, Modifier.size(width = 60.dp, height = 85.dp).clip(RoundedCornerShape(8.dp)))
            Column(Modifier.weight(1f)) {
                Text(col.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${col.chapterCount} capítulos", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(progress = { col.progressPercent / 100f },
                    Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = MaterialTheme.colorScheme.primary)
                Text("Cap. ${col.lastReadChapterNumber} / ${col.chapterCount}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.45f), modifier = Modifier.padding(top = 3.dp))
            }
            IconButton(onClick = onFav) {
                Icon(if (col.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null,
                    tint = if (col.isFavorite) Color(0xFFDB2777) else MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
        }
    }
}

@Composable
fun CollectionCover(col: Collection, modifier: Modifier = Modifier) {
    if (col.coverPath != null) {
        AsyncImage(model = col.coverPath, contentDescription = col.name, modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        val hue = (col.name.hashCode().and(0xFFFFFF) % 360).toFloat()
        Box(modifier.background(Brush.linearGradient(listOf(
            Color.hsl(hue, 0.6f, 0.25f), Color.hsl((hue + 40) % 360, 0.7f, 0.4f)))),
            contentAlignment = Alignment.Center) {
            Text(col.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

@Composable
private fun BookGrid(books: List<Book>, onBook: (Long) -> Unit, onFav: (Book) -> Unit) {
    LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(books, key = { it.id }) { book -> BookGridCard(book, { onBook(book.id) }, { onFav(book) }) }
    }
}

@Composable
private fun BookList(books: List<Book>, onBook: (Long) -> Unit, onFav: (Book) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(books, key = { it.id }) { book -> BookListCard(book, { onBook(book.id) }, { onFav(book) }) }
    }
}

@Composable
fun BookGridCard(book: Book, onClick: () -> Unit, onFavorite: () -> Unit) {
    Column(Modifier.clickable { onClick() }) {
        Box(Modifier.aspectRatio(0.72f).clip(RoundedCornerShape(12.dp))) {
            BookCover(book, Modifier.fillMaxSize())
            IconButton(onClick = onFavorite, Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp)
                .background(Color.Black.copy(0.5f), RoundedCornerShape(50))) {
                Icon(if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null,
                    tint = if (book.isFavorite) Color(0xFFDB2777) else Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        LinearProgressIndicator(progress = { book.progressPercent / 100f },
            Modifier.fillMaxWidth().height(2.dp).padding(top = 3.dp).clip(RoundedCornerShape(1.dp)),
            color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun BookListCard(book: Book, onClick: () -> Unit, onFavorite: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BookCover(book, Modifier.size(width = 60.dp, height = 85.dp).clip(RoundedCornerShape(8.dp)))
            Column(Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Spacer(Modifier.height(6.dp))
                FormatBadge(book.format.name)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(progress = { book.progressPercent / 100f },
                    Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = MaterialTheme.colorScheme.primary)
                Text("Cap. ${book.lastReadChapter} / ${book.totalChapters}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.45f), modifier = Modifier.padding(top = 3.dp))
            }
            IconButton(onClick = onFavorite) {
                Icon(if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null,
                    tint = if (book.isFavorite) Color(0xFFDB2777) else MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
        }
    }
}

@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier) {
    if (book.coverPath != null) {
        AsyncImage(model = book.coverPath, contentDescription = book.title, modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        val hue = (book.title.hashCode().and(0xFFFFFF) % 360).toFloat()
        Box(modifier.background(Brush.linearGradient(listOf(
            Color.hsl(hue, 0.6f, 0.2f), Color.hsl((hue + 40) % 360, 0.7f, 0.35f)))),
            contentAlignment = Alignment.Center) {
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
private fun EmptyLibrary(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.2f))
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
        }
    }
}

private val SortOrder.label get() = when (this) {
    SortOrder.DATE_ADDED -> "Data de adição"
    SortOrder.TITLE      -> "Título (A-Z)"
    SortOrder.LAST_READ  -> "Lido recentemente"
    SortOrder.PROGRESS   -> "Progresso"
}
