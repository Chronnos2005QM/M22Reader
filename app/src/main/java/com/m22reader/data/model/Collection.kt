package com.m22reader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Uma Coleção representa uma pasta com múltiplos ficheiros (capítulos).
 * Ex: /Manhwas/SoloLeveling/ → Coleção "Solo Leveling" com N capítulos
 */
@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val folderPath: String,          // Caminho absoluto da pasta
    val coverPath: String? = null,   // cover.jpg/png dentro da pasta (cacheado)
    val chapterCount: Int = 0,
    val lastReadChapter: Int = 0,
    val isFavorite: Boolean = false,
    val addedAt: Date = Date(),
    val lastReadAt: Date? = null,
    val tags: String = "",
) {
    val progressPercent: Int
        get() = if (chapterCount > 0) ((lastReadChapter.toFloat() / chapterCount) * 100).toInt() else 0
}
