package com.jonasrosendo.instagramclone.data

data class Comment(
    val commentId: String? = null,
    val postId: String? = null,
    val username: String? = null,
    val text: String? = null,
    val time: Long? = null
)
