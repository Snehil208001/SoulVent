package com.example.soulvent.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

// This data class remains the same
data class DrawnLine(
    val path: Path,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    drawingColor: Color,
    strokeWidth: Float,
    clear: Boolean,
    onCleared: () -> Unit
) {
    // This holds the list of lines that are completed
    val completedLines = remember { mutableStateListOf<DrawnLine>() }

    // This will hold the path for the line currently being drawn
    var inProgressPath by remember { mutableStateOf<Path?>(null) }

    // This effect will run when the clear signal is received
    LaunchedEffect(clear) {
        if (clear) {
            completedLines.clear()
            inProgressPath = null
            onCleared()
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    // When the user first touches the screen
                    onDragStart = { offset ->
                        // Start a new path for the line being drawn
                        inProgressPath = Path().apply { moveTo(offset.x, offset.y) }
                    },
                    // As the user drags their finger
                    onDrag = { change, dragAmount ->
                        // Add the new points to the in-progress path
                        inProgressPath?.lineTo(change.position.x, change.position.y)
                        // Force a redraw of the canvas to show the updated in-progress line
                        change.consume()
                    },
                    // When the user lifts their finger
                    onDragEnd = {
                        // The line is complete. Add it to our list of completed lines.
                        inProgressPath?.let {
                            completedLines.add(DrawnLine(it, drawingColor, strokeWidth))
                        }
                        // Reset the in-progress path
                        inProgressPath = null
                    }
                )
            }
    ) {
        // First, draw all the lines that are already completed
        completedLines.forEach { line ->
            drawPath(
                path = line.path,
                color = line.color,
                style = Stroke(width = line.strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Then, draw the line that is currently being drawn on top
        inProgressPath?.let {
            drawPath(
                path = it,
                color = drawingColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}