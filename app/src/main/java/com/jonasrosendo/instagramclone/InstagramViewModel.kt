package com.jonasrosendo.instagramclone

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.jonasrosendo.instagramclone.Constants.POSTS
import com.jonasrosendo.instagramclone.Constants.USERNAME
import com.jonasrosendo.instagramclone.Constants.USERS
import com.jonasrosendo.instagramclone.data.Event
import com.jonasrosendo.instagramclone.data.Post
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

    private val _refreshPostsProgress = mutableStateOf(false)
    val refreshPostsProgress: State<Boolean> = _refreshPostsProgress

    private val _posts = mutableStateOf<List<Post>>(listOf())
    val posts: State<List<Post>> = _posts

    private val _searchedPosts = mutableStateOf<List<Post>>(listOf())
    val searchedPosts: State<List<Post>> = _searchedPosts

    private val _searchPostProgress = mutableStateOf(false)
    val searchPostProgress: State<Boolean> = _searchPostProgress

    private val _feedPosts = mutableStateOf<List<Post>>(listOf())
    val feedPosts: State<List<Post>> = _feedPosts

    private val _feedPostsProgress = mutableStateOf(false)
    val feedPostsProgress: State<Boolean> = _feedPostsProgress

    init {
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
                refreshPosts()
                getPersonalizedFeed()
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
            updatePostUserImageData(it.toString())
        }
    }

    private fun updatePostUserImageData(imageUrl: String) {
        val currentUuid = firebaseAuth.currentUser?.uid
        currentUuid?.let { uid ->
            firebaseStore.collection(POSTS).whereEqualTo("userId", uid).get()
                .addOnSuccessListener { documents ->
                    val posts = mutableStateOf<List<Post>>(arrayListOf())
                    convertPosts(documents, posts)
                    val refs = arrayListOf<DocumentReference>()

                    for (post in posts.value) {
                        post.postId?.let { id ->
                            refs.add(firebaseStore.collection(POSTS).document(id))
                        }
                    }

                    if (refs.isNotEmpty()) {
                        firebaseStore.runBatch { batch ->
                            for (ref in refs) {
                                batch.update(ref, "userImage", imageUrl)
                            }
                        }.addOnSuccessListener {
                            refreshPosts()
                        }
                    }
                }.addOnFailureListener {

                }
        }
    }

    fun onSignOut() {
        firebaseAuth.signOut()
        _signedIn.value = false
        _user.value = null
        _popupNotification.value = Event("Logged out")
        _searchedPosts.value = emptyList()
        _feedPosts.value = emptyList()
    }

    fun onNewPost(uri: Uri, description: String, onPostSuccess: () -> Unit) {
        uploadImage(uri) {
            onCreatePost(it, description, onPostSuccess)
        }
    }

    private fun onCreatePost(imageUri: Uri, description: String, onPostSuccess: () -> Unit) {
        _inProgress.value = true
        val currentUid = firebaseAuth.currentUser?.uid
        val currentUsername = _user.value?.username
        val currentUserImage = _user.value?.imageUrl

        val searchTerms = description.split(" ", ".", ",", "?", "!", "#").map {
            it.lowercase()
        }.filter {
            it.isNotEmpty() and fillerWords.contains(it).not()
        }

        if (currentUid != null) {
            val postUuid = UUID.randomUUID().toString()

            val post = Post(
                postId = postUuid,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUri.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf<String>(),
                searchTerms = searchTerms
            )

            firebaseStore.collection(POSTS).document(postUuid).set(post)
                .addOnSuccessListener {
                    _popupNotification.value = Event("Post successfully created")
                    _inProgress.value = false
                    refreshPosts()
                    onPostSuccess()
                }.addOnFailureListener {
                    handleException(it, "Unable to create post")
                    _inProgress.value = false
                }

        } else {
            handleException(message = "Error: Username unavailable. Unable to create post.")
            onSignOut()
            _inProgress.value = false
        }
    }

    private fun refreshPosts() {
        val currentUid = firebaseAuth.currentUser?.uid
        if (currentUid != null) {
            _refreshPostsProgress.value = true
            firebaseStore.collection(POSTS).whereEqualTo("userId", currentUid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, _posts)
                    _refreshPostsProgress.value = false
                }.addOnFailureListener {
                    handleException(it, "Cannot fetch posts")
                    _refreshPostsProgress.value = false
                }
        } else {
            handleException(message = "Error: Username unavailable. Unable to refresh posts.")
            onSignOut()
            _inProgress.value = false
        }
    }

    private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<Post>>) {
        val newPosts = mutableListOf<Post>()
        documents.forEach { doc ->
            val post = doc.toObject<Post>()
            newPosts.add(post)
        }
        val sortedPosts = newPosts.sortedByDescending { it.time }
        outState.value = sortedPosts
    }

    fun searchPosts(searchTerm: String) {
        if (searchTerm.isNotEmpty()) {
            _searchPostProgress.value = true
            firebaseStore.collection(POSTS).whereArrayContains(
                "searchTerms", searchTerm.trim().lowercase()
            ).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, _searchedPosts)
                    _searchPostProgress.value = false
                }.addOnFailureListener {
                    handleException(it, "Cannot search posts")
                    _searchPostProgress.value = false
                }
        }
    }

    fun onFollowClick(userId: String) {
        firebaseAuth.currentUser?.uid?.let { currentUserId ->
            val following = arrayListOf<String>()
            _user.value?.following?.let {
                following.addAll(it)
            }

            if (following.contains(userId)) {
                following.remove(userId)
            } else {
                following.add(userId)
            }

            firebaseStore.collection(USERS).document(currentUserId).update("following", following)
                .addOnSuccessListener {
                    getUserData(currentUserId)
                }.addOnFailureListener {
                    handleException(it, "Not possible to follow user.")
                }
        }
    }

    private fun getPersonalizedFeed() {
        val following = _user.value?.following
        if (!following.isNullOrEmpty()) {
            _feedPostsProgress.value = true
            firebaseStore.collection(POSTS).whereIn("userId", following).get()
                .addOnSuccessListener {
                    convertPosts(it, _feedPosts)
                    if (_feedPosts.value.isEmpty()) {
                        getGeneralFeed()
                    } else {
                        _feedPostsProgress.value = false
                    }
                }.addOnFailureListener {
                    handleException(it, "Cannot get personalized feed")
                    _feedPostsProgress.value = false
                }
        } else {
            getGeneralFeed()
        }
    }

    private fun getGeneralFeed() {
        _feedPostsProgress.value = true
        val currentTime = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        val timeDifference = currentTime - oneDay

        firebaseStore.collection(POSTS).whereGreaterThan("time", timeDifference).get()
            .addOnSuccessListener {
                convertPosts(documents = it, outState = _feedPosts)
                _feedPostsProgress.value = false
            }.addOnFailureListener {
                handleException(it, "Not possible to get general feed")
                _feedPostsProgress.value = false
            }
    }

    fun onLikePost(post: Post) {
        firebaseAuth.currentUser?.uid?.let { uid ->
            post.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if (likes.contains(uid)) {
                    newLikes.addAll(likes.filter { uid != it })
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(uid)
                }

                post.postId?.let { postId ->
                    firebaseStore.collection(POSTS).document(postId).update("likes", newLikes)
                        .addOnSuccessListener {
                            post.likes = newLikes
                        }.addOnFailureListener {
                            handleException(it, "Unable to like post")
                        }
                }
            }
        }
    }
}