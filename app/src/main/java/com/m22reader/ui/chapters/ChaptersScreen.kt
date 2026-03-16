package com.m22reader.ui.chapters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.m22reader.data.model.Chapter
import com.m22reader.data.model.Collection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    collectionId: Long,
    onChapterClick: (Long) -> Unit,
    onBack: () -> Unit,
    vm: ChaptersViewModel = hiltViewModel()
) {
    val collection by vm.collection.collectAsState()
    val chapters   by vm.chapters.collectAsState()

    LaunchedEffect(collectionId) { vm.load(collectionId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(collection?.name ?: "Coleção", fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Voltar") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                collection?.let { col ->
                    CollectionHeader(col)
                    Spacer(Modifier.height(8.dp))
                    Text("${chapters.size} capítulos · ${chapters.count { it.isRead }} lidos",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                        modifier = Modifier.padding(bottom = 8.dp))
                }
            }
            items(chapters, key = { it.id }) { chapter ->
                ChapterCard(chapter = chapter, onClick = { onChapterClick(chapter.id) })
            }
        }
    }
}

@Composable
private fun CollectionHeader(col: Collection) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 80.dp, height = 110.dp).clip(RoundedCornerShape(10.dp))) {
            if (col.coverPath != null) {
                AsyncImage(model = col.coverPath, contentDescription = col.name,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                val hue = (col.name.hashCode().and(0xFFFFFF) % 360).toFloat()
                Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(
                    Color.hsl(hue, 0.6f, 0.25f), Color.hsl((hue + 40) % 360, 0.7f, 0.4f)))),
                    contentAlignment = Alignment.Center) {
                    Text(col.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(col.name, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 2)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { col.progressPercent / 100f },
                Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary)
            Text("${col.progressPercent}% · Cap. ${col.lastReadChapterNumber} / ${col.chapterCount}",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun ChapterCard(chapter: Chapter, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (chapter.isRead)
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(if (chapter.isRead) 0.dp else 2.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(if (chapter.isRead) 0.1f else 0.2f)),
                contentAlignment = Alignment.Center) {
                Text(if (chapter.chapterNumber > 0) "${chapter.chapterNumber}" else "?",
                    fontWeight = FontWeight.Black, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary.copy(if (chapter.isRead) 0.4f else 1f))
            }
            Column(Modifier.weight(1f)) {
                Text(chapter.fileName.substringBeforeLast('.'),
                    fontWeight = if (chapter.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(if (chapter.isRead) 0.5f else 1f),
                    maxLines = 2)
                Text(chapter.format.name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.35f))
            }
            Icon(if (chapter.isRead) Icons.Default.CheckCircle else Icons.Default.PlayCircleOutline, null,
                tint = MaterialTheme.colorScheme.primary.copy(if (chapter.isRead) 0.4f else 1f),
                modifier = Modifier.size(20.dp))
        }
    }
}
