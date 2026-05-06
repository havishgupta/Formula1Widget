package com.example.f1latest.ui.screens

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
import com.example.f1latest.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChampionshipsScreen(viewModel: MainViewModel = viewModel()) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Drivers", "Constructors")

    val wdcState by viewModel.wdcState.collectAsState()
    val wccState by viewModel.wccState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("World Championships", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFF1801))
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFFEEEEEE)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTabIndex == 0) {
                    when (val state = wdcState) {
                        is F1UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        is F1UiState.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                        is F1UiState.Success -> {
                            val standings = state.data as List<DriverStanding>
                            LazyColumn {
                                items(standings) { standing ->
                                    DriverStandingRow(standing)
                                }
                            }
                        }
                    }
                } else {
                    when (val state = wccState) {
                        is F1UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        is F1UiState.Error -> Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center).padding(16.dp))
                        is F1UiState.Success -> {
                            val standings = state.data as List<ConstructorStanding>
                            LazyColumn {
                                items(standings) { standing ->
                                    ConstructorStandingRow(standing)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverStandingRow(standing: DriverStanding) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = standing.position, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${standing.driver.givenName} ${standing.driver.familyName}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                val teamNames = standing.constructors.joinToString(", ") { it.name }
                Text(teamNames, fontSize = 14.sp, color = Color.Gray)
            }
            Text("${standing.points} pts", fontWeight = FontWeight.Bold, color = Color(0xFFFF1801))
        }
    }
}

@Composable
fun ConstructorStandingRow(standing: ConstructorStanding) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = standing.position, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            Text(standing.constructor.name, fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text("${standing.points} pts", fontWeight = FontWeight.Bold, color = Color(0xFFFF1801))
        }
    }
}
