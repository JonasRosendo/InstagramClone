package com.jonasrosendo.instagramclone.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jonasrosendo.instagramclone.InstagramViewModel
import com.jonasrosendo.instagramclone.R
import com.jonasrosendo.instagramclone.main.CheckSignedIn
import com.jonasrosendo.instagramclone.main.CommonCircularProgress
import com.jonasrosendo.instagramclone.navigation.DestinationScreen
import com.jonasrosendo.instagramclone.navigation.navigateTo

@Composable
fun SignUpScreen(navController: NavController, viewModel: InstagramViewModel) {

    CheckSignedIn(navController = navController, viewModel = viewModel)

    val focus = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            val usernameState = remember { mutableStateOf(TextFieldValue()) }
            val emailState = remember { mutableStateOf(TextFieldValue()) }
            val passwordState = remember { mutableStateOf(TextFieldValue()) }

            Image(
                painter = painterResource(id = R.drawable.ig_logo),
                contentDescription = null,
                modifier = Modifier
                    .width(96.dp)
                    .padding(top = 32.dp)
                    .padding(8.dp)
            )

            Text(
                text = "Welcome to Instagram Clone",
                modifier = Modifier.padding(8.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontFamily = FontFamily.SansSerif
            )

            val fieldModifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()

            OutlinedTextField(
                value = usernameState.value,
                onValueChange = { usernameState.value = it },
                modifier = fieldModifier,
                label = { Text(text = "Username") }
            )

            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                modifier = fieldModifier,
                label = { Text(text = "E-mail") }
            )

            OutlinedTextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                modifier = fieldModifier,
                label = { Text(text = "Password") },
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = {
                    focus.clearFocus(true)
                    viewModel.signUp(
                        usernameState.value.text,
                        emailState.value.text,
                        passwordState.value.text
                    )
                },
                modifier = fieldModifier
            ) {
                Text(
                    text = "Sign Up",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Text(
                text = "Already have an account? Sign In",
                color = Color.Blue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navController.navigateTo(DestinationScreen.SignIn)
                    }
            )
        }

        val isLoading = viewModel.inProgress.value
        if (isLoading) {
            CommonCircularProgress()
        }
    }
}
