package com.m22reader.data

import androidx.room.*
import com.m22reader.data.dao.BookDao
import com.m22reader.data.model.Book
import com.m22reader.data.model.BookFormat
import java.util.Date

@TypeConverters(Converters::class)
@Database(entities = [Book::class], version = 1, exportSchema = false)
abstract class M22Database : RoomDatabase() {
    abstract fun bookDao(): BookDao
}

class Converters {
    @TypeConverter fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    @TypeConverter fun dateToTimestamp(date: Date?): Long? = date?.time
    @TypeConverter fun fromFormat(value: String): BookFormat = BookFormat.valueOf(value)
    @TypeConverter fun formatToString(format: BookFormat): String = format.name
}
