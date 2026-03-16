package com.m22reader.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m22reader.data.model.Book
import com.m22reader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode { GRID, LIST }
enum class SortOrder { DATE_ADDED, TITLE, LAST_READ, PROGRESS }

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: BookRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val books: StateFlow<List<Book>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) repo.allBooks else repo.search(query)
        }
        .combine(_sortOrder) { books, sort ->
            when (sort) {
                SortOrder.TITLE       -> books.sortedBy { it.title }
                SortOrder.LAST_READ   -> books.sortedByDescending { it.lastReadAt }
                SortOrder.PROGRESS    -> books.sortedByDescending { it.progressPercent }
                SortOrder.DATE_ADDED  -> books // already sorted by Room query
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun toggleViewMode() { _viewMode.value = if (_viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID }
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
    fun toggleFavorite(book: Book) = viewModelScope.launch { repo.toggleFavorite(book.id, book.isFavorite) }
    fun deleteBook(book: Book) = viewModelScope.launch { repo.deleteBook(book) }
}
