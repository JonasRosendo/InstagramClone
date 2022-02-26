package com.jonasrosendo.instagramclone.navigation

import androidx.navigation.NavController

sealed class DestinationScreen(val route: String) {
    object SignUp : DestinationScreen(SIGNUP)
    object SignIn : DestinationScreen(SIGN_IN)
    object Feed : DestinationScreen(FEED)
    object MyPosts : DestinationScreen(MY_POSTS)
    object Search : DestinationScreen(SEARCH)
    object Profile : DestinationScreen(PROFILE)
    object NewPost : DestinationScreen("$NEW_POST/{imageUri}") {
        fun createRoute(uri: String) = "$NEW_POST/$uri"
    }

    companion object {
        private const val SIGNUP = "signup"
        private const val SIGN_IN = "signin"
        private const val FEED = "feed"
        private const val MY_POSTS = "myposts"
        private const val SEARCH = "search"
        private const val PROFILE = "profile"
        private const val NEW_POST = "newpost"
    }
}

fun NavController.navigateTo(destinationScreen: DestinationScreen) {
    navigate(destinationScreen.route) {
        popUpTo(destinationScreen.route)
        launchSingleTop = true
    }
}
