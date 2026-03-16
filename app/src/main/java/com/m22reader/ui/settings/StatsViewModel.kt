package com.m22reader.ui.settings

import androidx.lifecycle.ViewModel
import com.m22reader.data.dao.ReadingSessionDao
import com.m22reader.data.repository.BookRepository
import com.m22reader.data.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ReadingStats(
    val totalCollections: Int = 0,
    val totalBooks: Int = 0,
    val totalChaptersRead: Int = 0,
    val totalReadingSeconds: Long = 0,
    val totalPagesRead: Int = 0,
    val avgSecondsPerPage: Double = 0.0,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val collectionRepo: CollectionRepository,
    private val bookRepo: BookRepository,
    private val sessionDao: ReadingSessionDao,
) : ViewModel() {

    val stats: Flow<ReadingStats> = combine(
        collectionRepo.allCollections,
        bookRepo.allBooks,
        sessionDao.getTotalReadingSeconds(),
        sessionDao.getTotalPagesRead(),
        sessionDao.getAvgSecondsPerPage(),
    ) { collections, books, totalSecs, totalPages, avgSecs ->
        ReadingStats(
            totalCollections    = collections.size,
            totalBooks          = books.size,
            totalChaptersRead   = collections.sumOf { it.lastReadChapterNumber },
            totalReadingSeconds = totalSecs ?: 0L,
            totalPagesRead      = totalPages ?: 0,
            avgSecondsPerPage   = avgSecs ?: 0.0,
        )
    }
}
