package com.jonasrosendo.instagramclone.navigation

import androidx.navigation.NavController

sealed class DestinationScreen(val route: String) {
    object SignUp : DestinationScreen(SIGNUP)
    object SignIn : DestinationScreen(SIGNIN)
    object Feed : DestinationScreen(FEED)
    object MyPosts : DestinationScreen(MYPOSTS)
    object Search : DestinationScreen(SEARCH)

    companion object {
        private const val SIGNUP = "SIGNUP"
        private const val SIGNIN = "SIGNIN"
        private const val FEED = "FEED"
        private const val MYPOSTS = "MYPOSTS"
        private const val SEARCH = "SEARCH"
    }
}

fun NavController.navigateTo(destinationScreen: DestinationScreen) {
    navigate(destinationScreen.route) {
        popUpTo(destinationScreen.route)
        launchSingleTop = true
    }
}
