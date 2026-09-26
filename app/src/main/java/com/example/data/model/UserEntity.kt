package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val handle: String, // e.g. "@alex_playz" - unique username
    val avatarDrawableName: String? = null,
    val customAvatarUri: String? = null, // URI from photo picker or custom upload
    val bannerDrawableName: String? = null,
    val bio: String,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowedByMe: Boolean = false,
    val isCurrentUser: Boolean = false,
    val joinedDate: String = "Joined recently"
)
