package com.jonasrosendo.instagramclone.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import com.jonasrosendo.instagramclone.InstagramViewModel
import com.jonasrosendo.instagramclone.navigation.DestinationScreen
import com.jonasrosendo.instagramclone.navigation.navigateTo

@ExperimentalCoilApi
@Composable
fun ProfileScreen(navController: NavController, viewModel: InstagramViewModel) {
    val isLoading = viewModel.inProgress.value

    if (isLoading) {
        CommonCircularProgress()
    } else {
        val user = viewModel.user.value
        var name by rememberSaveable { mutableStateOf(user?.name ?: "") }
        var username by rememberSaveable { mutableStateOf(user?.username ?: "") }
        var bio by rememberSaveable { mutableStateOf(user?.bio ?: "") }

        ProfileContent(
            viewModel = viewModel,
            name = name,
            username = username,
            bio = bio,
            onNameChanged = { name = it },
            onUsernameChanged = { username = it },
            onBioChanged = { bio = it },
            onSave = { viewModel.updateProfileData(name, username, bio) },
            onBack = { navController.navigateTo(DestinationScreen.MyPosts) },
            onLogout = {
                viewModel.onSignOut()
                navController.navigateTo(DestinationScreen.SignIn)
            }
        )
    }
}

@ExperimentalCoilApi
@Composable
fun ProfileContent(
    viewModel: InstagramViewModel,
    name: String,
    username: String,
    bio: String,
    onNameChanged: (String) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val imageUrl = viewModel.user.value?.imageUrl

    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", modifier = Modifier.clickable { onBack() })
            Text(text = "Save", modifier = Modifier.clickable { onSave() })
        }

        CommonDivider()

        // user Image
        ProfileImage(imageUrl = imageUrl, viewModel = viewModel)

        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", modifier = Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChanged,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Username", modifier = Modifier.width(100.dp))
            TextField(
                value = username,
                onValueChange = onUsernameChanged,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "Bio", modifier = Modifier.width(100.dp))
            TextField(
                value = bio,
                onValueChange = onBioChanged,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                ),
                singleLine = false,
                modifier = Modifier.height(150.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Logout", modifier = Modifier.clickable { onLogout() })
        }
    }
}

@ExperimentalCoilApi
@Composable
fun ProfileImage(imageUrl: String?, viewModel: InstagramViewModel) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadProfileImage(it)
        }
    }

    Box(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable { launcher.launch("image/*") },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape, modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                CommonImage(data = imageUrl)
            }
            Text(text = "Change profile picture")
        }

        val isLoading = viewModel.inProgress.value
        if (isLoading) {
            CommonCircularProgress()
        }
    }
}