package com.m22reader.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.m22reader.data.model.Book
import com.m22reader.ui.library.BookCover
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(onBookClick: (Long) -> Unit, vm: HistoryViewModel = hiltViewModel()) {
    val history by vm.history.collectAsState(initial = emptyList())

    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(0.2f))
                Spacer(Modifier.height(12.dp))
                Text("Sem histórico ainda", color = MaterialTheme.colorScheme.onSurface.copy(0.4f), fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Leitura recente", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f), modifier = Modifier.padding(bottom = 4.dp))
        }
        items(history, key = { it.id }) { book -> HistoryCard(book) { onBookClick(book.id) } }
    }
}

@Composable
private fun HistoryCard(book: Book, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Coloured left stripe
            Box(Modifier.width(3.dp).height(72.dp).background(
                androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color(0xFF9333EA), Color(0xFFDB2777))),
                RoundedCornerShape(2.dp)
            ))
            BookCover(book, Modifier.size(width = 52.dp, height = 72.dp))
            Column(Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text("Cap. ${book.lastReadChapter}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { book.progressPercent / 100f },
                    Modifier.fillMaxWidth().height(2.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            book.lastReadAt?.let { date ->
                Text(
                    SimpleDateFormat("dd/MM", Locale.getDefault()).format(date),
                    fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.4f)
                )
            }
        }
    }
}

// ─── Modifier extension used inside HistoryCard ───────────────────────────────
