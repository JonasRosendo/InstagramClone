package com.jonasrosendo.instagramclone

import android.net.Uri
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
import java.util.*
import javax.inject.Inject

@HiltViewModel
class InstagramViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : ViewModel() {

    private val _signedIn = mutableStateOf(false)
    val signedIn: State<Boolean> = _signedIn
    private val _inProgress = mutableStateOf(false)
    val inProgress: State<Boolean> = _inProgress
    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user
    private val _popupNotification = mutableStateOf<Event<String>?>(null)
    val popupNotification: State<Event<String>?> = _popupNotification

    init {
        //firebaseAuth.signOut()
        val currentUser = firebaseAuth.currentUser
        _signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    fun signUp(username: String, email: String, password: String) {
        if (username.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(message = "Fill in all fields")
            return
        }

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
                handleException(
                    exception = it,
                    message = "Sign up failed"
                )
                _inProgress.value = false

            }
    }

    fun signIn(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(message = "Fill in all fields")
            return
        }

        _inProgress.value = true

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _signedIn.value = true
                    _inProgress.value = false
                    firebaseAuth.currentUser?.uid?.let { uid ->
                        getUserData(uid)
                    }
                } else {
                    handleException(task.exception, "Sign in failed")
                    _inProgress.value = false
                }
            }.addOnFailureListener {
                handleException(it, "Login failed")
                _inProgress.value = false
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
            name = name ?: _user.value?.name,
            username = username ?: _user.value?.username,
            imageUrl = imageUrl ?: _user.value?.imageUrl,
            bio = bio ?: _user.value?.bio,
            following = _user.value?.following
        )

        uid?.let {
            _inProgress.value = true
            firebaseStore.collection(USERS).document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.reference.update(currentUser.toMap())
                            .addOnSuccessListener {
                                _user.value = currentUser
                                _inProgress.value = false
                            }.addOnFailureListener { exception ->
                                handleException(exception, "Cannot update user")
                                _inProgress.value = false
                            }
                    } else {
                        firebaseStore.collection(USERS).document(uid).set(currentUser)
                        getUserData(uid)
                        _inProgress.value = false
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
                this._user.value = user
                _inProgress.value = false
                _popupNotification.value = Event("User data retrieved sucessfully")
            }.addOnFailureListener {
                handleException(it, "Cannot retrieve user data.")
                _inProgress.value = false
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

    fun updateProfileData(name: String, username: String, bio: String) {
        createOrUpdateProfile(name, username, bio)
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        _inProgress.value = true

        val storageRef = firebaseStorage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener { uri ->
                onSuccess(uri)
            }
        }.addOnFailureListener {
            handleException(it)
            _inProgress.value = false
        }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri = uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

}