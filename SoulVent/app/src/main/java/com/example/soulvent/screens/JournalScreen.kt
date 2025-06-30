package com.example.soulvent.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soulvent.nav.Screen
import com.example.soulvent.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    navController: NavController,
    viewModel: PostViewModel = viewModel()
) {
    val userPosts by viewModel.userPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Journal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isLoading && userPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (userPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't written any posts yet.", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = userPosts, key = { it.id }) { post ->
                        JournalPostItem(
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