package com.jonasrosendo.instagramclone

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.jonasrosendo.instagramclone.Constants.USERNAME
import com.jonasrosendo.instagramclone.Constants.USERS
import com.jonasrosendo.instagramclone.data.Event
import com.jonasrosendo.instagramclone.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InstagramViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : ViewModel() {

    private val _signedIn = mutableStateOf(false)
    private val _inProgress = mutableStateOf(false)
    private val user = mutableStateOf<User?>(null)
    private val _popupNotification = mutableStateOf<Event<String>?>(null)
    val popupNotification: State<Event<String>?> = _popupNotification

    init {
        val currentUser = firebaseAuth.currentUser
        _signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

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
                            createOrUpdateProfile(username = username)
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

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null,
    ) {
        val uid = firebaseAuth.currentUser?.uid
        val currentUser = User(
            userId = uid,
            name = name ?: user.value?.name,
            username = username ?: user.value?.username,
            imageUrl = imageUrl ?: user.value?.imageUrl,
            bio = bio ?: user.value?.bio,
            following = user.value?.following
        )

        uid?.let {
            _inProgress.value = true
            firebaseStore.collection(USERS).document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.reference.update(currentUser.toMap())
                            .addOnSuccessListener {
                                user.value = currentUser
                            }.addOnFailureListener { exception ->
                                handleException(exception, "Cannot update user")
                                _inProgress.value = false
                            }
                    } else {
                        firebaseStore.collection(USERS).document(uid).set(currentUser)
                        getUserData(uid)
                    }
                }.addOnFailureListener { exception ->
                    handleException(exception, "Cannot create user")
                    _inProgress.value = false
                }
        }
    }

    private fun getUserData(uid: String) {
        _inProgress.value = true
        firebaseStore.collection(USERS).document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject<User>()
                this.user.value = user
                _inProgress.value = false
                _popupNotification.value = Event("User data retrieved sucessfully")
            }.addOnFailureListener {
                handleException(it, "Cannot retrieve user data.")
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