package com.m22reader.ui.chapters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m22reader.data.model.Chapter
import com.m22reader.data.model.Collection
import com.m22reader.data.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChaptersViewModel @Inject constructor(
    private val repo: CollectionRepository
) : ViewModel() {

    private val _collectionId = MutableStateFlow<Long>(0)

    val collection: StateFlow<Collection?> = _collectionId
        .filter { it > 0 }
        .flatMapLatest { id ->
            flow { emit(repo.getById(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val chapters: StateFlow<List<Chapter>> = _collectionId
        .filter { it > 0 }
        .flatMapLatest { id -> repo.getChapters(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(collectionId: Long) {
        _collectionId.value = collectionId
    }
}
