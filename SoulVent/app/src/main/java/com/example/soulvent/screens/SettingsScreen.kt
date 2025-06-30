package com.example.soulvent.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soulvent.ui.theme.AppThemes
import com.example.soulvent.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val currentThemeName by settingsViewModel.themeName.collectAsState()
    val themes = AppThemes.keys.toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item {
                Text(
                    "General",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("journal") }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = "My Journal",
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("My Journal", style = MaterialTheme.typography.bodyLarge)
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("mood_history") }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Leaderboard,
                        contentDescription = "Mood History",
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("View Mood History", style = MaterialTheme.typography.bodyLarge)
                }
            }
            item {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Text(
                    "Choose Your Theme",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(themes) { themeName ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { settingsViewModel.setTheme(themeName) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(themeName, style = MaterialTheme.typography.bodyLarge)
                    if (themeName == currentThemeName) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected Theme",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}