package com.jonasrosendo.instagramclone.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jonasrosendo.instagramclone.R
import com.jonasrosendo.instagramclone.navigation.DestinationScreen
import com.jonasrosendo.instagramclone.navigation.navigateTo

enum class BottomNavigationItem(val icon: Int, val destination: DestinationScreen) {

    FEED(R.drawable.ic_home, DestinationScreen.Feed),
    SEARCH(R.drawable.ic_search, DestinationScreen.Search),
    POSTS(R.drawable.ic_person, DestinationScreen.MyPosts)
}

@Composable
fun BottomNavigationMenu(selectedItem: BottomNavigationItem, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp)
            .background(Color.White)
    ) {
        for (item in BottomNavigationItem.values()) {
            Image(
                painter = painterResource(id = item.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(5.dp)
                    .weight(1f)
                    .clickable {
                        navController.navigateTo(item.destination)
                    },
                colorFilter = if (item == selectedItem)
                    ColorFilter.tint(Color.Black)
                else ColorFilter.tint(Color.Gray)
            )
        }
    }
}