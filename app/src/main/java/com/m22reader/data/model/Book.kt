package com.m22reader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val format: BookFormat,
    val totalChapters: Int = 0,
    val lastReadChapterNumber: Int = 0,
    val lastReadPage: Int = 0,
    val isFavorite: Boolean = false,
    val addedAt: Date = Date(),
    val lastReadAt: Date? = null,
) {
    val progressPercent: Int
        get() = if (totalChapters > 0) ((lastReadChapterNumber.toFloat() / totalChapters) * 100).toInt() else 0
}
