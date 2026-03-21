package com.m22reader.ui.library

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor() : ViewModel() {
    // Exemplo de função corrigida
    fun allCollections(): List<String> {
        return emptyList()
    }
}
