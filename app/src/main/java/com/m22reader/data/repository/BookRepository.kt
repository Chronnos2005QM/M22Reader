package com.m22reader.data.repository

import com.m22reader.data.dao.BookDao
import com.m22reader.data.model.Book
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(private val dao: BookDao) {
    val allBooks: Flow<List<Book>> = dao.getAllBooks()
    fun search(q: String): Flow<List<Book>> = dao.searchBooks(q)
    suspend fun getById(id: Long): Book? = dao.getById(id)
    suspend fun toggleFavorite(id: Long, current: Boolean) = dao.setFavorite(id, !current)
    suspend fun updateProgress(id: Long, chapNum: Int, page: Int) = dao.updateProgress(id, chapNum, page)
}
