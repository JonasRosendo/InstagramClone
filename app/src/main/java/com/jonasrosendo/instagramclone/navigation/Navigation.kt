package com.jonasrosendo.instagramclone.navigation

import android.os.Parcelable
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

    object PostDetails : DestinationScreen(POST_DETAILS)

    companion object {
        private const val SIGNUP = "signup"
        private const val SIGN_IN = "signin"
        private const val FEED = "feed"
        private const val MY_POSTS = "myposts"
        private const val SEARCH = "search"
        private const val PROFILE = "profile"
        private const val NEW_POST = "newpost"
        private const val POST_DETAILS = "postdetails"
    }
}

data class NavParam(
    val name: String,
    val value: Parcelable
)

fun NavController.navigateTo(destinationScreen: DestinationScreen, vararg params: NavParam) {
    for (param in params) {
        currentBackStackEntry?.arguments?.putParcelable(param.name, param.value)
    }

    navigate(destinationScreen.route) {
        popUpTo(destinationScreen.route)
        launchSingleTop = true
    }
}
