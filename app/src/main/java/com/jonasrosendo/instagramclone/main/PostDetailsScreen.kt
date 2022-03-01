package com.jonasrosendo.instagramclone.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.jonasrosendo.instagramclone.InstagramViewModel
import com.jonasrosendo.instagramclone.R
import com.jonasrosendo.instagramclone.data.Post
import com.jonasrosendo.instagramclone.navigation.DestinationScreen

@ExperimentalCoilApi
@Composable
fun PostDetailsScreen(navController: NavController, viewModel: InstagramViewModel, post: Post) {

    val comments = viewModel.comments.value

    LaunchedEffect(key1 = Unit) {
        viewModel.getComments(post.postId)
    }

    post.userId?.let {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .wrapContentHeight()
        ) {
            Text(text = "Back", modifier = Modifier.clickable { navController.popBackStack() })
            CommonDivider()

            PostDetails(
                navController = navController,
                viewModel = viewModel,
                post = post,
                numberOfComments = comments.size
            )
        }
    }
}

@ExperimentalCoilApi
@Composable
fun PostDetails(
    navController: NavController,
    viewModel: InstagramViewModel,
    post: Post,
    numberOfComments: Int
) {
    val user = viewModel.user.value
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
            ) {
                Image(
                    painter = rememberImagePainter(data = post.userImage),
                    contentDescription = null
                )
            }

            Text(text = post.username ?: "")
            Text(text = ".", modifier = Modifier.padding(8.dp))

            when {
                user?.userId == post.userId -> {}
                user?.following?.contains(post.userId) == true -> {
                    Text(text = "Following", color = Color.Gray, modifier = Modifier.clickable {
                        post.userId?.let { viewModel.onFollowClick(it) }
                    })
                }
                else -> {
                    Text(text = "Follow", color = Color.Blue, modifier = Modifier.clickable {
                        post.userId?.let { viewModel.onFollowClick(it) }
                    })
                }
            }
        }
    }

    Box {
        CommonImage(
            data = post.postImage,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 150.dp),
            contentScale = ContentScale.FillWidth
        )
    }

    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {

        Image(
            painter = painterResource(id = R.drawable.ic_heart),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Color.Red)
        )
        Text(text = "${post.likes?.size ?: 0} likes", modifier = Modifier.padding(start = 0.dp))
    }

    Row(modifier = Modifier.padding(8.dp)) {
        Text(text = post.username ?: "", fontWeight = FontWeight.Bold)
        Text(text = post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))
    }

    Row(modifier = Modifier.padding(8.dp)) {
        Text(
            text = "$numberOfComments comments",
            color = Color.Gray,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable {
                    post.postId?.let {
                        navController.navigate(DestinationScreen.Comments.createRoute(it))
                    }
                }
        )
    }
}
