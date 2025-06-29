package com.example.soulvent.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    viewModel: PostViewModel = viewModel()
) {
    var text by remember { mutableStateOf("") }
    val moods = listOf("😊 Happy", "😢 Sad", "😬 Anxious", "🙏 Grateful", "😡 Angry")
    var selectedMood by remember { mutableStateOf<String?>(null) }

    val isAddingPost by viewModel.isAddingPost.collectAsState()
    val addPostError by viewModel.addPostError.collectAsState()

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
                        // FIX: This call now provides the `mood` and the `onComplete` callback correctly.
                        viewModel.addPost(text, selectedMood ?: "") {
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