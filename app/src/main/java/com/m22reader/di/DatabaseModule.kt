package com.m22reader.di

import android.content.Context
import androidx.room.Room
import com.m22reader.data.M22Database
import com.m22reader.data.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): M22Database =
        Room.databaseBuilder(ctx, M22Database::class.java, "m22_reader.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton fun provideBookDao(db: M22Database): BookDao = db.bookDao()
    @Provides @Singleton fun provideCollectionDao(db: M22Database): CollectionDao = db.collectionDao()
    @Provides @Singleton fun provideReadingSessionDao(db: M22Database): ReadingSessionDao = db.readingSessionDao()
}
