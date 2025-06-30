package com.example.soulvent.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.soulvent.R
import com.example.soulvent.model.Comment
import com.example.soulvent.utils.formatTimestamp
import com.example.soulvent.utils.parseMarkdown
import com.example.soulvent.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    navController: NavController,
    postId: String,
    viewModel: PostViewModel = viewModel()
) {
    val comments by viewModel.commentsForSelectedPost.collectAsState()
    val isCommentsLoading by viewModel.isCommentsLoading.collectAsState()
    var newCommentText by remember { mutableStateOf("") }
    val commentsLoadError by viewModel.commentsLoadError.collectAsState()
    val isAddingComment by viewModel.isAddingComment.collectAsState()
    var commentToBlock by remember { mutableStateOf<Comment?>(null) }
    var commentToEdit by remember { mutableStateOf<Comment?>(null) }
    var commentToReport by remember { mutableStateOf<Comment?>(null) }
    var editedCommentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.loadCommentsForPost(postId)
    }

    if (commentToBlock != null) {
        AlertDialog(
            onDismissRequest = { commentToBlock = null },
            title = { Text("Block User") },
            text = { Text("Are you sure you want to block this user? You will no longer see their posts or comments.") },
            confirmButton = {
                Button(onClick = {
                    commentToBlock?.userId?.let { viewModel.blockUser(it) }
                    commentToBlock = null
                }) { Text("Block") }
            },
            dismissButton = {
                Button(onClick = { commentToBlock = null }) { Text("Cancel") }
            }
        )
    }

    if (commentToEdit != null) {
        AlertDialog(
            onDismissRequest = { commentToEdit = null },
            title = { Text("Edit Comment") },
            text = {
                OutlinedTextField(
                    value = editedCommentText,
                    onValueChange = { editedCommentText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        commentToEdit?.let {
                            viewModel.editComment(it.postId, it.id, editedCommentText) {
                                commentToEdit = null
                            }
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { commentToEdit = null }) { Text("Cancel") }
            }
        )
    }

    if (commentToReport != null) {
        AlertDialog(
            onDismissRequest = { commentToReport = null },
            title = { Text("Report Comment") },
            text = { Text("Are you sure you want to report this comment?") },
            confirmButton = {
                Button(onClick = {
                    commentToReport?.let { viewModel.reportComment(it.postId, it.id) }
                    commentToReport = null
                }) { Text("Report") }
            },
            dismissButton = {
                Button(onClick = { commentToReport = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.comments_title), color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button_content_description), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        bottomBar = {
            CommentInput(
                commentText = newCommentText,
                onCommentTextChange = { newCommentText = it },
                onSendClick = {
                    if (newCommentText.isNotBlank()) {
                        viewModel.addComment(postId, newCommentText) { newCommentText = "" }
                    }
                },
                isSending = isAddingComment
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (commentsLoadError != null) {
                // Error UI
            } else if (isCommentsLoading && comments.isEmpty()) {
                // Loading UI
            } else if (comments.isEmpty() && !isCommentsLoading) {
                // No comments UI
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = comments, key = { it.id }) { comment ->
                        CommentItem(
                            comment = comment,
                            viewModel = viewModel,
                            onBlockClick = { commentToBlock = comment },
                            onEditClick = {
                                editedCommentText = it.content
                                commentToEdit = it
                            },
                            onReportClick = { commentToReport = comment }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    viewModel: PostViewModel,
    onBlockClick: () -> Unit,
    onEditClick: (Comment) -> Unit,
    onReportClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val currentUserId by viewModel.currentUserId.collectAsState()

    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parseMarkdown(comment.content),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val timestampText = comment.timestamp?.let { formatTimestamp(it) } ?: "..."
                Text(
                    text = timestampText + if (comment.lastEdited != null) " (edited)" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (comment.userId == currentUserId) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEditClick(comment)
                            }
                        )
                    }
                    if (comment.userId != currentUserId && currentUserId != null) {
                        DropdownMenuItem(
                            text = { Text("Block User") },
                            onClick = {
                                showMenu = false
                                onBlockClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Report") },
                            onClick = {
                                showMenu = false
                                onReportClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentInput(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean
) {
    Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 4.dp) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                label = { Text(stringResource(R.string.add_a_comment_label)) },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                enabled = !isSending,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSendClick,
                enabled = commentText.isNotBlank() && !isSending,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(12.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Send, contentDescription = stringResource(R.string.send_comment_button_content_description))
                }
            }
        }
    }
}