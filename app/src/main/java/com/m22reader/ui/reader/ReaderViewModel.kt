package com.m22reader.ui.reader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m22reader.data.model.Book
import com.m22reader.data.model.BookFormat
import com.m22reader.data.model.ReadingSession
import com.m22reader.data.repository.BookRepository
import com.m22reader.data.dao.ReadingSessionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReaderUiState(
    val book: Book? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val showControls: Boolean = true,
    val readingMode: ReadingMode = ReadingMode.VERTICAL_SCROLL,
    val brightness: Float = 1f,
)

enum class ReadingMode { VERTICAL_SCROLL, HORIZONTAL_PAGED, WEBTOON }

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repo: BookRepository,
    private val sessionDao: ReadingSessionDao,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    // Controlo do timer de sessão
    private var sessionStart = System.currentTimeMillis()
    private var timerJob: Job? = null
    private var pagesReadThisSession = 0

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val book = repo.getById(bookId)
            if (book == null) {
                _uiState.update { it.copy(isLoading = false, error = "Livro não encontrado.") }
                return@launch
            }
            _uiState.update { it.copy(book = book, currentPage = book.lastReadPage, isLoading = false) }
            startSessionTimer(bookId)
        }
    }

    private fun startSessionTimer(bookId: Long) {
        sessionStart = System.currentTimeMillis()
        timerJob?.cancel()
        // Guarda sessão a cada 60 segundos
        timerJob = viewModelScope.launch {
            while (true) {
                delay(60_000)
                saveSession(bookId)
            }
        }
    }

    private suspend fun saveSession(bookId: Long) {
        val duration = (System.currentTimeMillis() - sessionStart) / 1000
        if (duration > 5) {
            sessionDao.insert(ReadingSession(
                itemId = bookId,
                itemType = "book",
                durationSeconds = duration,
                pagesRead = pagesReadThisSession,
            ))
            // Reset para próximo intervalo
            sessionStart = System.currentTimeMillis()
            pagesReadThisSession = 0
        }
    }

    fun onPageChanged(page: Int, totalPages: Int) {
        val oldPage = _uiState.value.currentPage
        if (page != oldPage) pagesReadThisSession++
        _uiState.update { it.copy(currentPage = page, totalPages = totalPages) }
        viewModelScope.launch {
            _uiState.value.book?.let { book ->
                val chapterEstimate = if (book.totalChapters > 0 && totalPages > 0)
                    (page.toFloat() / totalPages * book.totalChapters).toInt() else page
                repo.updateProgress(book.id, chapterEstimate, page)
            }
        }
    }

    fun toggleControls() = _uiState.update { it.copy(showControls = !it.showControls) }
    fun setReadingMode(mode: ReadingMode) = _uiState.update { it.copy(readingMode = mode) }
    fun setBrightness(v: Float) = _uiState.update { it.copy(brightness = v.coerceIn(0.1f, 1f)) }
    fun dismissError() = _uiState.update { it.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        // Guarda sessão final ao sair
        _uiState.value.book?.let { book ->
            viewModelScope.launch { saveSession(book.id) }
        }
    }
}
