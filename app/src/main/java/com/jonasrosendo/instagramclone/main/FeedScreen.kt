package com.jonasrosendo.instagramclone.main

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.jonasrosendo.instagramclone.Constants.POST
import com.jonasrosendo.instagramclone.InstagramViewModel
import com.jonasrosendo.instagramclone.data.Post
import com.jonasrosendo.instagramclone.navigation.DestinationScreen
import com.jonasrosendo.instagramclone.navigation.NavParam
import com.jonasrosendo.instagramclone.navigation.navigateTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalCoilApi
@Composable
fun FeedScreen(navController: NavController, viewModel: InstagramViewModel) {

    val userDataIsLoading = viewModel.inProgress.value
    val user = viewModel.user.value
    val personalizedFeed = viewModel.feedPosts.value
    val personalizedFeedLoading = viewModel.feedPostsProgress.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        )
        {
            UserImageCard(userImage = user?.imageUrl)
        }

        FeedPostList(
            posts = personalizedFeed,
            modifier = Modifier.weight(1f),
            loading = personalizedFeedLoading or userDataIsLoading,
            navController = navController,
            viewModel = viewModel,
            currentUserId = user?.userId ?: ""
        )

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.FEED,
            navController = navController
        )
    }
}

@ExperimentalCoilApi
@Composable
fun FeedPostList(
    posts: List<Post>,
    modifier: Modifier,
    loading: Boolean,
    navController: NavController,
    viewModel: InstagramViewModel,
    currentUserId: String
) {
    Box(modifier = modifier) {
        LazyColumn {
            items(items = posts) {
                PostItem(post = it, currentUserId = currentUserId, viewModel = viewModel) {
                    navController.navigateTo(DestinationScreen.PostDetails, NavParam(POST, it))
                }
            }
        }

        if (loading) CommonCircularProgress()
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@ExperimentalCoilApi
@Composable
fun PostItem(
    post: Post,
    currentUserId: String,
    viewModel: InstagramViewModel,
    onPostClick: () -> Unit
) {

    val likeAnimation = remember { mutableStateOf(false) }
    val dislikeAnimation = remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(corner = CornerSize(4.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape, modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    CommonImage(data = post.userImage, contentScale = ContentScale.Crop)
                }

                Text(text = post.username ?: "", modifier = Modifier.padding(4.dp))
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CommonImage(
                    data = post.postImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 150.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (post.likes?.contains(currentUserId) == true) {
                                        dislikeAnimation.value = true
                                    } else {
                                        likeAnimation.value = true
                                    }
                                    viewModel.onLikePost(post)
                                },
                                onTap = {
                                    onPostClick()
                                }
                            )
                        },
                    contentScale = ContentScale.FillWidth
                )

                if (likeAnimation.value) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L)
                        likeAnimation.value = false
                    }
                    LikeAnimation()
                }

                if (dislikeAnimation.value) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L)
                        dislikeAnimation.value = false
                    }
                    LikeAnimation(false)
                }
            }
        }
    }
}