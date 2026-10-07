package com.justmusic.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Landing : Screen("landing", "Landing")
    object Home : Screen("home", "Home")
    object Songs : Screen("songs", "Songs")
    object Playlists : Screen("playlists", "Playlists")
    object Settings : Screen("settings", "Settings")
    object FullPlayer : Screen("full_player", "Player")
}
