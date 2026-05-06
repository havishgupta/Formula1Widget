package com.example.f1latest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class F1UiState {
    object Loading : F1UiState()
    data class Success(val data: Any) : F1UiState()
    data class Error(val message: String) : F1UiState()
}

class MainViewModel : ViewModel() {
    private val apiService = F1ApiService.create()

    private val _latestRaceState = MutableStateFlow<F1UiState>(F1UiState.Loading)
    val latestRaceState: StateFlow<F1UiState> = _latestRaceState.asStateFlow()

    private val _wdcState = MutableStateFlow<F1UiState>(F1UiState.Loading)
    val wdcState: StateFlow<F1UiState> = _wdcState.asStateFlow()

    private val _wccState = MutableStateFlow<F1UiState>(F1UiState.Loading)
    val wccState: StateFlow<F1UiState> = _wccState.asStateFlow()

    private val _scheduleState = MutableStateFlow<F1UiState>(F1UiState.Loading)
    val scheduleState: StateFlow<F1UiState> = _scheduleState.asStateFlow()

    init {
        fetchLatestResults()
        fetchStandings()
        fetchSchedule()
    }

    fun fetchLatestResults() {
        _latestRaceState.value = F1UiState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getLatestResults()
                val races = response.mrData.raceTable?.races
                if (!races.isNullOrEmpty()) {
                    _latestRaceState.value = F1UiState.Success(races.first())
                } else {
                    _latestRaceState.value = F1UiState.Error("No race data found.")
                }
            } catch (e: Exception) {
                _latestRaceState.value = F1UiState.Error("Failed to fetch latest: ${e.localizedMessage}")
            }
        }
    }

    fun fetchStandings() {
        _wdcState.value = F1UiState.Loading
        _wccState.value = F1UiState.Loading
        viewModelScope.launch {
            try {
                val wdcResponse = apiService.getDriverStandings()
                val wdcList = wdcResponse.mrData.standingsTable?.standingsLists?.firstOrNull()?.driverStandings
                if (wdcList != null) {
                    _wdcState.value = F1UiState.Success(wdcList)
                } else {
                    _wdcState.value = F1UiState.Error("No WDC data found.")
                }

                val wccResponse = apiService.getConstructorStandings()
                val wccList = wccResponse.mrData.standingsTable?.standingsLists?.firstOrNull()?.constructorStandings
                if (wccList != null) {
                    _wccState.value = F1UiState.Success(wccList)
                } else {
                    _wccState.value = F1UiState.Error("No WCC data found.")
                }

            } catch (e: Exception) {
                _wdcState.value = F1UiState.Error("Failed to fetch standings: ${e.localizedMessage}")
                _wccState.value = F1UiState.Error("Failed to fetch standings: ${e.localizedMessage}")
            }
        }
    }

    fun fetchSchedule() {
        _scheduleState.value = F1UiState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getSchedule()
                val races = response.mrData.raceTable?.races
                if (races != null) {
                    _scheduleState.value = F1UiState.Success(races)
                } else {
                    _scheduleState.value = F1UiState.Error("No schedule data found.")
                }
            } catch (e: Exception) {
                _scheduleState.value = F1UiState.Error("Failed to fetch schedule: ${e.localizedMessage}")
            }
        }
    }

    // Function to load specific race results from schedule
    private val _specificRaceState = MutableStateFlow<F1UiState>(F1UiState.Loading)
    val specificRaceState: StateFlow<F1UiState> = _specificRaceState.asStateFlow()

    fun fetchRaceResults(round: String) {
        _specificRaceState.value = F1UiState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getRaceResults(round)
                val races = response.mrData.raceTable?.races
                if (!races.isNullOrEmpty()) {
                    _specificRaceState.value = F1UiState.Success(races.first())
                } else {
                    _specificRaceState.value = F1UiState.Error("No results found for round $round.")
                }
            } catch (e: Exception) {
                _specificRaceState.value = F1UiState.Error("Failed to fetch results: ${e.localizedMessage}")
            }
        }
    }
}
