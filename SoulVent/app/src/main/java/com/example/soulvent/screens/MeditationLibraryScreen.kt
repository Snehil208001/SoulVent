package com.example.soulvent.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soulvent.model.Meditation
import com.example.soulvent.viewmodel.MeditationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationLibraryScreen(
    navController: NavController,
    viewModel: MeditationViewModel = viewModel()
) {
    val meditations by viewModel.meditations.collectAsState()
    val groupedMeditations = meditations.groupBy { it.category }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guided Meditations") },
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
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedMeditations.forEach { (category, meditations) ->
                item {
                    Text(category, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(meditations) { meditation ->
                    MeditationListItem(meditation = meditation, onClick = {
                        navController.navigate("meditation_player/${meditation.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun MeditationListItem(meditation: Meditation, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(meditation.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(meditation.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}