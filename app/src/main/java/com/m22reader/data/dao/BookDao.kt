package com.m22reader.data.dao

import androidx.room.*
import com.m22reader.data.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY addedAt DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE recentlyAdded = 1 ORDER BY addedAt DESC")
    fun getRecentlyAdded(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE lastReadAt IS NOT NULL ORDER BY lastReadAt DESC")
    fun getReadingHistory(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :q || '%'")
    fun searchBooks(q: String): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Book?

    @Query("SELECT * FROM books WHERE filePath = :path LIMIT 1")
    suspend fun getByPath(path: String): Book?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(book: Book): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBook(book: Book): Long

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("UPDATE books SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE books SET lastReadChapter = :chapNum, lastReadPage = :page, lastReadAt = :date WHERE id = :id")
    suspend fun updateProgress(id: Long, chapNum: Int, page: Int, date: java.util.Date = java.util.Date())
}
