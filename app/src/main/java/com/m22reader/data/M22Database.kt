package com.m22reader.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.m22reader.data.dao.BookDao
import com.m22reader.data.dao.CollectionDao
import com.m22reader.data.dao.ReadingSessionDao
import com.m22reader.data.model.Book
import com.m22reader.data.model.BookFormat
import com.m22reader.data.model.Chapter
import com.m22reader.data.model.Collection
import com.m22reader.data.model.ReadingSession
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
