package com.m22reader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class BookFormat { PDF, EPUB, CBZ, CBR }

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String = "Desconhecido",
    val filePath: String,
    val format: BookFormat,
    val coverPath: String? = null,
    val totalChapters: Int = 0,
    val lastReadChapter: Int = 0,
    val lastReadPage: Int = 0,
    val isFavorite: Boolean = false,
    val addedAt: Date = Date(),
    val lastReadAt: Date? = null,
    val tags: String = "", // comma-separated
)

// Computed helpers
val Book.progressPercent: Int
    get() = if (totalChapters > 0) ((lastReadChapter.toFloat() / totalChapters) * 100).toInt() else 0

val Book.isCompleted: Boolean
    get() = totalChapters > 0 && lastReadChapter >= totalChapters

val Book.tagList: List<String>
    get() = tags.split(",").filter { it.isNotBlank() }
