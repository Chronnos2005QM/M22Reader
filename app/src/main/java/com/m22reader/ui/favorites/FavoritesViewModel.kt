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
