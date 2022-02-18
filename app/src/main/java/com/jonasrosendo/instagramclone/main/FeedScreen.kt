package com.jonasrosendo.instagramclone.main

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.jonasrosendo.instagramclone.InstagramViewModel

@Composable
fun FeedScreen(navController: NavController, viewModel: InstagramViewModel) {
    Text(text = "Feed")
}