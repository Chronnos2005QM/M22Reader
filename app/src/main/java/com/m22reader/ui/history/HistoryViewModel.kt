package com.m22reader.ui.history

import androidx.lifecycle.ViewModel
import com.m22reader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(repo: BookRepository) : ViewModel() {
    val history = repo.readingHistory
}
