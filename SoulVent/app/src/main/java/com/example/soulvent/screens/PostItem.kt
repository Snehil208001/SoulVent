package com.example.soulvent.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.soulvent.R
import com.example.soulvent.model.Post
import com.example.soulvent.utils.formatTimestamp
import com.example.soulvent.utils.parseMarkdown
import com.example.soulvent.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostItem(
    post: Post,
    onCommentClick: (Post) -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    var userReaction by remember { mutableStateOf<String?>(null) }
    var showReactions by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf(post.content) }

    val currentUserId by viewModel.currentUserId.collectAsState()

    LaunchedEffect(post.id) {
        userReaction = viewModel.getUserReaction(post.id)
    }

    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            title = { Text("Block User") },
            text = { Text("Are you sure you want to block this user? You will no longer see their posts or comments.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.blockUser(post.userId)
                        showBlockConfirmDialog = false
                    }
                ) { Text("Block") }
            },
            dismissButton = { Button(onClick = { showBlockConfirmDialog = false }) { Text("Cancel") } }
        )
    }

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
                Button(
                    onClick = {
                        viewModel.editPost(post.id, editedContent) {
                            showEditDialog = false
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Post") },
            text = { Text("Are you sure you want to report this post?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reportPost(post.id)
                        showReportDialog = false
                    }
                ) { Text("Report") }
            },
            dismissButton = { Button(onClick = { showReportDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(1.dp))
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (post.userId == currentUserId) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    editedContent = post.content
                                    showEditDialog = true
                                    showMenu = false
                                }
                            )
                        }
                        if (post.userId != currentUserId && currentUserId != null) {
                            DropdownMenuItem(
                                text = { Text("Block User") },
                                onClick = {
                                    showMenu = false
                                    showBlockConfirmDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Report") },
                                onClick = {
                                    showMenu = false
                                    showReportDialog = true
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "AI-generated art for the post",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = parseMarkdown(post.content),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (post.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    post.tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .clickable { viewModel.setTagFilter(tag) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = stringResource(R.string.post_timestamp_content_description),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = stringResource(
                        R.string.timestamp_format,
                        formatTimestamp(post.timestamp)
                    ) + if (post.lastEdited != null) " (edited)" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.animateContentSize()
                ) {
                    ReactionButton(
                        onClick = { showReactions = !showReactions },
                        userReaction = userReaction
                    )

                    // Corrected Reaction Count Display
                    post.reactions.forEach { (type, count) ->
                        if (count > 0) {
                            Text("  ${getEmojiForReaction(type)} $count", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

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

            if (showReactions) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val reactionTypes = listOf("like", "hug", "support")
                    reactionTypes.forEach { type ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (userReaction == type) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .clickable {
                                    viewModel.toggleReaction(post.id, type)
                                    userReaction = if (userReaction == type) null else type
                                    showReactions = false
                                }
                                .padding(8.dp)
                        ) {
                            Text(getEmojiForReaction(type), fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReactionButton(onClick: () -> Unit, userReaction: String?) {
    OutlinedButton(onClick = onClick) {
        Text(getEmojiForReaction(userReaction ?: "like"))
        if (userReaction != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text("You reacted")
        }
    }
}

fun getEmojiForReaction(type: String): String {
    return when (type) {
        "like" -> "❤️"
        "hug" -> "🤗"
        "support" -> "🙏"
        else -> "❤️" // Default emoji
    }
}