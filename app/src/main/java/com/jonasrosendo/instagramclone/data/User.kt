package com.jonasrosendo.instagramclone.data

data class User(
    var userId: String? = null,
    var name: String? = null,
    var username: String? = null,
    var imageUrl: String? = null,
    var bio: String? = null,
    var following: List<String>? = null
) {
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "user" to username,
        "imageUrl" to imageUrl,
        "bio" to bio,
        "following" to following
    )
}