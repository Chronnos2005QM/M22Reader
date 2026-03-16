package com.m22reader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reading_sessions")
data class ReadingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,           // collectionId ou bookId
    val itemType: String,       // "collection" ou "book"
    val durationSeconds: Long,  // duração em segundos
    val pagesRead: Int = 0,
    val startedAt: Date = Date(),
)
