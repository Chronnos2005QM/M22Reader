package com.m22reader.ui.reader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m22reader.data.model.Book
import com.m22reader.data.model.BookFormat
import com.m22reader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────
data class ReaderUiState(
    val book: Book? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val showControls: Boolean = true,
    val readingMode: ReadingMode = ReadingMode.VERTICAL_SCROLL,
    val brightness: Float = 1f,
    val keepScreenOn: Boolean = true,
)

enum class ReadingMode {
    VERTICAL_SCROLL,   // Manhwa padrão — scroll contínuo
    HORIZONTAL_PAGED,  // Manga — página por página, direita→esquerda
    WEBTOON,           // Igual ao vertical mas sem espaços entre páginas
}

// ── ViewModel ─────────────────────────────────────────────────────────────────
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repo: BookRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val book = repo.getById(bookId)
            if (book == null) {
                _uiState.update { it.copy(isLoading = false, error = "Livro não encontrado.") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    book        = book,
                    currentPage = book.lastReadPage,
                    isLoading   = false,
                )
            }
        }
    }

    fun onPageChanged(page: Int, totalPages: Int) {
        _uiState.update { it.copy(currentPage = page, totalPages = totalPages) }
        // Persist progress (debounced via Room)
        viewModelScope.launch {
            _uiState.value.book?.let { book ->
                repo.updateProgress(book.id, chapter = (page / maxOf(totalPages / book.totalChapters, 1)), page = page)
            }
        }
    }

    fun toggleControls() = _uiState.update { it.copy(showControls = !it.showControls) }
    fun setReadingMode(mode: ReadingMode) = _uiState.update { it.copy(readingMode = mode) }
    fun setBrightness(v: Float) = _uiState.update { it.copy(brightness = v.coerceIn(0.1f, 1f)) }
    fun dismissError() = _uiState.update { it.copy(error = null) }
}
