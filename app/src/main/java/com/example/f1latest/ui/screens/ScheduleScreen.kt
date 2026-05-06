package com.example.f1latest.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1latest.F1UiState
import com.example.f1latest.MainViewModel
import com.example.f1latest.Race

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: MainViewModel = viewModel()) {
    val scheduleState by viewModel.scheduleState.collectAsState()
    
    // Simple state to hold the selected round for viewing results inline
    var expandedRound by remember { mutableStateOf<String?>(null) }
    val specificRaceState by viewModel.specificRaceState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Season Schedule", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF1801))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = scheduleState) {
                is F1UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is F1UiState.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                is F1UiState.Success -> {
                    val races = state.data as List<Race>
                    LazyColumn {
                        items(races) { race ->
                            RaceScheduleCard(
                                race = race,
                                isExpanded = expandedRound == race.round,
                                onClick = {
                                    if (expandedRound == race.round) {
                                        expandedRound = null
                                    } else {
                                        expandedRound = race.round
                                        viewModel.fetchRaceResults(race.round)
                                    }
                                },
                                specificRaceState = specificRaceState
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RaceScheduleCard(
    race: Race, 
    isExpanded: Boolean, 
    onClick: () -> Unit,
    specificRaceState: F1UiState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Round ${race.round}", fontSize = 12.sp, color = Color.Gray)
            Text(race.raceName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(race.circuit.circuitName, fontSize = 14.sp)
            Text("Date: ${race.date}", fontSize = 14.sp, color = Color(0xFFFF1801))
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Race Results", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                when (val state = specificRaceState) {
                    is F1UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp))
                    is F1UiState.Error -> Text(state.message, color = Color.Red, fontSize = 12.sp)
                    is F1UiState.Success -> {
                        val specificRace = state.data as Race
                        if (specificRace.round == race.round) {
                            val results = specificRace.results
                            if (results.isNullOrEmpty()) {
                                Text("No results available yet.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 14.sp)
                            } else {
                                results.take(10).forEach { result ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                        Text("${result.position}. ", fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
                                        Text("${result.driver.givenName} ${result.driver.familyName}", modifier = Modifier.weight(1f))
                                        Text(result.points, color = Color(0xFFFF1801), fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (results.size > 10) {
                                    Text("...and ${results.size - 10} more.", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
