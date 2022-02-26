package com.jonasrosendo.instagramclone.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.jonasrosendo.instagramclone.InstagramViewModel

@Composable
fun MyPostsScreen(navController: NavController, viewModel: InstagramViewModel) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.weight(1f))
        {
            Text(text = "Posts screen")
        }
        BottomNavigationMenu(selectedItem = BottomNavigationItem.POSTS, navController = navController)
    }
}