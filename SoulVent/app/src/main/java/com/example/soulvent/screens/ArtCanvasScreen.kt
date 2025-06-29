package com.example.soulvent.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.shreyaspatil.capturable.Capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtCanvasScreen(navController: NavController) {
    val context = LocalContext.current
    val captureController = rememberCaptureController()

    var drawingColor by remember { mutableStateOf(Color.Black) }
    var backgroundColor by remember { mutableStateOf(Color.White) }
    val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Black, Color.White)

    var clearCanvasSignal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Express Yourself") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clearCanvasSignal = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Canvas")
                    }
                    IconButton(onClick = {
                        captureController.capture()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Artwork")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Capturable(
                controller = captureController,
                modifier = Modifier.weight(1f),
                onCaptured = { bitmap, error ->
                    if (bitmap != null) {
                        Toast.makeText(context, "Artwork Saved!", Toast.LENGTH_SHORT).show()
                    }
                    if (error != null) {
                        Toast.makeText(context, "Error saving artwork.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                DrawingCanvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor),
                    drawingColor = drawingColor,
                    strokeWidth = 15f,
                    clear = clearCanvasSignal,
                    onCleared = { clearCanvasSignal = false }
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Brush Color", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (drawingColor == color) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { drawingColor = color }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Background Color", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (backgroundColor == color) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { backgroundColor = color }
                        )
                    }
                }
            }
        }
    }
}