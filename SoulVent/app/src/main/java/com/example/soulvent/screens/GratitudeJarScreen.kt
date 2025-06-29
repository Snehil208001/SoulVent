package com.example.soulvent.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soulvent.viewmodel.GratitudeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GratitudeJarScreen(
    navController: NavController,
    viewModel: GratitudeViewModel = viewModel()
) {
    val notes by viewModel.gratitudeNotes.collectAsState()
    var newNoteText by remember { mutableStateOf("") }
    var showRandomNoteDialog by remember { mutableStateOf<String?>(null) }

    if (showRandomNoteDialog != null) {
        AlertDialog(
            onDismissRequest = { showRandomNoteDialog = null },
            title = { Text("A Moment of Gratitude") },
            text = { Text(showRandomNoteDialog ?: "") },
            confirmButton = {
                Button(onClick = { showRandomNoteDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gratitude Jar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Button(
                onClick = { if (notes.isNotEmpty()) showRandomNoteDialog = notes.random() },
                enabled = notes.isNotEmpty()
            ) {
                Text("View a Random Note")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newNoteText,
                    onValueChange = { newNoteText = it },
                    label = { Text("What are you grateful for today?") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.addNote(newNoteText)
                        newNoteText = ""
                    },
                    enabled = newNoteText.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()

            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 16.dp)) {
                if(notes.isEmpty()){
                    item {
                        Text("Your gratitude jar is empty. Add a note to begin!")
                    }
                } else {
                    items(notes) { note ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(note, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.deleteNote(note) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete note")
                            }
                        }
                    }
                }
            }
        }
    }
}