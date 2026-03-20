package com.m22reader.ui.updates

import androidx.lifecycle.ViewModel
import com.m22reader.data.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor(repo: BookRepository) : ViewModel() {
    val recentlyAdded = repo.recentlyAdded
}
