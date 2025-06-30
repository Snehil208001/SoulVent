package com.example.soulvent.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soulvent.R
import com.example.soulvent.nav.Screen
import com.example.soulvent.viewmodel.PostViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: PostViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isPostsLoading by viewModel.isLoading.collectAsState()
    val loadError by viewModel.loadError.collectAsState()
    val selectedMoodFilter by viewModel.selectedMoodFilter.collectAsState()
    val dailyPrompt by viewModel.dailyPrompt.collectAsState()
    val moods = listOf("All", "😊 Happy", "😢 Sad", "😬 Anxious", "🙏 Grateful", "😡 Angry")

    val currentOnRefresh by rememberUpdatedState { viewModel.loadPosts() }
    val currentOnAddPostClick by rememberUpdatedState { navController.navigate(Screen.Post.route) }

    LaunchedEffect(Unit) {
        currentOnRefresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("meditation_library") }) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement, // A fitting icon for meditation
                            contentDescription = "Guided Meditations",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate("mindfulness") }) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = "Mindfulness Tool",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate("art_canvas") }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Art Canvas",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate("gratitude_jar") }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Gratitude Jar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = currentOnAddPostClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.new_vent_content_description)
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(items = moods, key = { it }) { mood ->
                    val isSelected = (selectedMoodFilter == mood) || (selectedMoodFilter == null && mood == "All")
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val filter = if (mood == "All") null else mood
                            viewModel.setMoodFilter(filter)
                        },
                        label = { Text(mood) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            dailyPrompt?.let { prompt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            navController.navigate("${Screen.Post.route}?prompt=${prompt}")
                        },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Prompt of the Day", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(prompt, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = isPostsLoading),
                onRefresh = currentOnRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                if (loadError != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.error_loading_data, loadError ?: "Unknown error"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = currentOnRefresh) {
                                Text(stringResource(R.string.retry_button_text))
                            }
                        }
                    }
                } else if (isPostsLoading && posts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (posts.isEmpty() && !isPostsLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_vents_message),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = posts, key = { it.id }) { post ->
                            PostItem(
                                post = post,
                                onCommentClick = { selectedPost ->
                                    navController.navigate(Screen.Comments.createRoute(selectedPost.id))
                                },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}