package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val videoId: String,
    val userId: String,
    val userName: String,
    val userHandle: String,
    val userAvatarDrawableName: String? = null,
    val text: String,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAtTimestamp: Long,
    val timeAgo: String = "Just now"
)
