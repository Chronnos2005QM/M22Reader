package com.m22reader.data.dao

import androidx.room.*
import com.m22reader.data.model.ReadingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {

    @Insert
    suspend fun insert(session: ReadingSession): Long

    @Query("SELECT SUM(durationSeconds) FROM reading_sessions")
    fun getTotalReadingSeconds(): Flow<Long?>

    @Query("SELECT SUM(pagesRead) FROM reading_sessions")
    fun getTotalPagesRead(): Flow<Int?>

    @Query("SELECT COUNT(DISTINCT itemId) FROM reading_sessions WHERE itemType = 'collection'")
    fun getCollectionsStarted(): Flow<Int>

    @Query("SELECT AVG(durationSeconds * 1.0 / NULLIF(pagesRead, 0)) FROM reading_sessions WHERE pagesRead > 0")
    fun getAvgSecondsPerPage(): Flow<Double?>
}
