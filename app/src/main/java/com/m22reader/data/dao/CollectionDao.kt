package com.m22reader.data.dao

import androidx.room.*
import com.m22reader.data.model.Collection
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections ORDER BY addedAt DESC")
    fun getAllCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE title LIKE '%' || :query || '%'")
    fun searchCollections(query: String): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getById(id: Long): Collection?

    @Query("SELECT * FROM collections WHERE folderPath = :path")
    suspend fun getByPath(path: String): Collection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: Collection): Long

    @Update
    suspend fun update(collection: Collection)

    @Delete
    suspend fun delete(collection: Collection)

    @Query("UPDATE collections SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("UPDATE collections SET lastReadChapter = :chapter, lastReadAt = :date WHERE id = :id")
    suspend fun updateProgress(id: Long, chapter: Int, date: java.util.Date = java.util.Date())

    @Query("UPDATE collections SET coverPath = :path WHERE id = :id")
    suspend fun updateCover(id: Long, path: String)
}
