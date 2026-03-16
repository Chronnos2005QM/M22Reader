package com.m22reader.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m22reader.data.model.Book
import com.m22reader.data.model.Collection
import com.m22reader.data.repository.BookRepository
import com.m22reader.data.repository.CollectionRepository
import com.m22reader.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode { GRID, LIST }
enum class SortOrder { DATE_ADDED, TITLE, LAST_READ, PROGRESS }
enum class LibraryTab { COLLECTIONS, FILES }

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookRepo: BookRepository,
    private val collectionRepo: CollectionRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.GRID)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _activeTab = MutableStateFlow(LibraryTab.COLLECTIONS)
    val activeTab: StateFlow<LibraryTab> = _activeTab.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val collections: StateFlow<List<Collection>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) collectionRepo.allCollections
            else collectionRepo.search(query)
        }
        .combine(_sortOrder) { cols, sort ->
            when (sort) {
                SortOrder.TITLE      -> cols.sortedBy { it.name }
                SortOrder.LAST_READ  -> cols.sortedByDescending { it.lastReadAt }
                SortOrder.PROGRESS   -> cols.sortedByDescending { it.progressPercent }
                SortOrder.DATE_ADDED -> cols
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val books: StateFlow<List<Book>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) bookRepo.allBooks else bookRepo.search(query)
        }
        .combine(_sortOrder) { books, sort ->
            when (sort) {
                SortOrder.TITLE      -> books.sortedBy { it.title }
                SortOrder.LAST_READ  -> books.sortedByDescending { it.lastReadAt }
                SortOrder.PROGRESS   -> books.sortedByDescending { it.progressPercent }
                SortOrder.DATE_ADDED -> books
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val folder = settings.libraryFolder.first()
            val autoScan = settings.autoScan.first()
            if (folder.isNotEmpty() && autoScan) {
                _isScanning.value = true
                collectionRepo.scanLibraryFolder(folder)
                _isScanning.value = false
            }
        }
    }

    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun toggleViewMode() { _viewMode.value = if (_viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID }
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
    fun setActiveTab(tab: LibraryTab) { _activeTab.value = tab }

    fun toggleFavoriteCollection(col: Collection) = viewModelScope.launch {
        collectionRepo.toggleFavorite(col.id, col.isFavorite)
    }
    fun toggleFavoriteBook(book: Book) = viewModelScope.launch {
        bookRepo.toggleFavorite(book.id, book.isFavorite)
    }
    fun scanLibrary() = viewModelScope.launch {
        _isScanning.value = true
        val folder = settings.libraryFolder.first()
        if (folder.isNotEmpty()) collectionRepo.scanLibraryFolder(folder)
        _isScanning.value = false
    }
}
