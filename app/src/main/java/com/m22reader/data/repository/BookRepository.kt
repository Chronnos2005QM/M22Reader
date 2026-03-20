package com.m22reader.data.repository

import com.m22reader.data.dao.BookDao
import com.m22reader.data.model.Book
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(private val dao: BookDao) {

    val allBooks: Flow<List<Book>> = dao.getAllBooks()
    val favorites: Flow<List<Book>> = dao.getFavorites()
    val readingHistory: Flow<List<Book>> = dao.getReadingHistory()
    val recentlyAdded: Flow<List<Book>> = dao.getRecentlyAdded()

    fun search(query: String): Flow<List<Book>> = dao.searchBooks(query)

    suspend fun addBook(book: Book): Long = dao.insertBook(book)
    suspend fun updateBook(book: Book) = dao.updateBook(book)
    suspend fun deleteBook(book: Book) = dao.deleteBook(book)
    suspend fun toggleFavorite(id: Long, current: Boolean) = dao.setFavorite(id, !current)
    suspend fun updateProgress(id: Long, chapter: Int, page: Int) = dao.updateProgress(id, chapter, page)
    suspend fun getById(id: Long): Book? = dao.getBookById(id)
}
