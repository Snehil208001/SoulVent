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
import com.example.soulvent.screens.ArtCanvasScreen
import com.example.soulvent.screens.CommentsScreen
import com.example.soulvent.screens.GratitudeJarScreen
import com.example.soulvent.screens.HomeScreen
import com.example.soulvent.screens.PostVentScreen
import com.example.soulvent.screens.SettingsScreen

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
        composable(Screen.Post.route) {
            PostVentScreen(navController = navController)
        }
        composable(
            route = Screen.Comments.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            if (postId != null) {
                CommentsScreen(navController = navController, postId = postId)
            } else {
                navController.popBackStack()
            }
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable("gratitude_jar") {
            GratitudeJarScreen(navController = navController)
        }
        // Add the new route for the Art Canvas screen
        composable("art_canvas") {
            ArtCanvasScreen(navController = navController)
        }
    }
}