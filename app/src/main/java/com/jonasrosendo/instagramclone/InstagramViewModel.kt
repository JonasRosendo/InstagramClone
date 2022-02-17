package com.jonasrosendo.instagramclone

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jonasrosendo.instagramclone.Constants.USERNAME
import com.jonasrosendo.instagramclone.Constants.USERS
import com.jonasrosendo.instagramclone.data.Event
import com.jonasrosendo.instagramclone.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InstagramViewModel @Inject constructor(
    val firebaseAuth: FirebaseAuth,
    val firebaseStore: FirebaseFirestore,
    val firebaseStorage: FirebaseStorage
) : ViewModel() {

    private val _signedIn = mutableStateOf(false)
    private val _inProgress = mutableStateOf(false)
    private val user = mutableStateOf<User?>(null)
    private val _popupNotification = mutableStateOf<Event<String>?>(null)
    val popupNotification: State<Event<String>?> = _popupNotification

    fun signUp(username: String, email: String, password: String) {
        _inProgress.value = true

        firebaseStore.collection(USERS).whereEqualTo(USERNAME, username).get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    handleException(message = "Username already exists")
                    _inProgress.value = false
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(
                        email, password
                    ).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _signedIn.value = true
                        } else {
                            handleException(
                                exception = task.exception,
                                message = "Sign up failed"
                            )
                        }

                        _inProgress.value = false
                    }
                }
            }
            .addOnFailureListener {

            }
    }

    private fun handleException(exception: Exception? = null, message: String? = "") {
        exception?.printStackTrace()
        val errorMessage = exception?.localizedMessage ?: ""
        val customMessage =
            if (message?.isEmpty() == true) errorMessage
            else "$message: $errorMessage"
        _popupNotification.value = Event(customMessage)
    }
}