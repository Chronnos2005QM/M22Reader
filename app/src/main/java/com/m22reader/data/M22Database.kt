package com.m22reader.data

import androidx.room.*
import com.m22reader.data.dao.*
import com.m22reader.data.model.*
import java.util.Date

@TypeConverters(Converters::class)
@Database(
    entities = [Book::class, Collection::class, Chapter::class, ReadingSession::class],
    version = 4,
    exportSchema = false
)
abstract class M22Database : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun collectionDao(): CollectionDao
    abstract fun readingSessionDao(): ReadingSessionDao
}

class Converters {
    @TypeConverter fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    @TypeConverter fun dateToTimestamp(date: Date?): Long? = date?.time
    @TypeConverter fun fromFormat(value: String): BookFormat = BookFormat.valueOf(value)
    @TypeConverter fun formatToString(format: BookFormat): String = format.name
}
