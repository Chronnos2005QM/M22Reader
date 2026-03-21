package com.m22reader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String? = null,
    val filePath: String,
    val coverPath: String? = null,
    val format: BookFormat,
    val totalChapters: Int = 0,
    val lastReadChapter: Int = 0,
    val lastReadPage: Int = 0,
    val progressPercent: Int = 0,
    val isFavorite: Boolean = false,
    val recentlyAdded: Boolean = false,
    val addedAt: Date = Date(),
    val lastReadAt: Date? = null,
)
