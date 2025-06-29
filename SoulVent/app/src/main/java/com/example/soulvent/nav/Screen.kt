package com.example.soulvent.nav

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Post : Screen("post")
    object Comments : Screen("comments/{postId}") {
        fun createRoute(postId: String) = "comments/$postId"
    }
}