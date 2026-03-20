package com.m22reader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val folderPath: String,
    val coverPath: String? = null,
    val chapterCount: Int = 0,
    val lastReadChapterId: Long = 0,
    val lastReadChapterNumber: Int = 0,
    val isFavorite: Boolean = false,
    val addedAt: Date = Date(),
    val lastReadAt: Date? = null,
) {
    val progressPercent: Int
        get() = if (chapterCount > 0) ((lastReadChapterNumber.toFloat() / chapterCount) * 100).toInt() else 0
}
