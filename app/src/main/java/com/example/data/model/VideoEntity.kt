package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val thumbnailDrawableName: String? = null,
    val customThumbnailUri: String? = null,
    val videoUri: String? = null,
    val creatorId: String,
    val creatorName: String,
    val creatorHandle: String,
    val creatorAvatarDrawableName: String? = null,
    val creatorCustomAvatarUri: String? = null,
    val duration: String, // e.g. "10:24" for long video or "00:45" for shorts
    val viewsCount: Long = 0L,
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val category: String, // "Gaming", "Tech", "Music", "Adventure", "Creative", "Vlogs"
    val tags: String, // comma-separated e.g. "Cyberpunk,Action,Playzo"
    val createdAtTimestamp: Long,
    val timeAgo: String, // e.g. "2 hours ago"
    val isLikedByMe: Boolean = false,
    val isShort: Boolean = false // TRUE for vertical Shorts, FALSE for Long Videos
)
