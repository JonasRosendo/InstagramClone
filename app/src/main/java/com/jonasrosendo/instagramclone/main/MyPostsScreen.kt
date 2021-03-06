package com.jonasrosendo.instagramclone.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.jonasrosendo.instagramclone.Constants
import com.jonasrosendo.instagramclone.InstagramViewModel
import com.jonasrosendo.instagramclone.R
import com.jonasrosendo.instagramclone.data.Post
import com.jonasrosendo.instagramclone.navigation.DestinationScreen
import com.jonasrosendo.instagramclone.navigation.NavParam
import com.jonasrosendo.instagramclone.navigation.navigateTo

data class PostRow(
    var post1: Post? = null,
    var post2: Post? = null,
    var post3: Post? = null
) {
    fun isFull() = post1 != null && post2 != null && post3 != null

    fun add(post: Post) {
        when {
            post1 == null -> {
                post1 = post
            }
            post2 == null -> {
                post2 = post
            }
            post3 == null -> {
                post3 = post
            }
        }
    }
}

@ExperimentalCoilApi
@Composable
fun MyPostsScreen(navController: NavController, viewModel: InstagramViewModel) {

    val newPostLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            val encoded = Uri.encode(it.toString())
            val route = DestinationScreen.NewPost.createRoute(encoded)
            navController.navigate(route)
        }
    }

    val user = viewModel.user.value
    val isLoading = viewModel.inProgress.value
    val postsLoading = viewModel.refreshPostsProgress.value
    val posts = viewModel.posts.value
    val followers = viewModel.followers.value

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row {
                ProfileImage(user?.imageUrl) {
                    newPostLauncher.launch("image/*")
                }

                Text(
                    text = "${posts.size ?: 0} \nposts",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$followers \nfollowers",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${user?.following?.size ?: 0} \nfollowing",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                val usernameDisplay = if (user?.username == null) "" else "@${user.username}"
                Text(text = user?.name ?: "", fontWeight = FontWeight.Bold)
                Text(text = usernameDisplay)
                Text(text = user?.bio ?: "This is my ig account")
            }

            OutlinedButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                shape = RoundedCornerShape(10),
                onClick = { navController.navigateTo(DestinationScreen.Profile) }
            ) {
                Text(text = "Edit Profile", color = Color.Black)
            }

            PostList(
                isContextLoading = isLoading,
                postsLoading = postsLoading,
                posts = posts,
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp)
                    .fillMaxSize()
            ) { post ->
                navController.navigateTo(
                    DestinationScreen.PostDetails,
                    NavParam(name = Constants.POST, post)
                )
            }
        }

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )
    }

    if (isLoading) {
        CommonCircularProgress()
    }
}

@ExperimentalCoilApi
@Composable
fun ProfileImage(imageUrl: String?, onClick: () -> Unit) {
    Box(modifier = Modifier
        .padding(top = 16.dp)
        .clickable { onClick() }) {
        UserImageCard(
            userImage = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )

        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(
                    Alignment.BottomEnd
                )
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.background(Color.Blue)
            )
        }
    }
}


@ExperimentalCoilApi
@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<Post>,
    modifier: Modifier,
    onPostClick: (Post) -> Unit
) {
    when {
        postsLoading -> {
            CommonCircularProgress()
        }
        posts.isEmpty() -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isContextLoading.not()) Text("No posts available.")
            }
        }
        else -> {
            LazyColumn(modifier = modifier) {
                val rows = arrayListOf<PostRow>()
                var currentRow = PostRow()
                rows.add(currentRow)

                for (post in posts) {
                    if (currentRow.isFull()) {
                        currentRow = PostRow()
                        rows.add(currentRow)
                    }

                    currentRow.add(post = post)
                }

                items(items = rows) { row ->
                    PostRow(item = row, onPostClick = onPostClick)
                }
            }
        }
    }

}

@ExperimentalCoilApi
@Composable
fun PostRow(item: PostRow, onPostClick: (Post) -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        PostImage(
            imageUrl = item.post1?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable { item.post1?.let { post -> onPostClick(post) } })

        PostImage(
            imageUrl = item.post2?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable { item.post2?.let { post -> onPostClick(post) } })

        PostImage(
            imageUrl = item.post3?.postImage,
            modifier = Modifier
                .weight(1f)
                .clickable { item.post3?.let { post -> onPostClick(post) } })
    }
}

@ExperimentalCoilApi
@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {

    Box(modifier = modifier) {
        var modifier = Modifier
            .padding(1.dp)
            .fillMaxSize()

        if (imageUrl == null) {
            modifier = modifier.clickable(enabled = false) {}
        }

        CommonImage(data = imageUrl, modifier = modifier, contentScale = ContentScale.Crop)
    }
}