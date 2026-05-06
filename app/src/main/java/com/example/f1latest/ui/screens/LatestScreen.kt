package com.example.f1latest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.f1latest.MainViewModel
import com.example.f1latest.F1UiState
import com.example.f1latest.Race
import com.example.f1latest.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatestScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.latestRaceState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("F1 Latest Results", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF1801))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.fetchLatestResults() },
                containerColor = Color(0xFFFF1801)
            ) {
                Text("Refresh", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is F1UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is F1UiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                is F1UiState.Success -> {
                    val race = state.data as Race
                    Column {
                        RaceHeader(race)
                        LazyColumn {
                            items(race.results ?: emptyList()) { result ->
                                ResultRow(result)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RaceHeader(race: Race) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEEEEEE))
            .padding(16.dp)
    ) {
        Text(
            text = "Season ${race.season} - Round ${race.round}",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = race.raceName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = race.circuit.circuitName,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ResultRow(result: Result) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = result.position,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${result.driver.givenName} ${result.driver.familyName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = result.constructor.name,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${result.points} pts",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF1801)
                )
                Text(
                    text = result.time?.time ?: result.status,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
