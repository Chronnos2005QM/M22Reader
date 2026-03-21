package com.m22reader.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor() : ViewModel() {
    // Exemplo de função corrigida
    fun getStats(): List<String> {
        return emptyList()
    }
}
