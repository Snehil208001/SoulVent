package com.example.soulvent.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.soulvent.screens.*

@Composable
fun SoulMateNavGraph(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = "${Screen.Post.route}?prompt={prompt}",
            arguments = listOf(navArgument("prompt") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val prompt = backStackEntry.arguments?.getString("prompt")
            PostVentScreen(navController = navController, initialPrompt = prompt)
        }
        composable(
            route = Screen.Comments.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            if (postId != null) {
                CommentsScreen(navController = navController, postId = postId)
            }
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable("gratitude_jar") {
            GratitudeJarScreen(navController = navController)
        }
        composable("art_canvas") {
            ArtCanvasScreen(navController = navController)
        }
        composable("mindfulness") {
            MindfulnessScreen(navController = navController)
        }
        composable("mood_history") {
            MoodHistoryScreen(navController = navController)
        }
        composable("journal") {
            JournalScreen(navController = navController)
        }
        composable("meditation_library") {
            MeditationLibraryScreen(navController = navController)
        }
        composable(
            "meditation_player/{meditationId}",
            arguments = listOf(navArgument("meditationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val meditationId = backStackEntry.arguments?.getString("meditationId")
            if (meditationId != null) {
                MeditationPlayerScreen(navController = navController, meditationId = meditationId)
            }
        }
    }
}