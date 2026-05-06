package com.example.f1latest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class F1UiState {
    object Loading : F1UiState()
    data class Success(val race: Race) : F1UiState()
    data class Error(val message: String) : F1UiState()
}

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<F1UiState>(F1UiState.Loading)
    val uiState: StateFlow<F1UiState> = _uiState.asStateFlow()

    private val apiService = F1ApiService.create()

    init {
        fetchLatestResults()
    }

    fun fetchLatestResults() {
        _uiState.value = F1UiState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getLatestResults()
                val races = response.mrData.raceTable.races
                if (races.isNotEmpty()) {
                    _uiState.value = F1UiState.Success(races.first())
                } else {
                    _uiState.value = F1UiState.Error("No race data found.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = F1UiState.Error("Failed to fetch data: ${e.localizedMessage}")
            }
        }
    }
}
