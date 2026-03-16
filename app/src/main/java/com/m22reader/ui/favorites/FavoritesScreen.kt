package com.m22reader.ui.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.m22reader.ui.library.BookListCard

@Composable
fun FavoritesScreen(onBookClick: (Long) -> Unit, vm: FavoritesViewModel = hiltViewModel()) {
    val favorites by vm.favorites.collectAsState(initial = emptyList())

    if (favorites.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FavoriteBorder, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.2f))
                Spacer(Modifier.height(12.dp))
                Text("Nenhum favorito ainda", color = MaterialTheme.colorScheme.onSurface.copy(0.4f), fontWeight = FontWeight.Bold)
                Text("Toca no ♡ de qualquer livro para adicionar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("${favorites.size} favorito${if (favorites.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                modifier = Modifier.padding(bottom = 4.dp))
        }
        items(favorites, key = { it.id }) { book ->
            BookListCard(book, { onBookClick(book.id) }, { vm.toggleFavorite(book) })
        }
    }
}
