package com.m22reader.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [ForeignKey(
        entity = Collection::class,
        parentColumns = ["id"],
        childColumns = ["collectionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("collectionId")]
)
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val collectionId: Long,
    val fileName: String,
    val filePath: String,
    val chapterNumber: Int = 0,   // Extraído do nome do ficheiro
    val format: BookFormat,
    val pageCount: Int = 0,
    val lastReadPage: Int = 0,
    val isRead: Boolean = false,
    val addedAt: java.util.Date = java.util.Date(),
)
