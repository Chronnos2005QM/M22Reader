package com.m22reader.ui.updates

import androidx.lifecycle.ViewModel
import com.m22reader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor(repo: BookRepository) : ViewModel() {
    val recentlyAdded = repo.recentlyAdded
}

// ─────────────────────────────────────────────────────────────────────────────

package com.m22reader.ui.history

import androidx.lifecycle.ViewModel
import com.m22reader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(repo: BookRepository) : ViewModel() {
    val history = repo.readingHistory
}

// ─────────────────────────────────────────────────────────────────────────────

package com.m22reader.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m22reader.data.model.Book
import com.m22reader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repo: BookRepository) : ViewModel() {
    val favorites = repo.favorites
    fun toggleFavorite(book: Book) = viewModelScope.launch { repo.toggleFavorite(book.id, book.isFavorite) }
}
