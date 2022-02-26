package com.jonasrosendo.instagramclone.main

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.jonasrosendo.instagramclone.InstagramViewModel
import com.jonasrosendo.instagramclone.data.Post

@Composable
fun PostDetailsScreen(navController: NavController, viewModel: InstagramViewModel, post: Post) {
    Text(text = "Post Details ${post.postDescription}")
}