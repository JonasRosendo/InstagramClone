package com.jonasrosendo.instagramclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.jonasrosendo.instagramclone.auth.SignInScreen
import com.jonasrosendo.instagramclone.auth.SignUpScreen
import com.jonasrosendo.instagramclone.data.Post
import com.jonasrosendo.instagramclone.main.*
import com.jonasrosendo.instagramclone.navigation.DestinationScreen
import com.jonasrosendo.instagramclone.ui.theme.InstagramCloneTheme
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalCoilApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstagramCloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    InstagramApp()
                }
            }
        }
    }
}

@ExperimentalCoilApi
@Composable
fun InstagramApp() {
    val vm = hiltViewModel<InstagramViewModel>()
    val navController = rememberNavController()

    NotificationMessage(viewModel = vm)
    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route) {
        composable(DestinationScreen.SignUp.route) {
            SignUpScreen(navController = navController, viewModel = vm)
        }

        composable(DestinationScreen.SignIn.route) {
            SignInScreen(navController = navController, viewModel = vm)
        }

        composable(DestinationScreen.Feed.route) {
            FeedScreen(navController = navController, viewModel = vm)
        }

        composable(DestinationScreen.MyPosts.route) {
            MyPostsScreen(navController = navController, viewModel = vm)
        }

        composable(DestinationScreen.Search.route) {
            SearchScreen(navController = navController, viewModel = vm)
        }

        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController = navController, viewModel = vm)
        }

        composable(DestinationScreen.NewPost.route) { navBackStackEntry ->
            val imageUri = navBackStackEntry.arguments?.getString("imageUri")
            imageUri?.let {
                NewPostScreen(navController = navController, viewModel = vm, encodedUri = it)
            }
        }

        composable(DestinationScreen.PostDetails.route) {
            val post =
                navController.previousBackStackEntry?.arguments?.getParcelable<Post>(Constants.POST)

            post?.let {
                PostDetailsScreen(navController = navController, viewModel = vm, post = post)
            }
        }
    }
}

@ExperimentalCoilApi
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InstagramCloneTheme {
        InstagramApp()
    }
}