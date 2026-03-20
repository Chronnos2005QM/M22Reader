package com.m22reader.ui.updates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.m22reader.data.model.Book
import com.m22reader.ui.library.BookCover
import com.m22reader.ui.library.FormatBadge
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UpdatesScreen(onBookClick: (Long) -> Unit, vm: UpdatesViewModel = hiltViewModel()) {
    val recent by vm.recentlyAdded.collectAsState(initial = emptyList())

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Adicionados Recentemente", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f), modifier = Modifier.padding(bottom = 4.dp))
        }
        items(recent, key = { it.id }) { book ->
            UpdateCard(book) { onBookClick(book.id) }
        }
    }
}

@Composable
private fun UpdateCard(book: Book, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BookCover(book, Modifier.size(width = 52.dp, height = 72.dp))
            Column(Modifier.weight(1f)) {
                Text(book.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Spacer(Modifier.height(6.dp))
                FormatBadge(book.format.name)
            }
            Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary.copy(0.12f)) {
                Text(
                    relativeDate(book.addedAt),
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun relativeDate(date: Date): String {
    val diff = System.currentTimeMillis() - date.time
    val days = (diff / (1000 * 60 * 60 * 24)).toInt()
    return when {
        days == 0 -> "Hoje"
        days == 1 -> "Ontem"
        days < 7  -> "${days}d atrás"
        else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
    }
}
