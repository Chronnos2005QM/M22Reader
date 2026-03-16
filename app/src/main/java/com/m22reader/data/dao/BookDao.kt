package com.m22reader.data.dao

import androidx.room.*
import com.m22reader.data.model.Book
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface BookDao {

    // ── Library ──────────────────────────────────────────────
    @Query("SELECT * FROM books ORDER BY addedAt DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<Book>>

    // ── History ───────────────────────────────────────────────
    @Query("SELECT * FROM books WHERE lastReadAt IS NOT NULL ORDER BY lastReadAt DESC LIMIT 50")
    fun getReadingHistory(): Flow<List<Book>>

    // ── Updates (recently added) ──────────────────────────────
    @Query("SELECT * FROM books ORDER BY addedAt DESC LIMIT 20")
    fun getRecentlyAdded(): Flow<List<Book>>

    // ── Single book ───────────────────────────────────────────
    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Long): Book?

    // ── Mutations ─────────────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("UPDATE books SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE books SET lastReadChapter = :chapter, lastReadPage = :page, lastReadAt = :date WHERE id = :id")
    suspend fun updateProgress(id: Long, chapter: Int, page: Int, date: Date = Date())
}
