package com.example.soulvent.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soulvent.R
import com.example.soulvent.model.Post
import com.example.soulvent.utils.formatTimestamp
import com.example.soulvent.utils.parseMarkdown
import com.example.soulvent.viewmodel.PostViewModel

@Composable
fun JournalPostItem(
    post: Post,
    onCommentClick: (Post) -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf(post.content) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Post") },
            text = {
                OutlinedTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.editPost(post.id, editedContent) { showEditDialog = false } }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to permanently delete this post?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deletePost(post.id) { showDeleteDialog = false } },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { Button(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.mood.isNotEmpty()) {
                    Text(
                        text = post.mood,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                editedContent = post.content
                                showEditDialog = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showDeleteDialog = true
                                showMenu = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = parseMarkdown(post.content), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = stringResource(R.string.post_timestamp_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = stringResource(R.string.timestamp_format, formatTimestamp(post.timestamp)) + if (post.lastEdited != null) " (edited)" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick(post) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = stringResource(R.string.comment_button_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = "${post.commentCount}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}