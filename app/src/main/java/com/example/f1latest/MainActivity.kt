package com.example.f1latest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.f1latest.ui.screens.ChampionshipsScreen
import com.example.f1latest.ui.screens.LatestScreen
import com.example.f1latest.ui.screens.ScheduleScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val sharedViewModel: MainViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = "Championships") },
                    label = { Text("Standings") },
                    selected = currentRoute == "championships",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF1801),
                        selectedTextColor = Color(0xFFFF1801)
                    ),
                    onClick = {
                        navController.navigate("championships") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Timer, contentDescription = "Latest") },
                    label = { Text("Latest") },
                    selected = currentRoute == "latest",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF1801),
                        selectedTextColor = Color(0xFFFF1801)
                    ),
                    onClick = {
                        navController.navigate("latest") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = "All Races") },
                    label = { Text("Schedule") },
                    selected = currentRoute == "schedule",
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFF1801),
                        selectedTextColor = Color(0xFFFF1801)
                    ),
                    onClick = {
                        navController.navigate("schedule") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "latest",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("championships") { ChampionshipsScreen(viewModel = sharedViewModel) }
            composable("latest") { LatestScreen(viewModel = sharedViewModel) }
            composable("schedule") { ScheduleScreen(viewModel = sharedViewModel) }
        }
    }
}
