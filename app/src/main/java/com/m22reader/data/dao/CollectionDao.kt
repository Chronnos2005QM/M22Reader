package com.m22reader.data.dao

import androidx.room.*
import com.m22reader.data.model.Chapter
import com.m22reader.data.model.Collection
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    // ── Collections ───────────────────────────────────────────────
    @Query("SELECT * FROM collections ORDER BY addedAt DESC")
    fun getAllCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE name LIKE '%' || :q || '%'")
    fun searchCollections(q: String): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE folderPath = :path LIMIT 1")
    suspend fun getByPath(path: String): Collection?

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Collection?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollection(c: Collection): Long

    @Update
    suspend fun updateCollection(c: Collection)

    @Delete
    suspend fun deleteCollection(c: Collection)

    @Query("UPDATE collections SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE collections SET lastReadChapterId = :chapId, lastReadChapterNumber = :chapNum, lastReadAt = :date WHERE id = :id")
    suspend fun updateProgress(id: Long, chapId: Long, chapNum: Int, date: java.util.Date = java.util.Date())

    @Query("UPDATE collections SET chapterCount = :count WHERE id = :id")
    suspend fun updateChapterCount(id: Long, count: Int)

    @Query("UPDATE collections SET coverPath = :path WHERE id = :id")
    suspend fun updateCover(id: Long, path: String)

    // ── Chapters ──────────────────────────────────────────────────
    @Query("SELECT * FROM chapters WHERE collectionId = :collectionId ORDER BY chapterNumber ASC")
    fun getChaptersForCollection(collectionId: Long): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE collectionId = :collectionId ORDER BY chapterNumber ASC")
    suspend fun getChaptersSync(collectionId: Long): List<Chapter>

    @Query("SELECT * FROM chapters WHERE filePath = :path LIMIT 1")
    suspend fun getChapterByPath(path: String): Chapter?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChapter(chapter: Chapter): Long

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Query("UPDATE chapters SET isRead = :read, lastReadPage = :page WHERE id = :id")
    suspend fun markChapterRead(id: Long, read: Boolean, page: Int = 0)

    @Query("SELECT COUNT(*) FROM chapters WHERE collectionId = :id")
    suspend fun countChapters(id: Long): Int
}
