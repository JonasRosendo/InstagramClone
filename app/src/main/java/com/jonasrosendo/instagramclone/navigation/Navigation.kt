package com.jonasrosendo.instagramclone.navigation

import androidx.navigation.NavController

sealed class DestinationScreen(val route: String) {
    object SignUp : DestinationScreen(SIGNUP)
    object SignIn : DestinationScreen(SIGN_IN)
    object Feed : DestinationScreen(FEED)
    object MyPosts : DestinationScreen(MY_POSTS)
    object Search : DestinationScreen(SEARCH)
    object Profile : DestinationScreen(PROFILE)

    companion object {
        private const val SIGNUP = "SIGNUP"
        private const val SIGN_IN = "SIGN_IN"
        private const val FEED = "FEED"
        private const val MY_POSTS = "MY_POSTS"
        private const val SEARCH = "SEARCH"
        private const val PROFILE = "PROFILE"
    }
}

fun NavController.navigateTo(destinationScreen: DestinationScreen) {
    navigate(destinationScreen.route) {
        popUpTo(destinationScreen.route)
        launchSingleTop = true
    }
}
