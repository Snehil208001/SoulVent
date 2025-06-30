package com.example.soulvent.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Import sp for TextUnit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.soulvent.ui.theme.*
import com.example.soulvent.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodHistoryScreen(
    navController: NavController,
    viewModel: PostViewModel = viewModel()
) {
    val userPosts by viewModel.userPosts.collectAsState()

    val moodCounts = userPosts
        .map { it.mood.split(" ").last() } // "😊 Happy" -> "Happy"
        .groupingBy { it }
        .eachCount()

    val pieChartData = PieChartData(
        slices = moodCounts.map { (mood, count) ->
            PieChartData.Slice(
                label = mood,
                value = count.toFloat(),
                color = getMoodColor(mood)
            )
        },
        plotType = PlotType.Donut
    )

    val pieChartConfig = PieChartConfig(
        isAnimationEnable = true,
        showSliceLabels = true,
        sliceLabelTextSize = 16.sp, // Changed from 16f to 16.sp
        labelVisible = true,
        strokeWidth = 120f,
        labelColor = MaterialTheme.colorScheme.onSurface
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Mood History") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You haven't posted anything yet. Share a vent to track your mood!")
                }
            } else {
                Text("Your Mood Distribution", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(32.dp))
                PieChart(
                    modifier = Modifier.size(300.dp),
                    pieChartData = pieChartData,
                    pieChartConfig = pieChartConfig
                )
            }
        }
    }
}

private fun getMoodColor(mood: String): Color = when (mood) {
    "Happy" -> SunsetLightOrange
    "Sad" -> OceanLightBlue
    "Anxious" -> ForestLightGreen
    "Grateful" -> Pink40
    "Angry" -> PurpleGrey40
    else -> Color.Gray
}