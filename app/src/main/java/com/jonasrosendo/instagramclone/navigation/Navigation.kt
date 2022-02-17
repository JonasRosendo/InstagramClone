package com.jonasrosendo.instagramclone.navigation

import androidx.navigation.NavController

sealed class DestinationScreen(val route: String) {
    object SignUp : DestinationScreen(SIGNUP)
    object SignIn : DestinationScreen(SIGNIN)

    companion object {
        private const val SIGNUP = "SIGNUP"
        private const val SIGNIN = "SIGNIN"
    }
}

fun NavController.navigateTo(destinationScreen: DestinationScreen) {
    navigate(destinationScreen.route) {
        popUpTo(destinationScreen.route)
        launchSingleTop = true
    }
}
