package com.jonasrosendo.instagramclone.main

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.jonasrosendo.instagramclone.InstagramViewModel
import com.jonasrosendo.instagramclone.R
import com.jonasrosendo.instagramclone.navigation.DestinationScreen

@Composable
fun NotificationMessage(viewModel: InstagramViewModel) {
    val notificationState = viewModel.popupNotification.value
    val notificationMessage = notificationState?.getContentOrNull()
    if (notificationMessage.isNullOrEmpty().not()) {
        Toast.makeText(LocalContext.current, notificationMessage, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun CommonCircularProgress() {
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) {}
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun CheckSignedIn(navController: NavController, viewModel: InstagramViewModel) {
    val alreadySignedIn = remember { mutableStateOf(false) }
    val signedIn = viewModel.signedIn.value

    if (signedIn && alreadySignedIn.value.not()) {
        alreadySignedIn.value = true
        navController.navigate(DestinationScreen.Feed.route) {
            popUpTo(0)
        }
    }
}

@ExperimentalCoilApi
@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop
) {
    val painter = rememberImagePainter(data = data)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale
    )

    if (painter.state is ImagePainter.State.Loading) {
        CommonCircularProgress()
    }
}

@ExperimentalCoilApi
@Composable
fun UserImageCard(
    userImage: String?,
    modifier: Modifier = Modifier
        .padding(8.dp)
        .size(64.dp)
) {
    Card(shape = CircleShape, modifier = modifier) {
        if (userImage.isNullOrEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = Color.Gray)
            )
        } else {
            CommonImage(data = userImage)
        }
    }
}

@Composable
fun CommonDivider() {
    Divider(
        color = Color.Gray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(vertical = 8.dp)
    )
}