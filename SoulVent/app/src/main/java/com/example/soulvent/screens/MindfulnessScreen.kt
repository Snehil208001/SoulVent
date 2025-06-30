package com.example.soulvent.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindfulnessScreen(navController: NavController) {
    var animationState by remember { mutableStateOf(BreathingPhase.Inhale) }
    val transition = updateTransition(targetState = animationState, label = "Breathing")

    val circleSize by transition.animateFloat(
        transitionSpec = {
            when {
                initialState == BreathingPhase.Inhale && targetState == BreathingPhase.HoldIn -> tween(4000, easing = LinearEasing)
                initialState == BreathingPhase.HoldIn && targetState == BreathingPhase.Exhale -> tween(1000)
                initialState == BreathingPhase.Exhale && targetState == BreathingPhase.HoldOut -> tween(6000, easing = LinearEasing)
                else -> tween(1000)
            }
        }, label = "CircleSize"
    ) { state ->
        when (state) {
            BreathingPhase.Inhale, BreathingPhase.HoldOut -> 100f
            BreathingPhase.HoldIn, BreathingPhase.Exhale -> 300f
        }
    }

    val instruction by remember(animationState) {
        derivedStateOf {
            when (animationState) {
                BreathingPhase.Inhale -> "Breathe In..."
                BreathingPhase.HoldIn -> "Hold"
                BreathingPhase.Exhale -> "Breathe Out..."
                BreathingPhase.HoldOut -> "Hold"
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            animationState = BreathingPhase.Inhale
            delay(4000) // Inhale duration
            animationState = BreathingPhase.HoldIn
            delay(1000) // Hold duration
            animationState = BreathingPhase.Exhale
            delay(6000) // Exhale duration
            animationState = BreathingPhase.HoldOut
            delay(1000) // Hold duration
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guided Breathing") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(300.dp)) {
                drawCircle(
                    color = Color.Cyan,
                    radius = circleSize,
                    center = Offset(size.width / 2, size.height / 2),
                    style = Stroke(width = 8f)
                )
            }
            Text(
                text = instruction,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private enum class BreathingPhase {
    Inhale, HoldIn, Exhale, HoldOut
}