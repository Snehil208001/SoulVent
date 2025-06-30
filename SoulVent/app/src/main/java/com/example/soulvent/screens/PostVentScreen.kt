package com.example.soulvent.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soulvent.R
import com.example.soulvent.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostVentScreen(
    navController: NavController,
    viewModel: PostViewModel = viewModel(),
    initialPrompt: String? = null
) {
    var text by remember { mutableStateOf(initialPrompt ?: "") }
    var tagsText by remember { mutableStateOf(if (initialPrompt != null) "dailyprompt" else "") }
    val moods = listOf("😊 Happy", "😢 Sad", "😬 Anxious", "🙏 Grateful", "😡 Angry")
    var selectedMood by remember { mutableStateOf<String?>(null) }
    val isAddingPost by viewModel.isAddingPost.collectAsState()
    val addPostError by viewModel.addPostError.collectAsState()
    val isGeneratingImage by viewModel.isGeneratingImage.collectAsState()
    val generatedBitmap by viewModel.generatedImageBitmap.collectAsState()

    val currentOnAddPostComplete by rememberUpdatedState {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.write_vent_title), color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { newText ->
                    if (!isAddingPost) {
                        text = newText
                    }
                },
                label = { Text(stringResource(R.string.whats_on_your_mind_label)) },
                placeholder = { Text(stringResource(R.string.whats_on_your_mind_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = 10,
                minLines = 3,
                enabled = !isAddingPost,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (generatedBitmap != null) {
                Image(
                    bitmap = generatedBitmap!!.asImageBitmap(),
                    contentDescription = "AI-generated art",
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Button(
                    onClick = { viewModel.generateArtForPost(text) },
                    enabled = text.isNotBlank() && !isGeneratingImage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGeneratingImage) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Generate Art from Your Vent")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                label = { Text("Add tags (e.g., #work, #study)") },
                placeholder = { Text("Comma-separated, like: work, relationships") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAddingPost
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("How are you feeling?", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEach { mood ->
                    val isSelected = selectedMood == mood
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMood = if (isSelected) null else mood },
                        label = { Text(mood) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected mood",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (addPostError != null) {
                Text(
                    text = stringResource(R.string.error_adding_post, addPostError ?: "Unknown error"),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        val tags = tagsText.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        viewModel.addPost(text, selectedMood ?: "", tags) {
                            currentOnAddPostComplete()
                        }
                    }
                },
                enabled = text.isNotBlank() && !isAddingPost,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    if (isAddingPost) stringResource(R.string.sharing_button_text)
                    else stringResource(R.string.share_anonymously_button)
                )
            }

            if (isAddingPost) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}